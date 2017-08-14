package es.uned.master.software.tfm.adtm.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.uned.master.software.tfm.adtm.entity.Transaction;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.TransactionDataService;

@Component
public class TransactionManager {
	
	@Autowired
	private TransactionDataService transactionDataService;
	
	public void runTransaction(Transaction transaction){
		TransactionData transactionData = new TransactionData(transaction);
		transactionDataService.startTransaction(transactionData);
	}

}
