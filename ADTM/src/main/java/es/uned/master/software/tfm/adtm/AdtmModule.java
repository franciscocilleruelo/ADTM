package es.uned.master.software.tfm.adtm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AdtmModule {
	
	private static final Logger log = LoggerFactory.getLogger(AdtmModule.class);
	
	public static void main(String[] args) {
		log.info("Arrancado ADTM");
		SpringApplication.run(AdtmModule.class, args);
	}

	@Bean
	public ThreadPoolTaskScheduler taskEcheduler(){
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
	
	
}
