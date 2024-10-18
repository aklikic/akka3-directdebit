package com.example.akka.directdebit.fileimport;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.payment.api.*;

import java.util.concurrent.CompletionStage;

public record ImportProcessFlowImpl(HttpTransactionClient transactionClient, PaymentClient paymentClient, Materializer materializer) implements ImportProcessFlow {

    public ImportProcessFlowImpl(HttpClientProvider httpClientProvider, Materializer materializer) {
        this(new HttpTransactionClient(httpClientProvider),new HttpPaymentClient(httpClientProvider), materializer);
    }
    public ImportProcessFlowImpl(HttpClientProvider httpClientProvider, PaymentClient paymentClient, Materializer materializer) {
        this(new HttpTransactionClient(httpClientProvider),paymentClient, materializer);
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
                        paymentClient.create(payment.paymentId(), new PaymentCommand.Create(payment.creditAmount()))
                                .thenCompose(r -> processCreateTransactions(parallelismTransactions,payment))
                                .thenCompose(r -> paymentClient.initialize(payment.paymentId()))
                                .thenCompose(r -> processInitializeTransactions(parallelismTransactions,payment))
                                .thenApply(r -> payment)
                );
    }


}
