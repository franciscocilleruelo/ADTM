package es.uned.master.software.tfm.adtm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
import es.uned.master.software.tfm.adtm.task.ResponseCheckingTask;

/**
 * Servicio (transaccional) encargado de la logica de negocio del gestor de transacciones
 * 
 * @author Francisco Cilleruelo
 */
@Service
@Transactional
public class DistributedTransactionService {
	
	private static final Logger log = LoggerFactory.getLogger(DistributedTransactionService.class);
	
	@Autowired
	private TransactionDataRepository transactionDataRepository;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private AmpqUtil ampqUtil;
	
	@Autowired
	private Map<Long, SenderConsumer> senderConsumerRepository;
	
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	
	/**
	 * Metodo invocado cuando el emisor no ha recibido respuesta del receptor pasado el tiempo limite establecido para la transaccion
	 * 
	 * @param transactionData Transacction sin respuesta a actualizar
	 * @return Transaccion actualizada
	 */
	public TransactionData transactionResponseNotReceived(TransactionData transactionData){
		log.info("Indicamos que la transacción {} no ha recibido respuesta", transactionData.getTransactionDataId());
		transactionData.setStatus(TransactionStatus.NOT_RECEIVED.toString());
		transactionData.setResponseCheckedDate(new Date());
		log.info("Ejecutamos el rollback definido en la transaccion");
		if (senderConsumerRepository.containsKey(transactionData.getTransactionDataId())){
			log.info("Ejecutamos el rollback asociado a la transacción {}", transactionData.getTransactionDataId());
			senderConsumerRepository.get(transactionData.getTransactionDataId()).rollback(transactionData.getObjectTransmited());
			log.info("Eliminamos el componente que debería recibir la respuesta de la transaccion {}", transactionData.getTransactionDataId());
			senderConsumerRepository.remove(transactionData.getTransactionDataId());
		}
		return transactionDataRepository.save(transactionData);
	}
	
	/**
	 * Metodo invocado cuando el emisor ha recibido una respuesta por parte del receptor
	 * 
	 * @param transaction Transaccion con respuesta a actualizar
	 * @return Transaccion actualizada
	 */
	public TransactionData transactionResponseReceived(TransactionElement<?> transaction){
		log.info("Se obtiene la transacccion {} persistida para actualizar su estado", transaction.getTransactionReference());
		TransactionData transactionData = transactionDataRepository.findOne(transaction.getTransactionReference());
		transactionData.setReceivedDate(new Date());
		transactionData.setStatus(transaction.getStatus().toString());
		transactionData.setAdditionalInfo(transaction.getAdditionalInfo());
		log.info("Eliminamos el componente ya ha recibido la respuesta de la transaccion {}", transactionData.getTransactionDataId());
		senderConsumerRepository.remove(transactionData.getTransactionDataId());
		return transactionDataRepository.save(transactionData);
	}
	
	/**
	 * Metodo invocado para empezar una transaccion
	 * 
	 * @param transaction Transaccion a empezar
	 * @return Transaccion persistida
	 */
	public TransactionData startTransaction(Transaction<?> transaction){
		TransactionData transactionData = new TransactionData(transaction);
		log.info("Se empieza una nueva transacción");
		transactionData.setStartDate(new Date());
		log.info("Se guarda para ser enviado de acuerdo con el proceso de envio de transacciones establecido periodicamente");
		transactionData.setStatus(TransactionStatus.TO_BE_SENT.toString());
		TransactionData transactionDataSaved = transactionDataRepository.save(transactionData);
		log.info("Asociamos para la transaccion recien creada {} el listener de la cola donde espera recibir la respuesta", transactionDataSaved.getTransactionDataId());
		beanFactory.autowireBean(transaction.getSenderConsumer());
		senderConsumerRepository.put(transactionData.getTransactionDataId(), transaction.getSenderConsumer());
		return transactionDataSaved;
	}
	
	/**
	 * @return Transaccion pendientes de ser enviadas
	 */
	public List<TransactionData> getTransactionsToBeSent(){
		log.info("Se obtienen las transacciones pendientes de ser enviadas");
		return transactionDataRepository.getTransactionToBeSent();
	}
	
	/**
	 * Metodo para recuperar una transaccion persistida por su identificador
	 * 
	 * @param transactionId Identificador de la transaccion
	 * @return La transaccion persistida para el identificador indicado
	 */
	public TransactionData getTransactionDataById(Long transactionId){
		log.info("Recuperamos los datos asociados a la transacción, incluyendo el Executor");
		return transactionDataRepository.findOne(transactionId);
	}
	
	/**
	 * Metodo invocado para enviar una transaccion
	 * 
	 * @param transactionData Transaccion a enviar
	 * @throws SendingException Excepcion por error de envio
	 */
	public void sendTransaction(TransactionData transactionData) throws SendingException{
		try {
			log.info("Se procede a enviar la transacción {} a su cola de envio {}", transactionData.getTransactionDataId(), 
					transactionData.getRequestQueueName());
			TransactionElement<?> transactionElement = new TransactionElement(transactionData);
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
			SenderConsumer<?> senderConsumer = senderConsumerRepository.get(transactionData.getTransactionDataId());
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
	
	/**
	 * Metodo invocado para recibir una transaccion por parte del receptor
	 * 
	 * @param requestQueueName Nombre de la cola donde el receptor espera recibir la transaccion
	 * @param receiverConsumer Componente encargado de recibir y procesar la transaccion
	 */
	public void receiveTransaction(String requestQueueName, ReceiverConsumer<?> receiverConsumer){
		try {
			log.info("Se procede a crear el listener correspondiente para la cola {} donde se espera recibir una transaccion", requestQueueName);
			beanFactory.autowireBean(receiverConsumer);
			ampqUtil.createRabbitListener(requestQueueName, receiverConsumer);
		} catch (Exception ex){
			log.error("Error al crear el listener para la cola {} donde se espera recibir la respuesta de una transaccion", requestQueueName);
		}
	}
	
	/**
	 * Metodo invocado por el receptor para devolver al emisor la transaccion una vez que la ha recibido y procesado
	 * 
	 * @param transaction Elemento transmitido entre emisor y receptor y que representa la transaccion
	 */
	public void sendResponse(TransactionElement<?> transaction){
		producer.sendTo(transaction.getResponseQueueName(), transaction);
	}

}
