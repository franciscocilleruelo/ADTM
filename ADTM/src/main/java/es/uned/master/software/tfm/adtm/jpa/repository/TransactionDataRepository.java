package es.uned.master.software.tfm.adtm.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.uned.master.software.tfm.adtm.jpa.entity.TransactionData;

/**
 * Repositorio de transacciones
 * 
 * @author Francisco Cilleruelo
 */
@Repository
public interface TransactionDataRepository extends JpaRepository<TransactionData, Long> {
	
	/**
	 * @return Lista de transacciones que todavia no se han enviado
	 */
	@Query("SELECT TD FROM TransactionData TD WHERE sentDate IS NULL and responseCheckedDate IS NULL")
	public List<TransactionData> getTransactionToBeSent();

}
