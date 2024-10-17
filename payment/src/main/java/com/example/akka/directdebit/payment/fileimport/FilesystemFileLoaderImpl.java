package com.example.akka.directdebit.payment.fileimport;

import akka.stream.Materializer;
import akka.stream.javadsl.Source;

public record FilesystemFileLoaderImpl(Materializer materializer) implements FileLoader{
    @Override
    public Source<ImportProcessFlow.Payment, ?> load(String location) {
        return ImportFileUtil.loadFromFile(purifyLocation(location),materializer);
    }
    private String purifyLocation(String location) {
        return location.replaceFirst("file://","");
    }
}
