package com.crydera.merchant.blockchain;

import com.crydera.merchant.domain.Network;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public class MockBlockchainGateway implements BlockchainGateway {

    @Override
    public DepositAddress createDepositAddress(Network network, UUID merchantId, UUID walletId) {
        String fake = switch (network) {
            case TRON -> "T" + walletId.toString().replace("-", "").substring(0, 33);
            case TON -> "EQ" + walletId.toString().replace("-", "").substring(0, 46);
        };
        log.info("[mock] createDepositAddress network={} wallet={} -> {}", network, walletId, fake);
        return new DepositAddress(network, fake);
    }

    @Override
    public WithdrawalReceipt initiateWithdrawal(Network network, UUID fromWalletId, String toAddress,
                                                BigDecimal amount, String idempotencyKey) {
        String fakeHash = "0xmock-" + UUID.randomUUID();
        log.info("[mock] initiateWithdrawal network={} from={} to={} amount={} -> {}",
                network, fromWalletId, toAddress, amount, fakeHash);
        return new WithdrawalReceipt(fakeHash, "PENDING");
    }

    @Override
    public Balance getBalance(Network network, String address) {
        return new Balance(BigDecimal.ZERO, "USDT");
    }
}
