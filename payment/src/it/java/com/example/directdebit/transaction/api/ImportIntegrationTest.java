package com.example.directdebit.transaction.api;

import akka.javasdk.impl.http.HttpClientImpl;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.payment.MyDependencyProvider;
import com.example.akka.directdebit.payment.api.*;
import com.example.akka.directdebit.payment.stream.ImportFileUtil;
import com.example.akka.directdebit.payment.stream.ImportProcessFlow;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ImportIntegrationTest extends TestKitSupport {

    private PaymentClient paymentClient;
    private TransactionClient transactionClient;
    private MyDependencyProvider dependencyProvider;
    private EventingTestKit.IncomingMessages inputTopic;
    @Override
    public void beforeAll() {
        super.beforeAll();
        transactionClient = new TransactionClient(new HttpClientImpl(testKit.getActorSystem(),"http://localhost:9000"));
        inputTopic = testKit.getTopicIncomingMessages(ImportTopicPublicMessage.IMPORT_TOPIC_NAME);
    }

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT.withTopicIncomingMessages(ImportTopicPublicMessage.IMPORT_TOPIC_NAME);
    }


    @Test
    public void happyPath() throws Exception{

        var location = "src/it/resources/import-%s.txt".formatted(UUID.randomUUID().toString());
        var debitAmount = 10;
        var numberOfPayments = 1;
        var numberOfTransactions = 1;

        var paymentPrefix = UUID.randomUUID().toString();

        var generatedPaymentsSource = ImportFileUtil.generate(paymentPrefix, numberOfPayments,numberOfTransactions,debitAmount);
        await(ImportFileUtil.storeToFile(generatedPaymentsSource,location,testKit.getMaterializer()));

        var payments = await(generatedPaymentsSource.toMat(Sink.seq(), Keep.right()).run(testKit.getMaterializer()));
        inputTopic.publish(new ImportTopicPublicMessage.FileToImport(location).deSerialize());

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(12,TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> {
                    ImportProcessFlow.Payment payment = payments.get(0);
                    return await(transactionClient.queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(payment.paymentId(), TransactionCommandResponse.ApiTransactionStatus.DEBIT_STARTED.name()))).records().size() == payment.trans().size();
                });


    }

}
