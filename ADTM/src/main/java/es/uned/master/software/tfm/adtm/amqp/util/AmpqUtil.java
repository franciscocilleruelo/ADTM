package es.uned.master.software.tfm.adtm.amqp.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.uned.master.software.tfm.adtm.amqp.receiver.ReceiverConsumer;
import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;

@Component
public class AmpqUtil {
	
	private static final Logger log = LoggerFactory.getLogger(AmpqUtil.class);
	
	private List<String> requestQueueNames = new ArrayList<>();
	private List<String> responseQueueNames = new ArrayList<>();
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Autowired
	private RabbitAdmin rabbitAdmin;
	
	@Autowired
	private SenderConsumer senderConsumer;
	
	public void createRabbitListenerForSender(String responseQueueName){
		if (!responseQueueNames.contains(responseQueueName)){
			createRabbitListener(responseQueueName, senderConsumer);
		}
	}
	
	public void createRabbitListenerForReceiver(String responseQueueName, ReceiverConsumer receiverConsumer){
		if (!requestQueueNames.contains(responseQueueName)){
			createRabbitListener(responseQueueName, receiverConsumer);
		}
	}
	
	private void createRabbitListener(String responseQueueName, Object consumer){
		log.info("No se ha creado un listener para la cola {} donde se espera recibir la respuesta", responseQueueName);
		log.info("Creamos la cola {}", responseQueueName);
		Queue queue = new Queue(responseQueueName, false, false, false);
		rabbitAdmin.declareQueue(queue);
		log.info("Procedemos a crear el listener para la cola {} recien creada");
		MessageListenerAdapter adapter = new MessageListenerAdapter(consumer);
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setMessageListener(adapter);
		container.setQueueNames(responseQueueName);
		log.info("Arrancamos el listener para la cola {}", responseQueueName);
		container.start();
	}

}
