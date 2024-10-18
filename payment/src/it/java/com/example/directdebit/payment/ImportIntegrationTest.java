package com.example.directdebit.payment;

import akka.actor.ActorSystem;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.akka.directdebit.fileimport.ImportFileUtil;
import com.example.akka.directdebit.fileimport.ImportProcessFlow;
import com.example.akka.directdebit.fileimport.S3FileLoaderImpl;
import com.example.akka.directdebit.payment.api.HttpTransactionClient;
import com.example.akka.directdebit.payment.api.ImportMessage;
import com.example.akka.directdebit.payment.api.TransactionByPaymentAndStatusViewModel;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse;
import com.typesafe.config.Config;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class ImportIntegrationTest extends TestKitSupport {

    private HttpTransactionClient transactionClient;
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
    public void happyPath() throws Exception{

//        var location = "s3://import-%s.txt".formatted(UUID.randomUUID().toString());
        var location = "s3://import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var paymentId = "p1-4cb2a2dd-0a23-45dc-9cc5-f181d0e65659";
        var debitAmount = 10;
        var numberOfPayments = 1;
        var numberOfTransactions = 1;

        var paymentPrefix = UUID.randomUUID().toString();

//        var generatedPaymentsSource = ImportFileUtil.generate(paymentPrefix, numberOfPayments,numberOfTransactions,debitAmount);
//        await(ImportFileUtil.storeToFile(generatedPaymentsSource,location,testKit.getMaterializer()));
//
//        var payments = await(generatedPaymentsSource.toMat(Sink.seq(), Keep.right()).run(testKit.getMaterializer()));
        inputTopic.publish(new ImportMessage.FileToImport(location).deSerialize());

        //query
        Awaitility.await()
                .ignoreExceptions()
                .pollDelay(12,TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> await(transactionClient.queryByPaymentAndStatus(new TransactionByPaymentAndStatusViewModel.QueryRequest(paymentId, TransactionCommandResponse.ApiTransactionStatus.DEBIT_STARTED.name()))).records().size() == 1);

//        Files.delete(Path.of(location));
    }

    @Test
    public void testS3FileImport() throws Exception{
        var location = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var s3bucket = "akka3-direct-debit";

        Materializer mat = testKit.getMaterializer();

        System.out.println("s3 config path: "+S3Settings.ConfigPath());


        var s3Settings = S3Settings.create(testKit.getActorSystem().settings().config().getConfig(S3Settings.ConfigPath()));
        System.out.println("s3: "+s3Settings.accessStyle());
        var s3Source = S3.getObject(s3bucket, location).withAttributes(S3Attributes.settings(s3Settings));
        var payments = s3Source.via(ImportFileUtil.parse()).toMat(Sink.seq(),Keep.right()).run(mat).toCompletableFuture().get(10,TimeUnit.SECONDS);
        System.out.println(payments.size());

    }

    @Test
    public void testS3FileImportMyActorSystem() throws Exception{
        var location = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var s3bucket = "akka3-direct-debit";

        ActorSystem as = ActorSystem.create("S3Test");
        Materializer mat = Materializer.matFromSystem(as);

        var s3Source = S3.getObject(s3bucket, location);
        var payments = s3Source.via(ImportFileUtil.parse()).toMat(Sink.seq(),Keep.right()).run(mat).toCompletableFuture().get(10,TimeUnit.SECONDS);
        System.out.println(payments.size());

    }
}
