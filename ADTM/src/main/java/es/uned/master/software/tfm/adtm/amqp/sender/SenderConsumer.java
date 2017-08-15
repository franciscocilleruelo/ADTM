package es.uned.master.software.tfm.adtm.amqp.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.uned.master.software.tfm.adtm.entity.Transaction;
import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.TransactionDataService;

@Component
public class SenderConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(SenderConsumer.class);
	
	@Autowired
	private TransactionDataService transactionDataService;
	
	public void handleMessage(String message){
		log.info("Recibido mensaje {}", message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			TransactionElement transactionElement = mapper.readValue(message, TransactionElement.class);
			TransactionData transactionData = transactionDataService.getTransactionDataById(transactionElement.getTransactionReference());
			if (transactionData!=null){
				Transaction transaction = new Transaction(transactionData);
				if (transaction.getStatus().equals(TransactionStatus.RECEIVED_OK)){
					log.info("La transaccion {} ha terminado correctamente, hacemos el commit", transaction.getTransactionReference());
					transaction.getExecutor().commit();
				} else { // Cualquier otro estado, preferiblemente RECEIVED_NOK
					log.info("La transaccion {} no ha terminado correctamente, hacemos el rollback", transaction.getTransactionReference());
					transaction.getExecutor().rollback();
				}
			}
		} catch (Exception ex){
			log.error("Error en la conversión del mensaje JSON {}", message);
		}
	}
}
