package es;


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

import es.uned.master.software.tfm.adtm.manager.DistributedTransactionManager;
import es.uned.master.software.tfm.adtm.repository.SenderConsumerRepository;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages={"es"})
@EnableJpaRepositories(basePackages={"es"})
@EntityScan(basePackages={"es"})
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
	
	@Bean
	public SenderConsumerRepository buildSenderConsumerRepository(){
		return new SenderConsumerRepository();
	}
	
}
