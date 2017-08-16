package es.uned.master.software.tfm.adtm.entity;

public class TransactionExecutor<T1 extends Runnable, T2 extends Runnable> {
	
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

}
