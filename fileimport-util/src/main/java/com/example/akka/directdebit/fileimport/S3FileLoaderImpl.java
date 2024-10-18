package com.example.akka.directdebit.fileimport;

import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record S3FileLoaderImpl(String s3Bucket, S3Settings s3settings) implements FileLoader {

    public S3FileLoaderImpl(String s3Bucket, Config config){
        this(s3Bucket, S3Settings.create(config.getConfig(S3Settings.ConfigPath())));
    }
    private static Logger logger = LoggerFactory.getLogger(S3FileLoaderImpl.class);
    @Override
    public Source<ImportProcessFlow.Payment, ?> load(String location) {
        logger.info("load - location:  {}, bucket: {}", location, s3Bucket);
        final Source<ByteString, CompletionStage<ObjectMetadata>> s3Source =
                S3.getObject(s3Bucket, purifyLocation(location))
                        .withAttributes(S3Attributes.settings(s3settings));
        return s3Source.via(ImportFileUtil.parse());

    }

    private String purifyLocation(String location) {
        return location.replaceFirst("s3://","");
    }

}
