package com.example.directdebit.transaction.api;

import akka.javasdk.testkit.TestKitSupport;
import com.example.akka.directdebit.payment.api.TransactionByPaymentAndStatusViewModel;
import com.example.akka.directdebit.payment.api.TransactionClient;
import com.example.akka.directdebit.payment.api.TransactionCommand;
import com.example.akka.directdebit.payment.api.TransactionCommandError;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.ApiTransactionStatus;
import com.example.akka.directdebit.payment.application.TransactionEntity;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionIntegrationTest extends TestKitSupport {


    @Test
    public void happyPath() {
        var paymentId = UUID.randomUUID().toString();
        var transId = "%s-%s".formatted(UUID.randomUUID().toString(),paymentId);
        var debitAmount = 10;
        //create
        var createCmd = new TransactionCommand.Create(paymentId, debitAmount);
        await(getClient().create(transId, createCmd));
        var reply = await(getClient().getTransactionState(transId));
        assertTrue(reply.error() == TransactionCommandError.NO_ERROR);
        assertEquals(transId, reply.state().get().transId());
        assertEquals(ApiTransactionStatus.CREATED, reply.state().get().status());

        //initialize
        await(getClient().initialize(transId));
        reply = await(getClient().getTransactionState(transId));
        assertEquals(ApiTransactionStatus.INITIALIZED, reply.state().get().status());

        //simulate start credit trigger
        await(componentClient.forEventSourcedEntity(transId).method(TransactionEntity::startDebit).invokeAsync());
        reply = await(getClient().getTransactionState(transId));
        assertEquals(ApiTransactionStatus.DEBIT_STARTED, reply.state().get().status());

        //credited
        //initialize
        await(getClient().setDebited(transId));
        reply = await(getClient().getTransactionState(transId));
        assertEquals(ApiTransactionStatus.DEBITED, reply.state().get().status());

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(1,TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        await(getClient().queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(paymentId, ApiTransactionStatus.DEBITED.name()))).records().size() == 1
                );


    }
    @Test
    public void happyPathWithDebitSchedule() {
        var paymentId = UUID.randomUUID().toString();
        var transId = "%s-%s".formatted(UUID.randomUUID().toString(),paymentId);
        var debitAmount = 10;
        //create
        var createCmd = new TransactionCommand.Create(paymentId, debitAmount);
        await(getClient().create(transId, createCmd));
        var reply = await(getClient().getTransactionState(transId));
        assertTrue(reply.error() == TransactionCommandError.NO_ERROR);
        assertEquals(transId, reply.state().get().transId());
        assertEquals(ApiTransactionStatus.CREATED, reply.state().get().status());

        //initialize
        await(getClient().initialize(transId));
        reply = await(getClient().getTransactionState(transId));
        assertEquals(ApiTransactionStatus.INITIALIZED, reply.state().get().status());


        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(1,TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        await(getClient().getTransactionState(transId)).state().get().status() == ApiTransactionStatus.DEBIT_STARTED
                );

        //credited
        //initialize
        await(getClient().setDebited(transId));
        reply = await(getClient().getTransactionState(transId));
        assertEquals(ApiTransactionStatus.DEBITED, reply.state().get().status());

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(1,TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        await(getClient().queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(paymentId, ApiTransactionStatus.DEBITED.name()))).records().size() == 1
                );


    }



    private TransactionClient transactionClient;
    private TransactionClient getClient(){
        if(transactionClient==null){
            transactionClient = new TransactionClient(httpClient);
        }
        return transactionClient;
    }
}
