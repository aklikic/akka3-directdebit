package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import akka.javasdk.http.HttpClientProvider;
import akka.stream.Materializer;
import com.example.akka.directdebit.payment.misc.FileLoader;
import com.example.akka.directdebit.payment.misc.FileLoaderImpl;
import com.example.akka.directdebit.payment.misc.ImportProcessFlow;
import com.example.akka.directdebit.payment.misc.ImportProcessFlowImpl;
import com.typesafe.config.Config;

@Setup
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class PaymentSetup implements ServiceSetup {

//    private final MySettings mySettings;
//    private final ImportProcessFlow importProcessFlow;
//    private final FileLoader fileLoader;
//    public PaymentSetup(Config config,
//                        HttpClientProvider httpClientProvider,
//                        Materializer materializer
//    ) {
//        this.mySettings = new MySettings(config);
//        this.importProcessFlow = new ImportProcessFlowImpl(httpClientProvider, materializer);
//        this.fileLoader = new FileLoaderImpl(materializer);
//    }
//
//    @Override
//    public DependencyProvider createDependencyProvider() {
//        return new MyDependencyProvider(mySettings,importProcessFlow,fileLoader);
//    }

//    public PaymentSetup(Config config) {
//        this.mySettings = new MySettings(config);
//    }
//
//
//    @Override
//    public DependencyProvider createDependencyProvider() {
//        return DependencyProvider.single(mySettings);
//    }
}
