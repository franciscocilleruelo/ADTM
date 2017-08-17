package es.uned.master.software.tfm.adtm.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uned.master.software.tfm.adtm.amqp.Producer;
import es.uned.master.software.tfm.adtm.amqp.receiver.ReceiverConsumer;
import es.uned.master.software.tfm.adtm.amqp.util.AmpqUtil;
import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionExecutorStore;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;
import es.uned.master.software.tfm.adtm.exception.SendingException;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.jpa.repository.TransactionDataRepository;
import es.uned.master.software.tfm.adtm.task.ResponseCheckerTask;

@Service
@Transactional
public class TransactionDataService {
	
	private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);
	
	@Autowired
	private TransactionDataRepository transactionDataRepository;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private AmpqUtil ampqUtil;
	
	@Autowired
	private TransactionExecutorStore executorMap;
	
	public TransactionData transactionResponseNotReceived(TransactionData transactionData){
		log.info("Indicamos que la transacción {} no ha recibido respuesta", transactionData.getTransactionDataId());
		transactionData.setStatus(TransactionStatus.NO_RECEIVED.toString());
		transactionData.setResponseCheckedDate(new Date());
		return transactionDataRepository.save(transactionData);
	}
	
	public TransactionData startTransaction(TransactionData transactionData){
		log.info("Se empieza una nueva transacción");
		transactionData.setStartDate(new Date());
		transactionData.setStatus(TransactionStatus.TO_BE_SENT.toString());
		TransactionData transactionDataSaved = transactionDataRepository.save(transactionData);
		executorMap.put(transactionDataSaved.getTransactionDataId(), transactionData.getExecutor());
		return transactionDataSaved;
	}
	
	public List<TransactionData> getTransactionsToBeSent(){
		log.info("Se obtienen las transacciones pendientes de ser enviadas");
		return transactionDataRepository.getTransactionToBeSent();
	}
	
	public TransactionData getTransactionDataById(Long transactionId){
		log.info("Recuperamos los datos asociados a la transacción, incluyendo el Executor");
		TransactionData transactionData = transactionDataRepository.findOne(transactionId);
		if (executorMap.containsKey(transactionData.getTransactionDataId())){
			transactionData.setExecutor(executorMap.get(transactionData.getTransactionDataId()));
		}
		return transactionData;
	}
	
	public void sendTransaction(TransactionData transactionData) throws SendingException{
		try {
			log.info("Se procede a enviar la transacción {} a su cola de envio {}", transactionData.getTransactionDataId(), 
					transactionData.getRequestQueueName());
			TransactionElement transactionElement = new TransactionElement(transactionData);
			producer.sendTo(transactionData.getRequestQueueName(), transactionElement);
			log.info("Se ha enviado la transacción {} a su cola de envio {}", transactionData.getTransactionDataId(), 
					transactionData.getRequestQueueName());
			transactionData.setSentDate(new Date());
			transactionData.setStatus(TransactionStatus.SENT.toString());
			log.info("Se guarda la transacción {} como enviada", transactionData.getTransactionDataId());
			transactionDataRepository.save(transactionData);
			if (transactionData.getMaxResponseTime()>0){ // Se ha establecido un tiempo máximo para recibir la respuesta
				log.info("Se ha establecido un tiempo maximo de respuesta de {} msg", transactionData.getMaxResponseTime());
				ResponseCheckerTask checkerTask = new ResponseCheckerTask(this, transactionData);
				log.info("Lanzamos el hilo de ejecución para comprobar si se ha recibido la respuesta para la transaccion {} en un tiempo maximo de {} msg",
						transactionData.getTransactionDataId(), transactionData.getMaxResponseTime());
				taskScheduler.execute(checkerTask, transactionData.getMaxResponseTime());
			}
			String responseQueueName = transactionData.getResponseQueueName();
			ampqUtil.createRabbitListenerForSender(responseQueueName);		
		} catch (Exception ex){
			log.error("Ha habido un error en el envio de la transacción {} a su cola de envio {}:", transactionData.getTransactionDataId(), 
					transactionData.getRequestQueueName(), ex);
			int sentTries = transactionData.getSentTries();
			transactionData.setSentDate(null);
			transactionData.setSentTries(sentTries+1);	
			log.info("Se guarda la transacción {} como NO enviada con un numero de intentos alcanzado de {}", transactionData.getTransactionDataId(),
					sentTries);
			transactionDataRepository.save(transactionData);
			throw new SendingException();
		}
	}
	
	public void receiveTransactionReponse(String responseQueueName, ReceiverConsumer receiverConsumer){
		try {
			log.info("Se procede a crear el listener correspondiente para la cola {} donde se espera recibir una transaccion", responseQueueName);
			ampqUtil.createRabbitListenerForReceiver(responseQueueName, receiverConsumer);
		} catch (Exception ex){
			log.error("Error al crear el listener para la cola {} donde se espera recibir la respuesta de una transaccion", responseQueueName);
		}
	}

}
