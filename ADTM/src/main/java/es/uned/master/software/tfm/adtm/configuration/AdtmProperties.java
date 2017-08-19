package es.uned.master.software.tfm.adtm.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="adtm")
public class AdtmProperties {
	
	private long pollingfrecuency;

	public long getPollingfrecuency() {
		return pollingfrecuency;
	}

	public void setPollingfrecuency(long pollingfrecuency) {
		this.pollingfrecuency = pollingfrecuency;
	}

}
