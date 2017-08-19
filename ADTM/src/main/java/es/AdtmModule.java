package es;


import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;
import es.uned.master.software.tfm.adtm.manager.DistributedTransactionManager;

/**
 * Libreria de gestion de transacciones distribuidas asincronas
 * Asynchronous Distributed Transactions Manager (ADTM)
 * 
 * @author Francisco Cilleruelo
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan
@EnableJpaRepositories
@EntityScan
public class AdtmModule {

	@Bean
	public ThreadPoolTaskScheduler taskScheduler(){
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix("ADTM_ScheduledTasks-");
		taskScheduler.setPoolSize(60);
		return taskScheduler;
	}
	
	@Autowired
	private ConnectionFactory connectionFactory; 
	
	@Bean
	public RabbitAdmin getRabbitAdmin(){
		return new RabbitAdmin(connectionFactory);
	}
	
	@Bean
	public DistributedTransactionManager distributedTransactionManager(){
		return new DistributedTransactionManager();
	}
	
	/**
	 * @return Repositorio de listeners para los mensajes de respuesta recibidos de los receptores de las transacciones 
	 */
	@Bean
	public Map<Long, SenderConsumer> buildSenderConsumerRepository(){
		return new HashMap<>();
	}
	
}
