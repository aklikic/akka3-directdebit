package com.example.akka.directdebit.fileimport.download;

import akka.stream.Materializer;
import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.akka.directdebit.fileimport.datamodel.Payment;
import com.example.akka.directdebit.fileimport.serialize.FileContentSerializationFlow;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record S3FileDownloadSource(S3Settings s3settings, FileContentSerializationFlow fileContentSerializerFlow) implements FileDownloadSource {

    private static Logger logger = LoggerFactory.getLogger(S3FileDownloadSource.class);

    public S3FileDownloadSource(FileContentSerializationFlow fileContentSerializerFlow, Config config){
        this(S3Settings.create(config.getConfig(S3Settings.ConfigPath())),fileContentSerializerFlow);
    }

    @Override
    public Source<Payment, ?> load(String fileName, String s3Bucket, Materializer materializer) {
        logger.info("load - location:  {}, bucket: {}", fileName, s3Bucket);
        final Source<ByteString, CompletionStage<ObjectMetadata>> s3Source =
                S3.getObject(s3Bucket, purifyLocation(fileName))
                        .withAttributes(S3Attributes.settings(s3settings));
        return s3Source.via(fileContentSerializerFlow.serialize());

    }

    private String purifyLocation(String location) {
        return location.replaceFirst("s3://","");
    }

}
