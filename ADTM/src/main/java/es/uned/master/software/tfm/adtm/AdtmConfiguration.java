package es.uned.master.software.tfm.adtm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import es.uned.master.software.tfm.adtm.manager.TransactionManager;

@Configuration
@EnableAsync
@EnableScheduling
public class AdtmConfiguration {

	@Bean
	public ThreadPoolTaskScheduler taskEcheduler(){
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix("ADTM_ScheduledTasks-");
		taskScheduler.setPoolSize(60);
		return taskScheduler;
	}
	
	@Bean
	public TransactionManager getTransactionManager(){
		return new TransactionManager();
	}
	
}
