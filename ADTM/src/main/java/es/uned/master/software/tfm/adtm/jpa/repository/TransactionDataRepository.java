package es.uned.master.software.tfm.adtm.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

@Repository
public interface TransactionDataRepository extends JpaRepository<TransactionData, Long> {
	
	@Query("SELECT * FROM TRANSACTIONS_DATA WHERE sentDate IS NULL and responseCheckedDate IS NULL")
	public List<TransactionData> getTransactionToBeSent();

}
