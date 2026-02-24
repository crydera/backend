package com.crydera.merchant.blockchain;

import com.crydera.merchant.domain.Network;

import java.math.BigDecimal;
import java.util.UUID;

public interface BlockchainGateway {

    DepositAddress createDepositAddress(Network network, UUID merchantId, UUID walletId);

    WithdrawalReceipt initiateWithdrawal(Network network, UUID fromWalletId, String toAddress,
                                         BigDecimal amount, String idempotencyKey);

    Balance getBalance(Network network, String address);

    record DepositAddress(Network network, String address) {}
    record WithdrawalReceipt(String txHash, String status) {}
    record Balance(BigDecimal amount, String currency) {}
}
