package com.crydera.merchant.blockchain;

import com.crydera.merchant.domain.Network;
import com.crydera.proto.blockchain.v1.BlockchainServiceGrpc;
import com.crydera.proto.blockchain.v1.CreateDepositAddressRequest;
import com.crydera.proto.blockchain.v1.CreateDepositAddressResponse;
import com.crydera.proto.blockchain.v1.GetBalanceRequest;
import com.crydera.proto.blockchain.v1.GetBalanceResponse;
import com.crydera.proto.blockchain.v1.InitiateWithdrawalRequest;
import com.crydera.proto.blockchain.v1.InitiateWithdrawalResponse;
import com.crydera.proto.common.v1.Money;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Primary
@Component
public class GrpcBlockchainGateway implements BlockchainGateway {

    @GrpcClient("tron-sidecar")
    private BlockchainServiceGrpc.BlockchainServiceBlockingStub tronStub;

    private final MockBlockchainGateway fallback = new MockBlockchainGateway();

    @Override
    public DepositAddress createDepositAddress(Network network, UUID merchantId, UUID walletId) {
        try {
            CreateDepositAddressResponse res = stubFor(network)
                    .createDepositAddress(CreateDepositAddressRequest.newBuilder()
                            .setNetwork(toProtoNetwork(network))
                            .setMerchantId(merchantId.toString())
                            .setWalletId(walletId.toString())
                            .build());
            String addr = res.getAddress().getValue();
            log.info("[grpc] createDepositAddress {}/{} -> {}", network, walletId, addr);
            return new DepositAddress(network, addr);
        } catch (StatusRuntimeException e) {
            log.warn("[grpc] sidecar unavailable for {} createDepositAddress: {}. Falling back to mock.",
                    network, e.getStatus());
            return fallback.createDepositAddress(network, merchantId, walletId);
        }
    }

    @Override
    public WithdrawalReceipt initiateWithdrawal(Network network, UUID fromWalletId, String toAddress,
                                                BigDecimal amount, String idempotencyKey) {
        try {
            InitiateWithdrawalResponse res = stubFor(network)
                    .initiateWithdrawal(InitiateWithdrawalRequest.newBuilder()
                            .setNetwork(toProtoNetwork(network))
                            .setFromWalletId(fromWalletId.toString())
                            .setToAddress(toAddress)
                            .setAmount(Money.newBuilder()
                                    .setAmount(amount.toPlainString())
                                    .setCurrency("USDT")
                                    .build())
                            .setIdempotencyKey(idempotencyKey)
                            .build());
            return new WithdrawalReceipt(res.getTxHash(), res.getStatus());
        } catch (StatusRuntimeException e) {
            
            log.warn("[grpc] {} initiateWithdrawal failed: {}", network, e.getStatus());
            org.springframework.http.HttpStatus http =
                    (e.getStatus().getCode() == io.grpc.Status.Code.INVALID_ARGUMENT
                            || e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND
                            || e.getStatus().getCode() == io.grpc.Status.Code.FAILED_PRECONDITION)
                            ? org.springframework.http.HttpStatus.BAD_REQUEST
                            : org.springframework.http.HttpStatus.BAD_GATEWAY;
            throw new org.springframework.web.server.ResponseStatusException(
                    http,
                    "withdrawal failed: " + e.getStatus().getDescription());
        }
    }

    @Override
    public Balance getBalance(Network network, String address) {
        try {
            GetBalanceResponse res = stubFor(network)
                    .getBalance(GetBalanceRequest.newBuilder()
                            .setNetwork(toProtoNetwork(network))
                            .setAddress(address)
                            .build());
            return new Balance(new BigDecimal(res.getBalance().getAmount()), res.getBalance().getCurrency());
        } catch (StatusRuntimeException e) {
            log.warn("[grpc] sidecar unavailable for {} getBalance: {}. Returning zero.", network, e.getStatus());
            return new Balance(BigDecimal.ZERO, "USDT");
        }
    }

    private BlockchainServiceGrpc.BlockchainServiceBlockingStub stubFor(Network network) {
        return switch (network) {
            case TRON -> tronStub;
            case TON -> throw new UnsupportedOperationException("TON sidecar gRPC channel not configured yet");
        };
    }

    private static com.crydera.proto.common.v1.Network toProtoNetwork(Network n) {
        return switch (n) {
            case TRON -> com.crydera.proto.common.v1.Network.NETWORK_TRON;
            case TON -> com.crydera.proto.common.v1.Network.NETWORK_UNSPECIFIED;
        };
    }
}
