package es.uned.master.software.tfm.adtm.entity;

import java.io.Serializable;

public class TransactionExecutor<T1 extends Runnable & Serializable, T2 extends Runnable & Serializable> implements Serializable{
	
	private static final long serialVersionUID = 1147455160041880190L;
	
	private Long executorId;
	private Runnable threadCommit;
	private Runnable threadRollback;
	
	public TransactionExecutor(T1 threadCommit, T2 threadRollback){
		this.threadCommit = threadCommit;
		this.threadRollback = threadRollback;
	}

	public void commit(){
		threadCommit.run();
	}
	
	public void rollback(){
		threadRollback.run();
	}

	public Long getExecutorId() {
		return executorId;
	}

	public void setExecutorId(Long executorId) {
		this.executorId = executorId;
	}

}
