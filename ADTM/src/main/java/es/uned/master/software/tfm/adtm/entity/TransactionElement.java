package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

public class TransactionElement<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 2432265495175652943L;
	
	private Long transactionReference;
	private String additionalInfo;
	private T objectTransmited;
	private TransactionStatus status;
	private String responseQueueName;
	
	public TransactionElement() {
		super();
	}
	
	public TransactionElement(TransactionData transactionData) {
		super();
		this.transactionReference = transactionData.getTransactionDataId();
		this.additionalInfo = transactionData.getAdditionalInfo();
		this.objectTransmited = (T)transactionData.getObjectTransmited();
		this.status = TransactionStatus.valueOf(transactionData.getStatus());
		this.responseQueueName = transactionData.getResponseQueueName();
	}

	public TransactionElement(Long transactionReference, String additionalInfo, T objectTransmited,
			TransactionStatus status) {
		super();
		this.transactionReference = transactionReference;
		this.additionalInfo = additionalInfo;
		this.objectTransmited = objectTransmited;
		this.setStatus(status);
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

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public String getResponseQueueName() {
		return responseQueueName;
	}

	public void setResponseQueueName(String responseQueueName) {
		this.responseQueueName = responseQueueName;
	}

}
