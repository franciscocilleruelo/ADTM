package es.uned.master.software.tfm.adtm.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import es.uned.master.software.tfm.adtm.amqp.sender.SenderConsumer;
import es.uned.master.software.tfm.adtm.entity.Transaction;

@Entity
@Table(name = "TRANSACTIONS_DATA")
public class TransactionData implements Serializable{
	
	private static final long serialVersionUID = 8238622515676354812L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long transactionDataId;
	@Lob
	private Serializable objectTransmited;	
	private Date startDate;
	private Date sentDate;
	private Date receivedDate;
	private Date responseCheckedDate;
	private String status;
	private String additionalInfo;
	private String requestQueueName;
	private String responseQueueName;
	private int sentTries;
	private int maxResponseTime;
	@Transient
	private SenderConsumer senderConsumer;
	
	public TransactionData() {
		super();
	}
	
	public TransactionData(Transaction transaction) {
		super();
		this.additionalInfo = transaction.getAdditionalInfo();
		this.objectTransmited = transaction.getObjectTransmited();
		this.senderConsumer = transaction.getSenderConsumer();
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

	public SenderConsumer getSenderConsumer() {
		return senderConsumer;
	}

	public void setSenderConsumer(SenderConsumer senderConsumer) {
		this.senderConsumer = senderConsumer;
	}
	
}
