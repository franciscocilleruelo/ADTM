package es.uned.master.software.tfm.adtm.task;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import es.uned.master.software.tfm.adtm.exception.SendingException;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;
import es.uned.master.software.tfm.adtm.service.TransactionDataService;

@Component
public class TransactionsScheduledPoller {
	
	private static final Logger log = LoggerFactory.getLogger(TransactionsScheduledPoller.class);
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private TransactionDataService transactionDataService;
	
	@Autowired
	private SimpleMessageListenerContainer listenerContainer;
	
	@Scheduled(fixedDelay=300000)
	public void run(){
		List<TransactionData> transactionDataList = transactionDataService.getTransactionsToBeSent();
		if (transactionDataList!=null && !transactionDataList.isEmpty()){
			for(TransactionData transactionData: transactionDataList){
				try {
					transactionDataService.sentTransaction(transactionData);
					// Establecemos el thread para evaluar si se ha recibido la respuesta 
					// pasado el tiempo limite establecido (en msg)
					ResponseCheckerTask checkerTask = new ResponseCheckerTask(transactionDataService, transactionData);
					taskScheduler.execute(checkerTask, transactionData.getMaxResponseTime());
					// Creamos el listener para la cola de respuesta
					String[] queueNames = listenerContainer.getQueueNames();
					List<String> queueNamesList = Arrays.asList(queueNames);
					if (!queueNamesList.contains(transactionData.getResponseQueueName())){
						listenerContainer.addQueueNames(transactionData.getResponseQueueName());
					}
				} catch (SendingException ex){
					log.error("Error en el envio de la transacción {}", transactionData.getTransactionDataId());
				}
				
			}
		}
	}

}
