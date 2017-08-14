package es.uned.master.software.tfm.adtm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfiguration {

	@Bean
	public ThreadPoolTaskScheduler taskEcheduler(){
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix("ADTM_ScheduledTasks-");
		taskScheduler.setPoolSize(60);
		return taskScheduler;
	}
	
}
