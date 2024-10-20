package com.example.akka.directdebit.fileimport.process;

import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.fileimport.datamodel.Payment;
import com.example.akka.directdebit.payment.api.*;

import java.util.concurrent.CompletionStage;

public record FileProcessFlowImpl(TransactionClient transactionClient, PaymentClient paymentClient) implements FileProcessFlow {

    public FileProcessFlowImpl(HttpClientProvider httpClientProvider) {
        this(new HttpTransactionClient(httpClientProvider),new HttpPaymentClient(httpClientProvider));
    }
    public FileProcessFlowImpl(HttpClientProvider httpClientProvider, PaymentClient paymentClient) {
        this(new HttpTransactionClient(httpClientProvider),paymentClient);
    }

    private CompletionStage<TransactionCommandResponse.Ack> processCreateTransactions(int parallelismTransactions, Payment payment, Materializer materializer){
        return Source.from(payment.trans())
                .mapAsync(parallelismTransactions,  trans -> transactionClient.create(trans.transId(), new TransactionCommand.Create(payment.paymentId(),trans.debitAmount())))
                .toMat(Sink.head(), Keep.right())
                .run(materializer);
    }
    private CompletionStage<TransactionCommandResponse.Ack> processInitializeTransactions(int parallelismTransactions, Payment payment, Materializer materializer){
        return Source.from(payment.trans())
                .mapAsync(parallelismTransactions,  trans -> transactionClient.initialize(trans.transId()))
                .toMat(Sink.head(), Keep.right())
                .run(materializer);
    }
    @Override
    public Flow<Payment, Payment, ?> flow(int parallelismPayment, int parallelismTransactions, Materializer materializer){
        return Flow.<Payment>create()
                .mapAsync(parallelismPayment, payment ->
                        paymentClient.create(payment.paymentId(), new PaymentCommand.Create(payment.creditAmount()))
                                .thenCompose(r -> processCreateTransactions(parallelismTransactions,payment, materializer))
                                .thenCompose(r -> paymentClient.initialize(payment.paymentId()))
                                .thenCompose(r -> processInitializeTransactions(parallelismTransactions,payment, materializer))
                                .thenApply(r -> payment)
                );
    }


}
