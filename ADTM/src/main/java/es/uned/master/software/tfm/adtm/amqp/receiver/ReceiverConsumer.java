package es.uned.master.software.tfm.adtm.amqp.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;

public abstract class ReceiverConsumer<T> {

	private static final Logger log = LoggerFactory.getLogger(ReceiverConsumer.class);
	
	private RabbitTemplate rabbitTemplate;
	
	public ReceiverConsumer(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}
	
	public void handleMessage(String message){
		log.info("Recibido mensaje {}", message);
		try {
			ObjectMapper mapper = new ObjectMapper();
			TransactionElement transaction = mapper.readValue(message, TransactionElement.class);
			try {
				processRequest((T)transaction.getObjectTransmited());
				log.info("La respuesta se ha procesado correctamente");
			} catch (Exception ex){
				log.info("La respuesta NO se ha procesado correctamente");
				transaction.setStatus(TransactionStatus.RECEIVED_NOK);
			}
			log.info("Procedemos a comunicarselo al emisor a traves de la cola {}", transaction.getResponseQueueName());
			String replyMessage = mapper.writeValueAsString(transaction);
			this.rabbitTemplate.convertAndSend(transaction.getResponseQueueName(), replyMessage);
		} catch (Exception ex){
			log.error("Error en la conversión del mensaje JSON {}", message);
		}		
	}
	
	public abstract void processRequest(T requestObject);
}
