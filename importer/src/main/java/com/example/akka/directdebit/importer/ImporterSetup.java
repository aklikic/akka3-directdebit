package com.example.akka.directdebit.importer;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import com.example.akka.directdebit.importer.fileimport.FileLoaderImpl;
import com.example.akka.directdebit.importer.fileimport.ImportFileProcessor;
import com.example.akka.directdebit.importer.fileimport.ImportProcessFlowImpl;
import com.typesafe.config.Config;

@Setup
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class ImporterSetup implements ServiceSetup {

    private final MySettings mySettings;
//    private final ImportProcessFlow importProcessFlow;
//    private final FileLoader fileLoader;
    private final ImportFileProcessor importFileProcessor;
    public ImporterSetup(Config config,
                         HttpClientProvider httpClientProvider,
                         ComponentClient componentClient,
                         Materializer materializer
    ) {
        System.out.println("kafka config:"+System.getProperty("kalix.proxy.eventing.kafka.bootstrap-servers"));
        this.mySettings = new MySettings(config);
        this.importFileProcessor = new ImportFileProcessor(new ImportProcessFlowImpl(httpClientProvider, materializer), new FileLoaderImpl(materializer), mySettings, materializer);
//        this.importProcessFlow = new ImportProcessFlowImpl(httpClientProvider, componentClient, materializer);
//        this.fileLoader = new FileLoaderImpl(materializer);
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new MyDependencyProvider(mySettings,importFileProcessor);
    }

}
