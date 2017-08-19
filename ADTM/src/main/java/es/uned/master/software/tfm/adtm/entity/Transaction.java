package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

/**
 * Objeto que representa la transaccion para el emisor que desencadena la comunicacion
 * 
 * @author Francisco Cilleruelo
 *
 * @param <T> Tipo de objeto de negocio (serializable) enviado como parte de la transaccion
 */
public class Transaction<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 6768349709404274424L;
	
	/**
	 * Objeto enviado como parte de la transaccion
	 */
	private T objectTransmited;
	/**
	 * Componente encarga de recibir y procesar la respuesta recibida del receptor de la transaccion
	 */
	private SenderConsumer<T> senderConsumer;
	/**
	 * Nombre de la cola donde se enviara la transaccion
	 */
	private String requestQueueName;
	/**
	 * Nombre de la cola donde se espera recibir la respuesta del receptor de la transaccion
	 */
	private String responseQueueName;
	/**
	 * Tiempo maximo de respuesta permitido
	 * Superado este tiempo si no se ha recibido respuesta, la transaccion se dara por invalida
	 * Un valor menor o igual que cero indicara que no hay limite para recibir la respuesta
	 */
	private int maxResponseTime;
	
	private Transaction() {
		super();
	}
	
	public Transaction(TransactionData transactionData) {
		super();
		this.requestQueueName = transactionData.getRequestQueueName();
		this.responseQueueName = transactionData.getResponseQueueName();
		this.objectTransmited = (T)transactionData.getObjectTransmited();
	}

	public Transaction(T objectTransmited, SenderConsumer<T> senderConsumer, String requestQueueName, String responseQueueName,
			int maxResponseTime) {
		super();
		this.objectTransmited = objectTransmited;
		this.senderConsumer = senderConsumer;
		this.requestQueueName = requestQueueName;
		this.responseQueueName = responseQueueName;
		this.maxResponseTime = maxResponseTime;
	}

	public T getObjectTransmited() {
		return objectTransmited;
	}

	public void setObjectTransmited(T objectTransmited) {
		this.objectTransmited = objectTransmited;
	}

	public SenderConsumer<T> getSenderConsumer() {
		return senderConsumer;
	}

	public void setSenderConsumer(SenderConsumer<T> senderConsumer) {
		this.senderConsumer = senderConsumer;
	}

	public String getRequestQueueName() {
		return requestQueueName;
	}

	public void setRequestQueueName(String requestQueueName) {
		this.requestQueueName = requestQueueName;
	}

	public String getResponseQueueName() {
		return responseQueueName;
	}

	public void setResponseQueueName(String responseQueueName) {
		this.responseQueueName = responseQueueName;
	}

	public int getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(int maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

}
