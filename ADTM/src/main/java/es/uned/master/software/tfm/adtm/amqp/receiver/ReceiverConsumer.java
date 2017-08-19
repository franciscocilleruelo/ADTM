package es.uned.master.software.tfm.adtm.amqp.receiver;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;

/**
 * Componente para recibir transacciones por parte del receptor de estas
 * 
 * @author Francisco Cilleruelo
 *
 * @param <T> Tipo de objeto de negocio (serializable) que espera recibir el receptor de la transaccion
 */
public abstract class ReceiverConsumer<T extends Serializable> implements Serializable{

	private static final long serialVersionUID = 7278191552064450096L;

	private static final Logger log = LoggerFactory.getLogger(ReceiverConsumer.class);
	
	private RabbitTemplate rabbitTemplate;
	private Class<T> classType;
	
	public ReceiverConsumer(RabbitTemplate rabbitTemplate, Class<T> classType){
		this.rabbitTemplate = rabbitTemplate;
		this.classType = classType;
	}
	
	/**
	 * Metodo encargado de recibir la transaccion como un String JSON
	 * 
	 * @param message Mensaje recibido
	 */
	public void handleMessage(String message){
		log.info("Recibido mensaje {}", message);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JavaType javaType = mapper.getTypeFactory().constructParametricType(TransactionElement.class, classType);
			TransactionElement<T> transaction = mapper.readValue(message, javaType);
			try {
				log.info("Se procesa la transaccion recibida");
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
				transaction.setAdditionalInfo("La respuesta NO se ha procesado correctamente" + ex.getMessage());
			}
			log.info("Procedemos a comunicar al emisor el resultado del procesamiento de la transaccion a traves de la cola {}", transaction.getResponseQueueName());
			String replyMessage = mapper.writeValueAsString(transaction);
			this.rabbitTemplate.convertAndSend(transaction.getResponseQueueName(), replyMessage);
		} catch (Exception ex){
			log.error("Error en la conversión del mensaje JSON {}", message);
		}		
	}
	
	/**
	 * Metodo a implementar por el receptor de la transaccion para definir el procesamiento
	 * a realizar con la transaccion. Para indicar posteriormente si se puede dar la transaccion
	 * como correcta o no
	 * 
	 * @param requestObject Objeto de negocio a procesar
	 * @return Resultado del procesamiento: Correcto (true) o Incorrecto (false)
	 */
	public abstract boolean processRequest(T requestObject);
}
