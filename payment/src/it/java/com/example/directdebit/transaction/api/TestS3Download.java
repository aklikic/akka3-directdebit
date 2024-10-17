package com.example.directdebit.transaction.api;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.fileimport.FileLoaderFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class TestS3Download {

    @Test
    public void test()throws Exception{
        String location = "s3://import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        ActorSystem as = ActorSystem.create("S3Test");
        Materializer mat = Materializer.matFromSystem(as);

        var bucketName = "akka3-direct-debit";

        var mySettings = new MySettings(1,1,1,bucketName);
        var fileLoader = new FileLoaderFactory(mySettings,mat).getFileLoader(location);

        fileLoader.load(location)
                .toMat(Sink.foreach(System.out::println), Keep.right()).run(mat)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return Done.getInstance();
                }).toCompletableFuture().get(10, TimeUnit.SECONDS);



    }
}
