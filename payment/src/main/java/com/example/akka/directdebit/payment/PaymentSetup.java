package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import com.example.akka.directdebit.fileimport.ImportFileProcessor;
import com.example.akka.directdebit.fileimport.ImportProcessFlowImpl;
import com.example.akka.directdebit.fileimport.S3FileLoaderImpl;
import com.typesafe.config.Config;

@Setup
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class PaymentSetup implements ServiceSetup {

    private final MySettings mySettings;
    private final ImportFileProcessor importFileProcessor;
    public PaymentSetup(Config config,
                        HttpClientProvider httpClientProvider,
                        ComponentClient componentClient,
//                        HttpClient httpClient,
                        Materializer materializer
    ) {
        System.out.println("kafka config:"+System.getProperty("kalix.proxy.eventing.kafka.bootstrap-servers"));
//        System.out.println("alpakka.s3.aws.region.default-region:"+config.getString("alpakka.s3.aws.region.default-region"));
//        System.out.println("alpakka.s3:"+config.getConfig("alpakka.s3"));
        this.mySettings = new MySettings(config);
        var fileLoader = new S3FileLoaderImpl(mySettings.s3BucketName, config);

//        var fileLoader = new FilesystemFileLoaderImpl(materializer);
        var paymentClient = new ComponentPaymentClient(componentClient);
        this.importFileProcessor = new ImportFileProcessor(new ImportProcessFlowImpl(httpClientProvider, paymentClient,  materializer), fileLoader, mySettings.importPaymentParallelism, mySettings.importTransactionParallelism, materializer);
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new MyDependencyProvider(mySettings,importFileProcessor);
    }

}
