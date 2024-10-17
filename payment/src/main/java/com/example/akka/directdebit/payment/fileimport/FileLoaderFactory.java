package com.example.akka.directdebit.payment.fileimport;

import akka.stream.Materializer;
import com.example.akka.directdebit.payment.MySettings;

public record FileLoaderFactory(S3FileLoaderImpl s3FileLoader, FilesystemFileLoaderImpl filesystemFileLoader) {

    public FileLoaderFactory(MySettings settings, Materializer materializer){
        this(new S3FileLoaderImpl(settings.s3BucketName), new FilesystemFileLoaderImpl(materializer));
        System.out.println("FileLoaderFactory created");
    }
    public FileLoader getFileLoader(String fileName) {
        if(fileName.startsWith("s3://")) {
            return s3FileLoader;
        }else{
            return filesystemFileLoader;
        }
    }
}
