package com.example.akka.directdebit.importer;

import akka.javasdk.http.StrictResponse;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.example.akka.directdebit.importer.api.ImportCommandResponse;
import com.example.akka.directdebit.importer.api.ImportMessage;
import com.example.akka.directdebit.payment.api.HttpTransactionClient;
import com.example.akka.directdebit.payment.api.TransactionByPaymentAndStatusViewModel;
import com.example.akka.directdebit.payment.api.TransactionClient;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class IntegrationTest extends TestKitSupport {

    private TransactionClient transactionClient;
    private EventingTestKit.IncomingMessages inputTopic;
    @Override
    public void beforeAll() {
        super.beforeAll();
        transactionClient = new HttpTransactionClient(testKit.getHttpClientProvider());
        inputTopic = testKit.getTopicIncomingMessages(ImportMessage.IMPORT_TOPIC_NAME);
    }

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT.withTopicIncomingMessages(ImportMessage.IMPORT_TOPIC_NAME);
    }


    @Test
    public void messageBrokerBasedImportTest() throws Exception{

        var fileName = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var s3Bucket = "akka3-direct-debit";
        var paymentId = "p1-4cb2a2dd-0a23-45dc-9cc5-f181d0e65659";

        inputTopic.publish(new ImportMessage.FileToImport(fileName, s3Bucket).serialize());

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(12,TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> await(transactionClient.queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(paymentId, TransactionCommandResponse.ApiTransactionStatus.DEBIT_STARTED.name()))).records().size() == 1);

    }

    @Test
    public void endpointBasedImportTest() throws Exception{

        var fileName = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var s3Bucket = "akka3-direct-debit";
        var paymentId = "p1-4cb2a2dd-0a23-45dc-9cc5-f181d0e65659";

        await(importFile(new ImportMessage.FileToImport(fileName,s3Bucket)));

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(12,TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> await(transactionClient.queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(paymentId, TransactionCommandResponse.ApiTransactionStatus.DEBIT_STARTED.name()))).records().size() == 1);

    }

    private CompletionStage<ImportCommandResponse.Ack> importFile(ImportMessage.FileToImport message){
        return testKit.getSelfHttpClient()
                .POST("/importer/import")
                .withRequestBody(message)
                .responseBodyAs(ImportCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

}
