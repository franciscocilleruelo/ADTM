package es.uned.master.software.tfm.adtm.amqp.receiver;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;

public abstract class ReceiverConsumer<T extends Serializable> implements Serializable{

	private static final long serialVersionUID = 7278191552064450096L;

	private static final Logger log = LoggerFactory.getLogger(ReceiverConsumer.class);
	
	private RabbitTemplate rabbitTemplate;
	private Class<T> classType;
	
	public ReceiverConsumer(RabbitTemplate rabbitTemplate, Class<T> classType){
		this.rabbitTemplate = rabbitTemplate;
		this.classType = classType;
	}
	
	public void handleMessage(String message){
		log.info("Recibido mensaje {}", message);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JavaType javaType = mapper.getTypeFactory().constructParametricType(TransactionElement.class, classType);
			TransactionElement<T> transaction = mapper.readValue(message, javaType);
			try {
				boolean resultado = processRequest((T)transaction.getObjectTransmited());
				if (resultado){
					log.info("La respuesta se ha procesado correctamente");
					transaction.setStatus(TransactionStatus.RECEIVED_OK);
				} else {
					log.info("La respuesta NO se ha procesado correctamente");
					transaction.setStatus(TransactionStatus.RECEIVED_NOK);
				}
			} catch (Exception ex){
				log.info("La respuesta NO se ha procesado correctamente");
				transaction.setStatus(TransactionStatus.RECEIVED_NOK);
				transaction.setAdditionalInfo(ex.getMessage());
			}
			log.info("Procedemos a comunicarselo al emisor a traves de la cola {}", transaction.getResponseQueueName());
			String replyMessage = mapper.writeValueAsString(transaction);
			this.rabbitTemplate.convertAndSend(transaction.getResponseQueueName(), replyMessage);
		} catch (Exception ex){
			log.error("Error en la conversión del mensaje JSON {}", message);
		}		
	}
	
	public abstract boolean processRequest(T requestObject);
}
