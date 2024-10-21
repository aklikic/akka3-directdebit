package com.example.akka.directdebit.importer.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.stream.Materializer;
import com.example.akka.directdebit.fileimport.FileImporter;
import com.example.akka.directdebit.importer.api.ImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

//@ComponentId("import-topic-consumer")
//@Consume.FromTopic(ImportMessage.IMPORT_TOPIC_NAME)
public class ImportTopicConsumer extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final FileImporter fileImporter;
    private final Materializer materializer;

    public ImportTopicConsumer(FileImporter fileImporter, Materializer materializer) {
        this.fileImporter = fileImporter;
        this.materializer= materializer;
    }

    public Effect onMessage(byte[] rawMessage){
        final ImportMessage.FileToImport message;
        try {
            message = ImportMessage.FileToImport.deSerialize(rawMessage);
        } catch (IOException e) {
            logger.error("Error serializing input message: [{}]",rawMessage);
            return effects().done();
        }
        logger.info("onMessage {}", message);
        var process = fileImporter.process(message.fileName(), message.folder(), materializer)
                .exceptionally(e-> {
                    logger.error("Error processing flow for message (will retry)", e);
//                    throw new RuntimeException(e);
                    return Done.getInstance();
                });
        return effects().asyncDone(process);
    }
}
