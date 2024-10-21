package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import com.typesafe.config.Config;

@Setup
public class PaymentSetup implements ServiceSetup {

    private final MySettings mySettings;
    public PaymentSetup(Config config
    ) {
        this.mySettings = new MySettings(config);
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new MyDependencyProvider(mySettings);
    }

}
