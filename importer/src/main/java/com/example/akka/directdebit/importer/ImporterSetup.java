package com.example.akka.directdebit.importer;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Setup;
import com.typesafe.config.Config;

@Setup
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
public class ImporterSetup implements ServiceSetup {

    private final Config config;
    public ImporterSetup(Config config) {
        this.config = config;
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return DependencyProvider.single(new MySettings(config));
    }
}
