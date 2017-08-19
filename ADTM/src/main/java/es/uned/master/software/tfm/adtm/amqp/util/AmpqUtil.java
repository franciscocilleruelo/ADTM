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

/**
 * Componente de utilidades relativas al gestor de mensajes AMPQ
 * 
 * @author Francisco Cilleruelo
 */
@Component
public class AmpqUtil {
	
	private static final Logger log = LoggerFactory.getLogger(AmpqUtil.class);
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Autowired
	private RabbitAdmin rabbitAdmin;
	
	private List<String> queuesCreated = new ArrayList<>();
	
	/**
	 * Metodo para crear el listener para los mensajes recibidos por la cola indicada
	 * 
	 * @param queueName Nombre de la cola
	 * @param consumer Componente encargado de recibir y procesar los mensajes recibidos en esa cola
	 */
	public void createRabbitListener(String queueName, Object consumer){
		log.info("Comprobacion de que no se haya creado ya la cola con el nombre {}", queueName);
		if (!queuesCreated.contains(queueName)){
			log.info("La cola con el nombre {} no existia, procedemos a crearla", queueName);
			Queue queue = new Queue(queueName, false, false, false);
			rabbitAdmin.declareQueue(queue);
		}
		log.info("Procedemos a crear el listener para la cola {}", queueName);
		MessageListenerAdapter adapter = new MessageListenerAdapter(consumer);
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
		container.setMessageListener(adapter);
		container.setQueueNames(queueName);
		log.info("Arrancamos el listener para la cola {}", queueName);
		container.start();
	}

}
