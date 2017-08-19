package es.uned.master.software.tfm.adtm.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.uned.master.software.tfm.adtm.exception.SendingException;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.TransactionDataService;

@Component
public class TransactionsPollingScheduledTask {
	
	private static final Logger log = LoggerFactory.getLogger(TransactionsPollingScheduledTask.class);
	
	@Autowired
	private TransactionDataService transactionDataService;
	
	@Scheduled(fixedDelayString="${adtm.pollingfrecuency:60000}")
	public void run(){
		List<TransactionData> transactionDataList = transactionDataService.getTransactionsToBeSent();
		if (transactionDataList!=null && !transactionDataList.isEmpty()){
			for(TransactionData transactionData: transactionDataList){
				try {
					// Se envia la transacción
					transactionDataService.sendTransaction(transactionData);
				} catch (SendingException ex){
					log.error("Error en el envio de la transacción {}", transactionData.getTransactionDataId());
				}
			}
		}
	}

}
