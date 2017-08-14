package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

public class TransactionElement implements Serializable {

	private static final long serialVersionUID = 2432265495175652943L;
	
	private Long transactionReference;
	private String additionalInfo;
	private Object objectTransmited;
	private TransactionStatus status;
	
	private TransactionElement() {
		super();
	}
	
	public TransactionElement(TransactionData transactionData) {
		super();
		this.transactionReference = transactionData.getTransactionDataId();
		this.additionalInfo = transactionData.getAdditionalInfo();
		this.objectTransmited = transactionData.getObjectTransmited();
		this.status = TransactionStatus.valueOf(transactionData.getStatus());
	}

	public TransactionElement(Long transactionReference, String additionalInfo, Object objectTransmited,
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
	
	public Object getObjectTransmited() {
		return objectTransmited;
	}
	
	public void setObjectTransmited(Object objectTransmited) {
		this.objectTransmited = objectTransmited;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

}
