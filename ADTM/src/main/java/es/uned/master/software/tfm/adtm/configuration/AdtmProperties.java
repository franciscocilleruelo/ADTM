package es.uned.master.software.tfm.adtm.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Componente de propiedades para el gestor de transacciones ADTM
 * 
 * @author Francisco Cilleruelo
 */
@Component
@ConfigurationProperties(prefix="adtm")
public class AdtmProperties {
	
	/**
	 * Frecuencia de escaneo de transacciones no enviadas para ser enviadas a su cola correspondiente
	 */
	private long pollingfrecuency;

	public long getPollingfrecuency() {
		return pollingfrecuency;
	}

	public void setPollingfrecuency(long pollingfrecuency) {
		this.pollingfrecuency = pollingfrecuency;
	}

}
