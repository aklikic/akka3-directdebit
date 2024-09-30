package com.example.akka.directdebit.payment.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.payment.stream.FileLoader;
import com.example.akka.directdebit.payment.stream.ImportProcessFlow;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.api.ImportTopicPublicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ComponentId("import-topic-consumer")
@Consume.FromTopic(ImportTopicPublicMessage.IMPORT_TOPIC_NAME)
public class ImportTopicConsumer extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Materializer materializer;
    private final MySettings mySettings;
    private final ImportProcessFlow processFlow;
    private final FileLoader fileLoader;
    public ImportTopicConsumer(ImportProcessFlow processFlow, FileLoader fileLoader, MySettings mySettings, Materializer materializer) {
        this.materializer = materializer;
        this.mySettings =  mySettings;
        this.processFlow = processFlow;
        this.fileLoader = fileLoader;
    }
    public Effect onMessage(byte[] rawMessage){
        final ImportTopicPublicMessage.FileToImport message;
        try {
            message = ImportTopicPublicMessage.FileToImport.serialize(rawMessage);
        } catch (IOException e) {
            logger.error("Error serializing input message: [{}]",rawMessage);
            return effects().done();
        }
        logger.info("onMessage {}", message);
        var run = fileLoader.load(message.s3fileLocation())
                .via(processFlow.flow(mySettings.importPaymentParallelism, mySettings.importTransactionParallelism))
                .toMat(Sink.ignore(), Keep.right())
                .run(materializer)
                .exceptionally(e-> {
                    logger.error("Error processing flow for message (will retry): {}",e);
                    return Done.getInstance();
                });
        return effects().asyncDone(run);
    }
}
