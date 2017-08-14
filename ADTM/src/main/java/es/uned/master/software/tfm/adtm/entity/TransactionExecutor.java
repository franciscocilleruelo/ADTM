package es.uned.master.software.tfm.adtm.entity;

public interface TransactionExecutor {
	
	public void commit();
	public void rollback();

}
