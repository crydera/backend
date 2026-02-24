package com.crydera.merchant.service;

import com.crydera.merchant.api.dto.WalletDtos;
import com.crydera.merchant.blockchain.BlockchainGateway;
import com.crydera.merchant.domain.Transaction;
import com.crydera.merchant.domain.Wallet;
import com.crydera.merchant.repo.TransactionRepository;
import com.crydera.merchant.repo.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BlockchainGateway blockchainGateway;

    @Transactional
    public WalletDtos.WalletResponse create(UUID merchantId, WalletDtos.CreateWalletRequest req) {
        
        Wallet w = walletRepository.save(Wallet.builder()
                .merchantId(merchantId)
                .network(req.network())
                .address("pending:" + UUID.randomUUID())
                .label(req.label())
                .build());

        BlockchainGateway.DepositAddress addr =
                blockchainGateway.createDepositAddress(req.network(), merchantId, w.getId());
        w.setAddress(addr.address());
        w = walletRepository.save(w);

        return toDto(w, BigDecimal.ZERO, "USDT");
    }

    @Transactional
    public WalletDtos.WalletResponse rename(UUID merchantId, WalletDtos.RenameWalletRequest req) {
        Wallet w = walletRepository.findById(req.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        if (!w.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your wallet");
        }
        w.setLabel(req.label());
        walletRepository.save(w);
        return toDtoWithBalance(w);
    }

    public WalletDtos.WalletListResponse list(UUID merchantId) {
        List<WalletDtos.WalletResponse> wallets = walletRepository.findByMerchantId(merchantId).stream()
                .map(this::toDtoWithBalance)
                .toList();
        return new WalletDtos.WalletListResponse(wallets);
    }

    public WalletDtos.WalletDetailsResponse details(UUID merchantId, UUID walletId) {
        Wallet w = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        if (!w.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your wallet");
        }
        return new WalletDtos.WalletDetailsResponse(toDtoWithBalance(w), List.of());
    }

    private WalletDtos.WalletResponse toDtoWithBalance(Wallet w) {
        BigDecimal balance = BigDecimal.ZERO;
        String currency = "USDT";
        try {
            BlockchainGateway.Balance b = blockchainGateway.getBalance(w.getNetwork(), w.getAddress());
            balance = b.amount();
            currency = b.currency();
        } catch (Exception e) {
            log.warn("getBalance failed for wallet {} address {}: {} — returning 0",
                    w.getId(), w.getAddress(), e.getMessage());
        }
        return toDto(w, balance, currency);
    }

    @Transactional
    public WalletDtos.WithdrawalResponse withdraw(UUID merchantId, WalletDtos.WithdrawalRequest req) {
        Wallet w = walletRepository.findById(req.walletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found"));
        if (!w.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your wallet");
        }
        Transaction pending = transactionRepository.save(Transaction.builder()
                .id(UUID.randomUUID())
                .walletId(w.getId())
                .type(Transaction.Type.WITHDRAWAL)
                .status(Transaction.Status.PENDING)
                .amount(req.amount())
                .currency("USDT")
                .counterparty(req.toAddress())
                .build());

        BlockchainGateway.WithdrawalReceipt receipt = blockchainGateway.initiateWithdrawal(
                w.getNetwork(), w.getId(), req.toAddress(), req.amount(), pending.getId().toString());
        pending.setTxHash(receipt.txHash());
        transactionRepository.save(pending);

        return new WalletDtos.WithdrawalResponse(pending.getId(), receipt.status());
    }

    private static WalletDtos.WalletResponse toDto(Wallet w, BigDecimal balance, String currency) {
        return new WalletDtos.WalletResponse(
                w.getId(), w.getNetwork(), w.getAddress(), w.getLabel(),
                balance, currency, w.getCreatedAt()
        );
    }
}
