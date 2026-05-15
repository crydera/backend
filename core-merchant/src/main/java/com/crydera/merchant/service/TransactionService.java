package com.crydera.merchant.service;

import com.crydera.merchant.api.dto.TransactionDtos;
import com.crydera.merchant.domain.Transaction;
import com.crydera.merchant.domain.Wallet;
import com.crydera.merchant.repo.TransactionRepository;
import com.crydera.merchant.repo.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionDtos.TransactionListResponse list(UUID merchantId, Transaction.Type filter) {
        List<UUID> walletIds = walletRepository.findByMerchantId(merchantId).stream()
                .map(Wallet::getId)
                .toList();
        if (walletIds.isEmpty()) {
            return new TransactionDtos.TransactionListResponse(List.of());
        }
        List<Transaction> txs = filter == null
                ? transactionRepository.findByWalletIdIn(walletIds)
                : transactionRepository.findByWalletIdInAndType(walletIds, filter);

        List<TransactionDtos.TransactionResponse> mapped = txs.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(TransactionService::toDto)
                .toList();
        return new TransactionDtos.TransactionListResponse(mapped);
    }

    private static TransactionDtos.TransactionResponse toDto(Transaction t) {
        return new TransactionDtos.TransactionResponse(
                t.getId(),
                t.getWalletId(),
                t.getType(),
                t.getStatus(),
                t.getAmount(),
                t.getCurrency(),
                t.getTxHash(),
                t.getCounterparty(),
                t.getCreatedAt(),
                t.getConfirmedAt()
        );
    }
}
