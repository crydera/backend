package com.crydera.merchant.repo;

import com.crydera.merchant.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByMerchantId(UUID merchantId);
    Optional<Wallet> findFirstByMerchantIdAndAddress(UUID merchantId, String address);
}
