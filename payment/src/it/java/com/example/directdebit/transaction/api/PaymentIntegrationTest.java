package com.example.directdebit.transaction.api;

import akka.javasdk.testkit.TestKitSupport;
import com.example.akka.directdebit.payment.api.PaymentClient;
import com.example.akka.directdebit.payment.api.PaymentCommand;
import com.example.akka.directdebit.payment.api.PaymentCommandError;
import com.example.akka.directdebit.payment.api.PaymentCommandResponse.ApiPaymentStatus;
import com.example.akka.directdebit.payment.application.PaymentEntity;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentIntegrationTest extends TestKitSupport {


    @Test
    public void happyPath() {
        var paymentId = UUID.randomUUID().toString();
        var creditAmount = 10;
        //create
        var createCmd = new PaymentCommand.Create(creditAmount);
        await(getClient().create(paymentId, createCmd));
        var reply = await(getClient().getPaymentState(paymentId));
        assertTrue(reply.error() == PaymentCommandError.NO_ERROR);
        assertEquals(paymentId, reply.state().get().paymentId());
        assertEquals(ApiPaymentStatus.CREATED, reply.state().get().status());

        //initialize
        await(getClient().initialize(paymentId));
        reply = await(getClient().getPaymentState(paymentId));
        assertEquals(ApiPaymentStatus.INITIALIZED, reply.state().get().status());

        //simulate start credit trigger
        await(componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::startCredit).invokeAsync());
        reply = await(getClient().getPaymentState(paymentId));
        assertEquals(ApiPaymentStatus.CREDIT_STARTED, reply.state().get().status());

        //credited
        //initialize
        await(getClient().setCredited(paymentId));
        reply = await(getClient().getPaymentState(paymentId));
        assertEquals(ApiPaymentStatus.CREDITED, reply.state().get().status());


    }
    @Test
    public void happyPathWithDebitSchedule() {
        var paymentId = UUID.randomUUID().toString();
        var creditAmount = 10;
        //create
        var createCmd = new PaymentCommand.Create(creditAmount);
        await(getClient().create(paymentId, createCmd));
        var reply = await(getClient().getPaymentState(paymentId));
        assertTrue(reply.error() == PaymentCommandError.NO_ERROR);
        assertEquals(paymentId, reply.state().get().paymentId());
        assertEquals(ApiPaymentStatus.CREATED, reply.state().get().status());

        //initialize
        await(getClient().initialize(paymentId));
        reply = await(getClient().getPaymentState(paymentId));
        assertEquals(ApiPaymentStatus.INITIALIZED, reply.state().get().status());


        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(1,TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() ->
                        await(getClient().getPaymentState(paymentId)).state().get().status() == ApiPaymentStatus.CREDIT_STARTED
                );

        //credited
        //initialize
        await(getClient().setCredited(paymentId));
        reply = await(getClient().getPaymentState(paymentId));
        assertEquals(ApiPaymentStatus.CREDITED, reply.state().get().status());
    }



    private PaymentClient transactionClient;
    private PaymentClient getClient(){
        if(transactionClient==null){
            transactionClient = new PaymentClient(httpClient);
        }
        return transactionClient;
    }
}
