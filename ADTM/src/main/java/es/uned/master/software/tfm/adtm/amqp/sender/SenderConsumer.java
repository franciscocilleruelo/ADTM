package es.uned.master.software.tfm.adtm.amqp.sender;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;
import es.uned.master.software.tfm.adtm.service.DistributedTransactionService;

/**
 * Componente para recibir por parte del remitente la respuesta del receptor
 * 
 * @author Francisco Cilleruelo
 *
 * @param <T> Tipo de objeto de negocio (serializable) que espera recibir el emisor de la transaccion
 */
public abstract class SenderConsumer<T extends Serializable> implements Serializable {
	
	private static final long serialVersionUID = 8480348200882129004L;

	private static final Logger log = LoggerFactory.getLogger(SenderConsumer.class);
	
	private Class<T> classType;
	
	@Autowired
	private DistributedTransactionService distributedTransactionService;
	
	public SenderConsumer(Class<T> classType) {
		super();
		this.classType = classType;
	}

	/**
	 * Metodo encargado de recibir la transaccion como un String JSON
	 * 
	 * @param message Mensaje recibido
	 */
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
			distributedTransactionService.transactionResponseReceived(transaction);
		} catch (Exception ex){
			log.error("Error en la recepción de la respuesta del destinatario por parte del emisor: ", ex);
		}
	}
	
	/**
	 * Metodo implementado por el emisor para definir el proceso a realizar
	 * en el caso de que la transaccion haya terminado correctamente (TransactionStatus.RECEIVED_OK)
	 * 
	 * @param requestObject Objeto de negocio a procesar
	 */
	public abstract void commit(T requestObject);
	
	/**
	 * Metodo implementado por el emisor para definir el proceso a realizar
	 * en el caso de que la transaccion NO haya terminado correctamente 
	 * (TransactionStatus.RECEIVED_NOK o TransactionStatus.NOT_RECEIVED)
	 * 
	 * @param requestObject Objeto de negocio a procesar
	 */
	public abstract void rollback(T requestObject);
}
