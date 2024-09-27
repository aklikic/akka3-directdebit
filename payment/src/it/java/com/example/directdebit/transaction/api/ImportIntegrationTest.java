package com.example.directdebit.transaction.api;

import akka.javasdk.http.HttpClient;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.payment.api.*;
import com.example.akka.directdebit.payment.misc.FileLoaderImpl;
import com.example.akka.directdebit.payment.misc.ImportFileUtil;
import com.example.akka.directdebit.payment.misc.ImportProcessFlow;
import com.example.akka.directdebit.payment.misc.ImportProcessFlowImpl;
import com.example.akka.directdebit.payment.MyDependencyProvider;
import com.example.akka.directdebit.payment.MySettings;
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
        paymentClient = new PaymentClient(new HttpClient(testKit.getActorSystem(),"http://localhost:9001"));
        transactionClient = new TransactionClient(new HttpClient(testKit.getActorSystem(),"http://localhost:9000"));
        var processFlow = new ImportProcessFlowImpl(transactionClient,paymentClient,testKit.getMaterializer());
        var fileLoader = new FileLoaderImpl(testKit.getMaterializer());
        dependencyProvider = new MyDependencyProvider(new MySettings(testKit.getActorSystem().settings().config()),processFlow,fileLoader);
        inputTopic = testKit.getTopicIncomingMessages(ImportTopicPublicMessage.IMPORT_TOPIC_NAME);
    }

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT.withTopicIncomingMessages(ImportTopicPublicMessage.IMPORT_TOPIC_NAME);
    }

    @Override
    public <T> T getDependency(Class<T> aClass) {
        return dependencyProvider.getDependency(aClass);
    }

    @Test
    public void happyPath() throws Exception{

        var location = "payment/src/it/resources/import-%s.txt".formatted(UUID.randomUUID().toString());
        var debitAmount = 10;
        var numberOfPayments = 1;
        var numberOfTransactions = 1;

        var paymentPrefix = UUID.randomUUID().toString();

        var generatedPaymentsSource = ImportFileUtil.generate(paymentPrefix, numberOfPayments,numberOfTransactions,debitAmount);
        await(ImportFileUtil.storeToFile(generatedPaymentsSource,location,testKit.getMaterializer()));

//        List<ImportProcessFlow.Payment> payments =
//                await(
////                        importFileUtil.mockFileLoad(UUID.randomUUID().toString(),numOfPayment,numOfTransactions,transDebit)
//                        processFlow.loadFromFile("src/it/resources/import-ff42129a-3a45-475f-a9cf-f5fe9bf0f106.txt")
//                        .via(processFlow.flow(parallelismPayment,parallelismTransactions))
//                        .toMat(Sink.seq(),Keep.right())
//                        .run(testKit.getMaterializer()));


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
