package com.example.akka.directdebit.importer;

import akka.Done;
import akka.javasdk.http.HttpClient;
import akka.javasdk.testkit.TestKitSupport;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.payment.api.*;
import org.awaitility.Awaitility;
import org.h2.mvstore.tx.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntegrationTest extends TestKitSupport {

    private PaymentClient paymentClient;
    private TransactionClient transactionClient;

    @Override
    public void beforeAll() {
        super.beforeAll();
        paymentClient = new PaymentClient(new HttpClient(testKit.getActorSystem(),"http://localhost:9001"));
        transactionClient = new TransactionClient(new HttpClient(testKit.getActorSystem(),"http://localhost:9000"));
    }

    @Test
    public void happyPath() {
        var transDebit = 10;
        var numOfPayment = 1;
        var numOfTransactions = 1;
        var parallelismTransactions = 1;
        var parallelismPayment = 1;
        List<Payment> payments =
                await(mockFileLoad(numOfPayment,numOfTransactions,transDebit)
                        .via(paymentProcessFlow(parallelismPayment,parallelismTransactions))
                        .toMat(Sink.seq(),Keep.right())
                        .run(testKit.getMaterializer()));


        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(10,TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> {
                    Payment payment = payments.get(0);
                    return await(transactionClient.queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(payment.paymentId(), TransactionCommandResponse.ApiTransactionStatus.DEBITED.name()))).records().size() == payment.trans.size();
                });

    }

    private Source<Payment,?> mockFileLoad(int numOfPayment, int numOfTransactions, int tranDebitAmount){
        return Source.range(1,numOfPayment).map(paymentIndex -> {
                    var paymentId = "p%s".formatted(paymentIndex);
                    var trans = IntStream.range(1,numOfPayment+1).mapToObj(transIndex -> new Transaction("t%s#%s".formatted(transIndex,paymentId),tranDebitAmount)).collect(Collectors.toList());
                    return new Payment(paymentId,tranDebitAmount*trans.size(),trans);
                });
    }

    private CompletionStage<TransactionCommandResponse.Ack> processCreateTransactions(int parallelismTransactions, Payment payment){
        return Source.from(payment.trans())
                        .mapAsync(parallelismTransactions,  trans -> transactionClient.create(trans.transId(), new TransactionCommand.Create(payment.paymentId(),trans.debitAmount())))
                        .toMat(Sink.head(), Keep.right())
                        .run(testKit.getMaterializer());
    }
    private CompletionStage<TransactionCommandResponse.Ack> processInitializeTransactions(int parallelismTransactions, Payment payment){
        return Source.from(payment.trans())
                        .mapAsync(parallelismTransactions,  trans -> transactionClient.initialize(trans.transId()))
                        .toMat(Sink.head(), Keep.right())
                        .run(testKit.getMaterializer());
    }
    private Flow<Payment, Payment, ?> paymentProcessFlow(int parallelismPayment, int parallelismTransactions){
        return Flow.<Payment>create()
                .mapAsync(parallelismPayment, payment ->
                   paymentClient.create(payment.paymentId(),new PaymentCommand.Create(payment.creditAmount()))
                            .thenCompose(r -> processCreateTransactions(parallelismTransactions,payment))
                            .thenCompose(r -> paymentClient.initialize(payment.paymentId()))
                            .thenCompose(r -> processInitializeTransactions(parallelismTransactions,payment))
                            .thenApply(r -> payment)
                );
    }

    record Transaction(String transId, int debitAmount){}
    record Payment(String paymentId, int creditAmount, List<Transaction> trans){}


}
