package com.example.directdebit;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.directdebit.payment.PaymentApi;
import com.example.directdebit.payment.PaymentDomain;
import com.example.directdebit.payment.PaymentService;
import com.example.directdebit.transaction.*;
import com.google.protobuf.Empty;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class SystemIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final TransactionService transactionServiceClient;
  private final PaymentService paymentServiceClient;
  private final TransactionByPaymentAndStatusView viewClient;

  public SystemIntegrationTest() {
    paymentServiceClient = testKit.getGrpcClient(PaymentService.class);
    transactionServiceClient = testKit.getGrpcClient(TransactionService.class);
    viewClient = testKit.getGrpcClient(TransactionByPaymentAndStatusView.class);
  }

  @Test
  public void test() throws Exception {
    var numOfPayment = 1;
    var numOfTransactions = 1;
    var paymentsProcessed =
            mockFileLoad(numOfPayment,numOfTransactions,10)
            .via(paymentProcessFlow(1,1))
            .toMat(Sink.seq(),Keep.right())
            .run(testKit.getMaterializer()).toCompletableFuture().get(10, TimeUnit.SECONDS);

    Thread.sleep(10000);
    var viewRes = paymentsProcessed.stream().map(payment -> {
      var cmd = TransactionByPaymentAndStatus.GetTransactionByPaymentAndStatusRequest.newBuilder()
              .setPaymentId(payment.paymentId())
              .setStatusId(TransactionDomain.TransactionStatus.DEBITED.getNumber())
              .build();
      return viewClient.getTransactionByPaymentAndStatus(cmd)
              .thenApply(res -> res.getRecordsCount() == numOfTransactions).toCompletableFuture();
    }).collect(Collectors.toList());
    var zip = Utils.allOf(viewRes);
    var list = zip.get(5,TimeUnit.SECONDS);
    System.out.println(list);
    list.stream().forEach(Assert::assertTrue);


  }

  private Source<Payment,?> mockFileLoad(int numOfPayment, int numOfTransactions, int tranDebitAmount){
    return
    Source.range(1,numOfPayment).map(paymentIndex -> {
      var paymentId = "p%s".formatted(paymentIndex);
      var trans = IntStream.range(1,numOfPayment+1).mapToObj(transIndex -> new Transaction("t%s#%s".formatted(transIndex,paymentId),tranDebitAmount)).collect(Collectors.toList());
      return new Payment(paymentId,tranDebitAmount*trans.size(),trans);
    });
  }

  private CompletionStage<Empty> processTransactions(int parallelismTransactions, Payment payment){
      return
      Source.from(payment.trans())
              .mapAsync(parallelismTransactions,  trans -> {
                  var transCreateCmd = TransactionApi.CreateCommand.newBuilder()
                          .setTransId(trans.transId())
                          .setPaymentId(payment.paymentId())
                          .setDebitAmount(trans.debitAmount())
                          .build();
                  return transactionServiceClient.create(transCreateCmd);
              })
              .toMat(Sink.head(), Keep.right())
              .run(testKit.getMaterializer());
  }
  private Flow<Payment, Payment, ?> paymentProcessFlow(int parallelismPayment, int parallelismTransactions){
    return Flow.<Payment>create()
            .mapAsync(parallelismPayment, payment -> {
                var ts = payment.trans.stream().map(trans -> PaymentDomain.Transaction.newBuilder().setTransId(trans.transId()).build()).collect(Collectors.toList());
                var paymentCreateCmd = PaymentApi.CreateCommand.newBuilder()
                        .setPaymentId(payment.paymentId())
                        .setCreditAmount(payment.creditAmount())
                        .addAllTransactions(ts)
                        .build();
                var paymentInitializeCmd = PaymentApi.InitializeCommand.newBuilder()
                        .setPaymentId(payment.paymentId())
                        .build();
                return paymentServiceClient.create(paymentCreateCmd)
                        .thenCompose(r -> processTransactions(parallelismTransactions,payment))
                        .thenCompose(r -> paymentServiceClient.initialize(paymentInitializeCmd))
                        .thenApply(r -> payment);
            });
  }

  record Transaction(String transId, int debitAmount){}
  record Payment(String paymentId, int creditAmount, List<Transaction> trans){}

}

