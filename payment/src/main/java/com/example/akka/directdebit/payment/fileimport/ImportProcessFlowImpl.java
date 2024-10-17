package com.example.akka.directdebit.payment.fileimport;

import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.payment.api.*;
import com.example.akka.directdebit.payment.application.PaymentEntity;

import java.util.concurrent.CompletionStage;

public record ImportProcessFlowImpl(TransactionClient transactionClient, ComponentClient componentClient, Materializer materializer) implements ImportProcessFlow{

    public ImportProcessFlowImpl(HttpClientProvider httpClientProvider, ComponentClient componentClient, Materializer materializer) {
        this(new TransactionClient(httpClientProvider),componentClient, materializer);
    }

    private CompletionStage<TransactionCommandResponse.Ack> processCreateTransactions(int parallelismTransactions, Payment payment){
        return Source.from(payment.trans())
                .mapAsync(parallelismTransactions,  trans -> transactionClient.create(trans.transId(), new TransactionCommand.Create(payment.paymentId(),trans.debitAmount())))
                .toMat(Sink.head(), Keep.right())
                .run(materializer);
    }
    private CompletionStage<TransactionCommandResponse.Ack> processInitializeTransactions(int parallelismTransactions, Payment payment){
        return Source.from(payment.trans())
                .mapAsync(parallelismTransactions,  trans -> transactionClient.initialize(trans.transId()))
                .toMat(Sink.head(), Keep.right())
                .run(materializer);
    }
    @Override
    public Flow<Payment, Payment, ?> flow(int parallelismPayment, int parallelismTransactions){
        return Flow.<Payment>create()
                .mapAsync(parallelismPayment, payment ->
                        componentClient.forEventSourcedEntity(payment.paymentId()).method(PaymentEntity::create).invokeAsync(new PaymentCommand.Create(payment.creditAmount()))
                                .thenCompose(r -> processCreateTransactions(parallelismTransactions,payment))
                                .thenCompose(r -> componentClient.forEventSourcedEntity(payment.paymentId()).method(PaymentEntity::initialize).invokeAsync())
                                .thenCompose(r -> processInitializeTransactions(parallelismTransactions,payment))
                                .thenApply(r -> payment)
                );
    }



}
