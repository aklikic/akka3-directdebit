package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;

public record MyDependencyProvider(MySettings mySettings) implements DependencyProvider {

    @Override
    public <T> T getDependency(Class<T> var1) {
        if (var1.isAssignableFrom(mySettings.getClass())) {
            return (T) mySettings;
        } else {
            throw new RuntimeException("No such dependency found: " + String.valueOf(var1));
        }
    }

}
