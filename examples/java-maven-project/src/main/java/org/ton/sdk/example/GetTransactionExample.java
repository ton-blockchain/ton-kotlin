package org.ton.sdk.example;

import org.ton.sdk.blockchain.address.AddressStd;
import org.ton.sdk.blockchain.currency.Coins;
import org.ton.sdk.toncenter.client.TonCenterV3Client;
import org.ton.sdk.toncenter.model.TonCenterTransaction;
import org.ton.sdk.toncenter.model.TonCenterTransactionsRequestBuilder;
import org.ton.sdk.toncenter.model.TonCenterTransactionsResponse;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class GetTransactionExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TonCenterV3Client client = TonCenterV3Client.create();
        TonCenterTransactionsResponse response = client.transactionsAsync(
                new TonCenterTransactionsRequestBuilder()
                        .address(AddressStd.parse("UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH"))
                        .limit(15)
        ).get();
        for (TonCenterTransaction transaction : response.getTransactions()) {
            Coins balance = transaction.getAccountStateAfter().getBalance();

            BigInteger value = BigInteger.ZERO;
            if (balance != null) {
                value = balance.value();
            }
            System.out.println("hash="+transaction.getHash()+" lt="+transaction.getLt()+" balance="+value);
        }
    }
}
