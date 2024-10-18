package com.example.akka.directdebit.fileimport;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.fileimport.S3FileLoaderImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestS3Download {

    @Test
    public void test()throws Exception{
        String location = "s3://import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        ActorSystem as = ActorSystem.create("S3Test");
        Materializer mat = Materializer.matFromSystem(as);
        var bucketName = "akka3-direct-debit";
        var s3Settings = S3Settings.create(as.settings().config().getConfig(S3Settings.ConfigPath()));
        var fileLoader = new S3FileLoaderImpl(bucketName, s3Settings);

        var payments = fileLoader.load(location)
                .toMat(Sink.seq(), Keep.right()).run(mat)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return List.of();
                }).toCompletableFuture().get(10, TimeUnit.SECONDS);

        System.out.println("payments:"+payments.size());
        assertFalse(payments.isEmpty());

    }
}
