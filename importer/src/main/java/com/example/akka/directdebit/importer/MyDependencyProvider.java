package com.example.akka.directdebit.importer;

import akka.javasdk.DependencyProvider;
import com.example.akka.directdebit.fileimport.FileImporter;
import com.example.akka.directdebit.fileimport.list.FileListSource;
import com.example.akka.directdebit.fileimport.download.FileDownloadSource;

public record MyDependencyProvider(MySettings mySettings, FileImporter fileImporter,  FileListSource fileListSource) implements DependencyProvider {

    @Override
    public <T> T getDependency(Class<T> var1) {
        if (var1.isAssignableFrom(mySettings.getClass())) {
            return (T) mySettings;
        } else if (var1.isAssignableFrom(fileImporter.getClass())) {
            return (T) fileImporter;
        } else if (var1.isAssignableFrom(fileListSource.getClass())) {
            return (T) fileListSource;
        } else {
            throw new RuntimeException("No such dependency found: " + String.valueOf(var1));
        }
    }

}
