package com.example.akka.directdebit.importer;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import akka.javasdk.http.HttpClientProvider;
import com.example.akka.directdebit.fileimport.FileImporter;
import com.example.akka.directdebit.fileimport.download.S3FileDownloadSource;
import com.example.akka.directdebit.fileimport.list.FileListSource;
import com.example.akka.directdebit.fileimport.list.S3BucketListSource;
import com.example.akka.directdebit.fileimport.process.FileProcessFlowImpl;
import com.example.akka.directdebit.fileimport.serialize.FileContentSerializationFlow;
import com.typesafe.config.Config;

@Setup
public class ImporterSetup implements ServiceSetup {

    private final MySettings mySettings;
    private final FileImporter fileImporter;
    private final FileListSource fileListSource;
    public ImporterSetup(Config config,
                         HttpClientProvider httpClientProvider
    ) {
        System.out.println("kafka config:"+System.getProperty("kalix.proxy.eventing.kafka.bootstrap-servers"));
        this.mySettings = new MySettings(config);
        var fileDownloadSource = new S3FileDownloadSource(new FileContentSerializationFlow(), config);
        this.fileImporter = new FileImporter(fileDownloadSource, new FileProcessFlowImpl(httpClientProvider), mySettings.importPaymentParallelism, mySettings.importTransactionParallelism);
        this.fileListSource = new S3BucketListSource(config);
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new MyDependencyProvider(mySettings,fileImporter,fileListSource);
    }

}
