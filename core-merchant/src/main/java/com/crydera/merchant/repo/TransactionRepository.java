package com.crydera.merchant.repo;

import com.crydera.merchant.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWalletIdIn(List<UUID> walletIds);
    List<Transaction> findByWalletIdInAndType(List<UUID> walletIds, Transaction.Type type);
}
