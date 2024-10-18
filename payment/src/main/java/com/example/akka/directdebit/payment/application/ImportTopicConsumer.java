package com.example.akka.directdebit.payment.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import com.example.akka.directdebit.fileimport.ImportFileProcessor;
import com.example.akka.directdebit.payment.api.ImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

//@ComponentId("import-topic-consumer")
//@Consume.FromTopic(ImportMessage.IMPORT_TOPIC_NAME)
public class ImportTopicConsumer extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ImportFileProcessor importFileProcessor;

    public ImportTopicConsumer(ImportFileProcessor importFileProcessor) {
        this.importFileProcessor = importFileProcessor;
    }

    public Effect onMessage(byte[] rawMessage){
        final ImportMessage.FileToImport message;
        try {
            message = ImportMessage.FileToImport.serialize(rawMessage);
        } catch (IOException e) {
            logger.error("Error serializing input message: [{}]",rawMessage);
            return effects().done();
        }
        logger.info("onMessage {}", message);
        var process = importFileProcessor.process(message)
                .exceptionally(e-> {
                    logger.error("Error processing flow for message (will retry): {}",e);
//                    throw new RuntimeException(e);
                    return Done.getInstance();
                });
        return effects().asyncDone(process);
    }
}
