package com.example.akka.directdebit.importer.fileimport;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.importer.MySettings;
import com.example.akka.directdebit.payment.api.ImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record ImportFileProcessor(ImportProcessFlow processFlow, FileLoader fileLoader, MySettings mySettings, Materializer materializer) {

    private static Logger logger = LoggerFactory.getLogger(ImportFileProcessor.class);

    public CompletionStage<Done> process(ImportMessage.FileToImport message){

        return fileLoader.load(message.fileLocation())
                .via(processFlow.flow(mySettings.importPaymentParallelism, mySettings.importTransactionParallelism))
                .toMat(Sink.ignore(), Keep.right())
                .run(materializer);
    }
}
