package org.ton.sdk.example;

import org.ton.sdk.blockchain.address.AddressStd;
import org.ton.sdk.blockchain.currency.Coins;
import org.ton.sdk.toncenter.client.TonCenterV3Client;
import org.ton.sdk.toncenter.model.TonCenterTransaction;
import org.ton.sdk.toncenter.model.TonCenterTransactionsRequestBuilder;
import org.ton.sdk.toncenter.model.TonCenterTransactionsResponse;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GetTransactionExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // create TON Center v3 http client
        TonCenterV3Client client = TonCenterV3Client.create();

        // Get 10 last transactions for account
        Future<TonCenterTransactionsResponse> response = client.transactionsAsync(
                new TonCenterTransactionsRequestBuilder()
                        .address(AddressStd.parse("UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH"))
                        .limit(10)
        );

        // Print transactions info and balance after transaction
        for (TonCenterTransaction transaction : response.get().transactions()) {
            Coins balance = transaction.accountStateAfter().balance();

            BigInteger value = BigInteger.ZERO;
            if (balance != null) {
                value = balance.value();
            }
            System.out.println("hash="+transaction.hash()+" lt="+transaction.lt()+" balance="+value);
        }
    }
}
