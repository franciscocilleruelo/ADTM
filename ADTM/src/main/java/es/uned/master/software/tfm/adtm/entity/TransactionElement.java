package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

/**
 * Objeto compartido entre emisor y receptor como elemento transmitido en la transaccion
 * 
 * @author Francisco Cilleruelo
 *
 * @param <T> Tipo de objeto de negocio (serializable) enviado como parte de la transaccion
 */
public class TransactionElement<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 2432265495175652943L;
	
	/**
	 * Identificador unico de la transaccion
	 */
	private Long transactionReference;
	/**
	 * Informacion adicional por parte del emisor o del receptor hacia la otra parte
	 */
	private String additionalInfo;
	/**
	 * Objeto de negocio transmitido
	 */
	private T objectTransmited;
	/**
	 * Estado de la transaccion (TO_BE_SENT, SENT, RECEIVED_OK, RECEIVED_NOK, NOT_RECEIVED) 
	 */
	private TransactionStatus status;
	/**
	 * Nombre de la cola donde el emisor espera recibir la respuesta del receptor
	 */
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
