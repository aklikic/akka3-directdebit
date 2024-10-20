package com.example.akka.directdebit.fileimport;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import com.example.akka.directdebit.fileimport.download.S3FileDownloadSource;
import com.example.akka.directdebit.fileimport.list.S3BucketListSource;
import com.example.akka.directdebit.fileimport.serialize.FileContentSerializationFlow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3Test {

    @Test
    public void testDownload()throws Exception{
        var fileName = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var bucketName = "akka3-direct-debit";
        ActorSystem as = ActorSystem.create("S3Test");
        Materializer mat = Materializer.matFromSystem(as);

        var s3Settings = S3Settings.create(as.settings().config().getConfig(S3Settings.ConfigPath()));
        var fileLoader = new S3FileDownloadSource(s3Settings, new FileContentSerializationFlow());

        var payments = fileLoader.load(fileName,bucketName, mat)
                .toMat(Sink.seq(), Keep.right()).run(mat)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return List.of();
                }).toCompletableFuture().get(10, TimeUnit.SECONDS);

        System.out.println("payments:"+payments.size());
        assertFalse(payments.isEmpty());

    }

    @Test
    public void testList()throws Exception{
        var fileName = "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt";
        var bucketName = "akka3-direct-debit";
        ActorSystem as = ActorSystem.create("S3Test");
        Materializer mat = Materializer.matFromSystem(as);

        var s3Settings = S3Settings.create(as.settings().config().getConfig(S3Settings.ConfigPath()));
        var listSource = new S3BucketListSource(s3Settings);

        var fileNames = listSource.list(bucketName, mat)
                .toMat(Sink.seq(), Keep.right()).run(mat)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return List.of();
                }).toCompletableFuture().get(10, TimeUnit.SECONDS);

        System.out.println("fileNames:"+fileNames.size());
        assertTrue(fileNames.contains(fileName));

    }
}
