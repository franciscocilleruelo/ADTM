package es.uned.master.software.tfm.adtm.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;

/**
 * Componente para enviar a una cola determinada una transaccion 
 * 
 * @author Francisco Cilleruelo
 */
@Component
public class Producer {
	
	private static final Logger log = LoggerFactory.getLogger(Producer.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	/**
	 * Envia la transaccion a la cola indicada
	 * 
	 * @param routingKey La cola destino a la que sera enviada la transaccion
	 * @param transaction Transaccion a enviar
	 */
	public void sendTo(String routingKey, TransactionElement<?> transaction){
		try {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(transaction);
			this.rabbitTemplate.convertAndSend(routingKey, message);
			log.info("Mensaje {} enviado satisfactoriamente a la cola {}", message, routingKey);
		} catch (JsonProcessingException jsonEx){
			log.error("Error al intentar convertir un objeto a JSON para ser transmitido a la cola {}", routingKey);
		} catch (Exception ex){
			log.error("Error en el envio del mensaje a la cola {}", routingKey);
		}
	}
	

}
