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

@Component
public class AmpqUtil {
	
	private static final Logger log = LoggerFactory.getLogger(AmpqUtil.class);
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Autowired
	private RabbitAdmin rabbitAdmin;
	
	private List<String> queuesCreated = new ArrayList<>();
	
	public void createRabbitListener(String queueName, Object consumer){
		log.info("No se ha creado un listener para la cola {} donde se espera recibir la respuesta", queueName);
		if (!queuesCreated.contains(queueName)){
			log.info("Creamos la cola {}", queueName);
			Queue queue = new Queue(queueName, false, false, false);
			rabbitAdmin.declareQueue(queue);
		}
		log.info("Procedemos a crear el listener para la cola {} recien creada");
		MessageListenerAdapter adapter = new MessageListenerAdapter(consumer);
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setMessageListener(adapter);
		container.setQueueNames(queueName);
		log.info("Arrancamos el listener para la cola {}", queueName);
		container.start();
	}

}
