package com.example.akka.directdebit.importer;

import akka.javasdk.http.HttpClient;
import akka.javasdk.testkit.TestKitSupport;
import akka.stream.javadsl.*;
import com.example.akka.directdebit.payment.api.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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
    public void happyPath() throws Exception{
        var transDebit = 10;
        var numOfPayment = 1;
        var numOfTransactions = 1;
        var parallelismTransactions = 1;
        var parallelismPayment = 1;
        List<Payment> payments =
                await(
//                        mockFileLoad(numOfPayment,numOfTransactions,transDebit)
                        ImportFileUtil.loadFromFile("importer/src/it/resources/import-ff42129a-3a45-475f-a9cf-f5fe9bf0f106.txt", testKit.getMaterializer())
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

//    private Source<Payment,?> loadFromFile(String location){
//        final Path file = Paths.get(location);
//        var lines = await(
//                FileIO.fromPath(file)
//                        .via(Framing.delimiter(ByteString.fromString("\n"),100,FramingTruncation.ALLOW))
//                        .map(bs -> bs.decodeString("utf8"))
//                        .toMat(Sink.seq(),Keep.right())
//                        .run(testKit.getMaterializer())
//        );
//
//        return Source.from(lines.stream().map(line -> {
//            var p = line.split("\\#");
//            var pd = p[0].split("\\/");
//
//            var ts = p[1].split(";");
//            var trans = Stream.of(ts).map(t -> {
//                var td = t.split("\\/");
//                return new Transaction(td[0],Integer.parseInt(td[1]));
//            }).collect(Collectors.toList());
//            return new Payment(pd[0],Integer.parseInt(pd[1]),trans);
//        }).collect(Collectors.toList()));
//
//    }

//    private Source<Payment,?> mockFileLoad(int numOfPayment, int numOfTransactions, int tranDebitAmount){
//        return Source.range(1,numOfPayment).map(paymentIndex -> {
//                    var paymentId = "p%s".formatted(paymentIndex);
//                    var trans = IntStream.range(1,numOfPayment+1).mapToObj(transIndex -> new Transaction("t%s#%s".formatted(transIndex,paymentId),tranDebitAmount)).collect(Collectors.toList());
//                    return new Payment(paymentId,tranDebitAmount*trans.size(),trans);
//                });
//    }

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

//    record Transaction(String transId, int debitAmount){}
//    record Payment(String paymentId, int creditAmount, List<Transaction> trans){}


}
