package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import com.example.akka.directdebit.payment.fileimport.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setup
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class PaymentSetup implements ServiceSetup {

    private final MySettings mySettings;
    private final ImportFileProcessor importFileProcessor;
    public PaymentSetup(Config config,
                        HttpClientProvider httpClientProvider,
                        ComponentClient componentClient,
                        Materializer materializer
    ) {
        System.out.println("kafka config:"+System.getProperty("kalix.proxy.eventing.kafka.bootstrap-servers"));
        this.mySettings = new MySettings(config);
        this.importFileProcessor = new ImportFileProcessor(new ImportProcessFlowImpl(httpClientProvider, componentClient, materializer), new S3FileLoaderImpl(mySettings), mySettings, materializer);
//        this.importFileProcessor = new ImportFileProcessor(new ImportProcessFlowImpl(httpClientProvider, componentClient, materializer), new FilesystemFileLoaderImpl(materializer), mySettings, materializer);
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new MyDependencyProvider(mySettings,null);
    }

}
