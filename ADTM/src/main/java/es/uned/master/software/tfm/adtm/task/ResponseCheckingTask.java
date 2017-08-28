package es.uned.master.software.tfm.adtm.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.DistributedTransactionService;

/**
 * Tarea ejecutada como un thread independiente para comprobar si se ha recibido respuesta
 * para una transaccion determinada pasado el limite maximo establecido por esta
 * 
 * @author Francisco Cilleruelo
 */
public class ResponseCheckingTask implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ResponseCheckingTask.class);
	
	private DistributedTransactionService distributedTransactionService;
	private TransactionData transactionData;

	public ResponseCheckingTask(DistributedTransactionService transactionDataService, TransactionData transactionData) {
		super();
		this.distributedTransactionService = transactionDataService;
		this.transactionData = transactionData;
	}

	@Override
	public void run() {
		TransactionData currentTransaction = distributedTransactionService.getTransactionDataById(transactionData.getTransactionDataId());
		if (currentTransaction!=null && currentTransaction.getSentDate()!=null &&
				currentTransaction.getReceivedDate()==null){ // Se ha enviado la peticion, pero no se ha recibido respuesta
			log.info("No se ha recibido respuesta para la transaccion {} pasado el tiempo limite establecido de {} msg",
					transactionData.getTransactionDataId(), transactionData.getMaxResponseTime());
			distributedTransactionService.transactionResponseNotReceived(currentTransaction);
		}
	}

}
