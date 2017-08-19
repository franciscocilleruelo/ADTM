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
import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;
import es.uned.master.software.tfm.adtm.amqp.util.AmpqUtil;
import es.uned.master.software.tfm.adtm.entity.Transaction;
import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;
import es.uned.master.software.tfm.adtm.exception.SendingException;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.jpa.repository.TransactionDataRepository;
import es.uned.master.software.tfm.adtm.repository.SenderConsumerRepository;
import es.uned.master.software.tfm.adtm.task.ResponseCheckingTask;

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
	private SenderConsumerRepository senderConsumerRepository;
	
	public TransactionData transactionResponseNotReceived(TransactionData transactionData){
		log.info("Indicamos que la transacción {} no ha recibido respuesta", transactionData.getTransactionDataId());
		transactionData.setStatus(TransactionStatus.NOT_RECEIVED.toString());
		transactionData.setResponseCheckedDate(new Date());
		log.info("Ejecutamos el rollback definido en la transaccion");
		if (senderConsumerRepository.containsKey(transactionData.getTransactionDataId())){
			log.info("Ejecutamos el rollback asociado a la transacción");
			senderConsumerRepository.get(transactionData.getTransactionDataId()).rollback(transactionData.getObjectTransmited());
		}
		return transactionDataRepository.save(transactionData);
	}
	
	public TransactionData startTransaction(Transaction transaction){
		TransactionData transactionData = new TransactionData(transaction);
		log.info("Se empieza una nueva transacción");
		transactionData.setStartDate(new Date());
		log.info("Se guarda para ser enviado de acuerdo con el proceso de envio de transacciones establecido periodicamente");
		transactionData.setStatus(TransactionStatus.TO_BE_SENT.toString());
		TransactionData transactionDataSaved = transactionDataRepository.save(transactionData);
		log.info("Asociamos para la transaccion recien creada {} el listener de la cola donde espera recibir la respuesta", transactionDataSaved.getTransactionDataId());
		senderConsumerRepository.put(transactionData.getTransactionDataId(), transactionData.getSenderConsumer());
		return transactionDataSaved;
	}
	
	public List<TransactionData> getTransactionsToBeSent(){
		log.info("Se obtienen las transacciones pendientes de ser enviadas");
		return transactionDataRepository.getTransactionToBeSent();
	}
	
	public TransactionData getTransactionDataById(Long transactionId){
		log.info("Recuperamos los datos asociados a la transacción, incluyendo el Executor");
		return transactionDataRepository.findOne(transactionId);
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
				ResponseCheckingTask checkerTask = new ResponseCheckingTask(this, transactionData);
				log.info("Lanzamos el hilo de ejecución para comprobar si se ha recibido la respuesta para la transaccion {} en un tiempo maximo de {} msg",
						transactionData.getTransactionDataId(), transactionData.getMaxResponseTime());
				taskScheduler.execute(checkerTask, transactionData.getMaxResponseTime());
			}
			log.info("Recuperamos el consumidor de la respuesta para la transaccion {}", transactionData.getTransactionDataId());
			SenderConsumer senderConsumer = senderConsumerRepository.get(transactionData.getTransactionDataId());
			String responseQueueName = transactionData.getResponseQueueName();
			log.info("Creamos el listener (y la cola si fuese necesario) de la cola {} donde la transaccion {} espera recibir la respuesta", responseQueueName, transactionData.getTransactionDataId());
			ampqUtil.createRabbitListener(responseQueueName, senderConsumer);		
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
			ampqUtil.createRabbitListener(responseQueueName, receiverConsumer);
		} catch (Exception ex){
			log.error("Error al crear el listener para la cola {} donde se espera recibir la respuesta de una transaccion", responseQueueName);
		}
	}

}
