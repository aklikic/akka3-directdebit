package com.example.akka.directdebit.payment.fileimport;

import akka.stream.alpakka.s3.ObjectMetadata;
import akka.stream.alpakka.s3.javadsl.S3;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.akka.directdebit.payment.MySettings;

import java.util.concurrent.CompletionStage;

public record S3FileLoaderImpl(String s3Bucket) implements FileLoader{

    public S3FileLoaderImpl(MySettings mySettings){
        this(mySettings.s3BucketName);
    }
    @Override
    public Source<ImportProcessFlow.Payment, ?> load(String location) {
        final Source<ByteString, CompletionStage<ObjectMetadata>> s3Source =
                S3.getObject(s3Bucket, purifyLocation(location));
        return s3Source.via(ImportFileUtil.parse());

    }

    private String purifyLocation(String location) {
        return location.replaceFirst("s3://","");
    }

}
