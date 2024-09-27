package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import com.example.akka.directdebit.payment.misc.FileLoader;
import com.example.akka.directdebit.payment.misc.ImportProcessFlow;

public record MyDependencyProvider(MySettings mySettings, ImportProcessFlow importProcessFlow, FileLoader fileLoader) implements DependencyProvider {

    @Override
    public <T> T getDependency(Class<T> var1) {
        if (var1.isAssignableFrom(mySettings.getClass())) {
            return (T) mySettings;
        } else if (var1.isAssignableFrom(importProcessFlow.getClass())) {
            return (T) importProcessFlow;
        } else if (var1.isAssignableFrom(fileLoader.getClass())) {
            return (T) fileLoader;
        } else {
            throw new RuntimeException("No such dependency found: " + String.valueOf(var1));
        }
    }

}
