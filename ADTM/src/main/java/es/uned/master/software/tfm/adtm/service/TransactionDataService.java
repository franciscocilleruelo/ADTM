package es.uned.master.software.tfm.adtm.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uned.master.software.tfm.adtm.amqp.Producer;
import es.uned.master.software.tfm.adtm.entity.Transaction;
import es.uned.master.software.tfm.adtm.entity.TransactionElement;
import es.uned.master.software.tfm.adtm.entity.TransactionStatus;
import es.uned.master.software.tfm.adtm.exception.SendingException;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.jpa.repository.TransactionDataRepository;

@Service
@Transactional
public class TransactionDataService {
	
	private static final Logger log = LoggerFactory.getLogger(TransactionDataService.class);
	
	@Autowired
	private TransactionDataRepository transactionDataRepository;
	
	@Autowired
	private Producer producer;
	
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
		return transactionDataRepository.save(transactionData);
	}
	
	public List<TransactionData> getTransactionsToBeSent(){
		log.info("Se obtienen las transacciones pendientes de ser enviadas");
		return transactionDataRepository.getTransactionToBeSent();
	}
	
	public TransactionData getTransactionDataById(Long transactionId){
		return transactionDataRepository.findOne(transactionId);
	}
	
	public void sentTransaction(TransactionData transactionData) throws SendingException{
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
		} catch (Exception ex){
			log.error("Ha habido un error en el envio de la transacción {} a su cola de envio {}", transactionData.getTransactionDataId(), 
					transactionData.getRequestQueueName());
			int sentTries = transactionData.getSentTries();
			transactionData.setSentDate(null);
			transactionData.setSentTries(sentTries+1);	
			log.info("Se guarda la transacción {} como NO enviada con un numero de intentos alcanzado de {}", transactionData.getTransactionDataId(),
					sentTries);
			transactionDataRepository.save(transactionData);
			throw new SendingException();
		}
		
	}

}
