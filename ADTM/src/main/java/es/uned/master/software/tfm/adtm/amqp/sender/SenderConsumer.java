package es.uned.master.software.tfm.adtm.amqp.sender;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;

public abstract class SenderConsumer<T extends Serializable> implements Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(SenderConsumer.class);
	
	private Class<T> classType;
	
	public SenderConsumer(Class<T> classType) {
		super();
		this.classType = classType;
	}

	public void handleMessage(String message){
		log.info("Recibido mensaje {}", message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JavaType javaType = mapper.getTypeFactory().constructParametricType(TransactionElement.class, classType);
			TransactionElement<T> transaction = mapper.readValue(message, javaType);
			if (transaction.getStatus().equals(TransactionStatus.RECEIVED_OK)){
				log.info("La transaccion ha terminado correctamente, hacemos el commit");
				commit((T)transaction.getObjectTransmited());
			} else { // Cualquier otro estado, preferiblemente RECEIVED_NOK
				log.info("La transaccion no ha terminado correctamente, hacemos el rollback");
				rollback((T)transaction.getObjectTransmited());
			}
		} catch (Exception ex){
			log.error("Error en la recepción de la respuesta del destinatario por parte del emisor: ", ex);
		}
	}
	
	public abstract void commit(T requestObject);
	public abstract void rollback(T requestObject);
}
