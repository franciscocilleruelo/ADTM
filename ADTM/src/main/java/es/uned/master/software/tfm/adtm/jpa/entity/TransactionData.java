package es.uned.master.software.tfm.adtm.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import es.uned.master.software.tfm.adtm.entity.Transaction;

/**
 * Objeto perisistido en BD que representa la transaccion
 * 
 * @author Francisco Cilleruelo
 */
@Entity
@Table(name = "TRANSACTIONS_DATA")
public class TransactionData implements Serializable{
	
	private static final long serialVersionUID = 8238622515676354812L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long transactionDataId;
	/**
	 * Objeto de negocio transmitido en la transaccion
	 */
	@Lob
	private Serializable objectTransmited;	
	/**
	 * Fecha de comienzo de la transaccion
	 * Cuando el emisor la comienza, no cuando se envia a la cola del receptor de la transaccion
	 */
	private Date startDate;
	/**
	 * Fecha de envio a la cola del receptor de la transaccion
	 */
	private Date sentDate;
	/**
	 * Fecha en la que el emisor se recibe la respuesta por parte del receptor
	 */
	private Date receivedDate;
	/**
	 * Fecha en la que se comprueba si se ha recibido una respuesta por parte del receptor
	 * de acuerdo con el valor establecido en la propiedad maxResponseTime
	 */
	private Date responseCheckedDate;
	/**
	 * Estado de la transacción (TO_BE_SENT, SENT, RECEIVED_OK, RECEIVED_NOK, NOT_RECEIVED)
	 */
	private String status;
	/**
	 * Informacion adicional
	 */
	private String additionalInfo;
	/**
	 * Nombre de la cola donde el emisor envia la transaccion
	 */
	private String requestQueueName;
	/**
	 * Nombre de la cola donde se espera recibir la respuesta del receptor de la transaccion
	 */
	private String responseQueueName;
	/**
	 * Numero de intentos de envio
	 */
	private int sentTries;
	/**
	 * Tiempo maximo de respuesta permitido
	 * Superado este tiempo si no se ha recibido respuesta, la transaccion se dara por invalida
	 * Un valor menor o igual que cero indicara que no hay limite para recibir la respuesta 
	 */
	private int maxResponseTime;
	
	public TransactionData() {
		super();
	}
	
	public TransactionData(Transaction transaction) {
		super();
		this.objectTransmited = transaction.getObjectTransmited();
		this.requestQueueName = transaction.getRequestQueueName();
		this.responseQueueName = transaction.getResponseQueueName();
		this.maxResponseTime = transaction.getMaxResponseTime();
	}

	public Long getTransactionDataId() {
		return transactionDataId;
	}

	public void setTransactionDataId(Long transactionDataId) {
		this.transactionDataId = transactionDataId;
	}

	public Serializable getObjectTransmited() {
		return objectTransmited;
	}

	public void setObjectTransmited(Serializable objectTransmited) {
		this.objectTransmited = objectTransmited;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Date getResponseCheckedDate() {
		return responseCheckedDate;
	}

	public void setResponseCheckedDate(Date responseCheckedDate) {
		this.responseCheckedDate = responseCheckedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public int getSentTries() {
		return sentTries;
	}

	public void setSentTries(int sentTries) {
		this.sentTries = sentTries;
	}

	public int getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(int maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}
	
}
