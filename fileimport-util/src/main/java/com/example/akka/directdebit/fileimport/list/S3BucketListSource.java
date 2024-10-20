package com.example.akka.directdebit.fileimport.list;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.ListBucketResultContents;
import akka.stream.alpakka.s3.ListBucketsResultContents;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.fileimport.download.S3FileDownloadSource;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public record S3BucketListSource(S3Settings s3settings) implements FileListSource{

    private static Logger logger = LoggerFactory.getLogger(S3FileDownloadSource.class);

    public S3BucketListSource(Config config){
        this(S3Settings.create(config.getConfig(S3Settings.ConfigPath())));
    }

    @Override
    public Source<String, ?> list(String s3Bucket, Materializer materializer) {
        logger.info("list bucket: {}", s3Bucket);
        return  S3.listBucket(s3Bucket, Optional.empty())
                  .withAttributes(S3Attributes.settings(s3settings))
                  .map(ListBucketResultContents::key);
    }

    @Override
    public Source<Done, ?> delete(String fileName, String s3Bucket, Materializer materializer) {
        logger.info("delete file from bucket {}: {}", s3Bucket,fileName);
        return  S3.deleteObject(s3Bucket,fileName)
                  .withAttributes(S3Attributes.settings(s3settings));
    }
}
