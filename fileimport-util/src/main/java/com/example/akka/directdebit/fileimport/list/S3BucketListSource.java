package com.example.akka.directdebit.fileimport.list;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.ListBucketsResultContents;
import akka.stream.alpakka.s3.S3Attributes;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.fileimport.download.S3FileDownloadSource;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record S3BucketListSource(String s3Bucket, S3Settings s3settings) implements FileListSource{

    private static Logger logger = LoggerFactory.getLogger(S3FileDownloadSource.class);

    public S3BucketListSource(String s3Bucket, Config config){
        this(s3Bucket, S3Settings.create(config.getConfig(S3Settings.ConfigPath())));
    }

    @Override
    public Source<String, ?> list(Materializer materializer) {
        logger.info("list bucket: {}", s3Bucket);
        return  S3.listBuckets()
                  .withAttributes(S3Attributes.settings(s3settings))
                  .map(ListBucketsResultContents::getName);
    }

    @Override
    public Source<Done, ?> delete(String fileName,Materializer materializer) {
        logger.info("delete file from bucket {}: {}", s3Bucket,fileName);
        return  S3.deleteObject(s3Bucket,fileName)
                  .withAttributes(S3Attributes.settings(s3settings));
    }
}
