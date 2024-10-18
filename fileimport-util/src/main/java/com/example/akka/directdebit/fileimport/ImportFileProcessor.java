package com.example.akka.directdebit.fileimport;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.payment.api.ImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record ImportFileProcessor(ImportProcessFlow processFlow, FileLoader fileLoader, int importPaymentParallelism, int importTransactionParallelism, Materializer materializer) {

    private static Logger logger = LoggerFactory.getLogger(ImportFileProcessor.class);

    public CompletionStage<Done> process(ImportMessage.FileToImport message){
       return fileLoader.load(message.fileLocation())
                .via(processFlow.flow(importPaymentParallelism, importTransactionParallelism))
                .toMat(Sink.ignore(), Keep.right())
                .run(materializer);
    }
}
