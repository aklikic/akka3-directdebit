package com.example.akka.directdebit.importer;

import com.example.akka.directdebit.payment.api.PaymentClient;
import com.example.akka.directdebit.payment.api.PaymentCommand;
import com.example.akka.directdebit.payment.api.PaymentCommandResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public record MockPaymentClient() implements PaymentClient {
    @Override
    public CompletionStage<PaymentCommandResponse.Ack> create(String paymentId, PaymentCommand.Create command) {
        return CompletableFuture.completedFuture(PaymentCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<PaymentCommandResponse.Ack> initialize(String paymentId) {
        return CompletableFuture.completedFuture(PaymentCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<PaymentCommandResponse.Ack> setCredited(String paymentId) {
        return CompletableFuture.completedFuture(PaymentCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<PaymentCommandResponse.Ack> setCreditFailed(String paymentId, PaymentCommand.SetCreditFailed command) {
        return CompletableFuture.completedFuture(PaymentCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<PaymentCommandResponse.GetPaymentStateReply> getPaymentState(String paymentId) {
        return null;
    }
}
