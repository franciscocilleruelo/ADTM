package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;
import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

public class Transaction<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 6768349709404274424L;
	
	private Long transactionReference;
	private String additionalInfo;
	private T objectTransmited;
	private SenderConsumer<T> senderConsumer;
	private String requestQueueName;
	private String responseQueueName;
	private int maxResponseTime;
	private TransactionStatus status;
	
	private Transaction() {
		super();
	}
	
	public Transaction(TransactionData transactionData) {
		super();
		this.transactionReference = transactionData.getTransactionDataId();
		this.additionalInfo = transactionData.getAdditionalInfo();
		this.requestQueueName = transactionData.getRequestQueueName();
		this.responseQueueName = transactionData.getResponseQueueName();
		this.objectTransmited = (T)transactionData.getObjectTransmited();
		this.status = TransactionStatus.valueOf(transactionData.getStatus());
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

	public Long getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(Long transactionReference) {
		this.transactionReference = transactionReference;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
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

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

}
