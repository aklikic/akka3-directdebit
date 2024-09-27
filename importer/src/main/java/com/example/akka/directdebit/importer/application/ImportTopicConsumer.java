package com.example.akka.directdebit.importer.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.util.ByteString;
import com.example.akka.directdebit.importer.api.ImportTopicPublicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@ComponentId("import-topic-consumer")
@Consume.FromTopic("import")
public class ImportTopicConsumer extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    public Effect onMessage(ImportTopicPublicMessage message){
//        logger.info("onMessage {}", message);
//        return switch (message){
//            case ImportTopicPublicMessage.FileToImport m -> {
//                final Path file = Paths.get(m.s3fileLocation());
//                FileIO.fromPath(file).via(Framing.delimiter(ByteString.fromString("\n"),1024))
//                        .
//            }
//        };
//    }
}
