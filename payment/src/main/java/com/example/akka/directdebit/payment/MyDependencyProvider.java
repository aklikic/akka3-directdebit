package com.example.akka.directdebit.payment;

import akka.javasdk.DependencyProvider;
import com.example.akka.directdebit.fileimport.ImportFileProcessor;

public record MyDependencyProvider(MySettings mySettings, ImportFileProcessor messageProcessor) implements DependencyProvider {

    @Override
    public <T> T getDependency(Class<T> var1) {
        if (var1.isAssignableFrom(mySettings.getClass())) {
            return (T) mySettings;
//        } else if (var1.isAssignableFrom(importProcessFlow.getClass())) {
//            return (T) importProcessFlow;
//        } else if (var1.isAssignableFrom(fileLoader.getClass())) {
//            return (T) fileLoader;
        } else if (var1.isAssignableFrom(messageProcessor.getClass())) {
            return (T) messageProcessor;
        } else {
            throw new RuntimeException("No such dependency found: " + String.valueOf(var1));
        }
    }

}
