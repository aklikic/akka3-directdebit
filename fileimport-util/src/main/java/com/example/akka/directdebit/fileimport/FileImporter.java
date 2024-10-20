package com.example.akka.directdebit.fileimport;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.fileimport.download.FileDownloadSource;
import com.example.akka.directdebit.fileimport.process.FileProcessFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record FileImporter(FileDownloadSource fileDownloadSource, FileProcessFlow processFlow, int importPaymentParallelism, int importTransactionParallelism) {

    private static Logger logger = LoggerFactory.getLogger(FileImporter.class);

    public CompletionStage<Done> process(String fileName, String folder, Materializer materializer){
       return fileDownloadSource.load(fileName, folder, materializer)
                .via(processFlow.flow(importPaymentParallelism, importTransactionParallelism, materializer))
                .toMat(Sink.ignore(), Keep.right())
                .run(materializer);
    }
}
