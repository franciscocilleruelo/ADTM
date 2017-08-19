package es.uned.master.software.tfm.adtm.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.TransactionDataService;

public class ResponseCheckingTask implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ResponseCheckingTask.class);
	
	private TransactionDataService transactionDataService;
	private TransactionData transactionData;

	public ResponseCheckingTask(TransactionDataService transactionDataService, TransactionData transactionData) {
		super();
		this.transactionDataService = transactionDataService;
		this.transactionData = transactionData;
	}

	@Override
	public void run() {
		TransactionData currentTransaction = transactionDataService.getTransactionDataById(transactionData.getTransactionDataId());
		if (currentTransaction!=null && currentTransaction.getSentDate()!=null &&
				currentTransaction.getReceivedDate()==null){ // Se ha enviado la peticion, pero no se ha recibido respuesta
			log.info("No se ha recibido respuesta para la transaccion {} pasado el tiempo limite establecido de {} msg",
					transactionData.getTransactionDataId(), transactionData.getMaxResponseTime());
			transactionDataService.transactionResponseNotReceived(currentTransaction);
		}
	}

}
