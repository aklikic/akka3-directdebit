package com.example.akka.directdebit.fileimport;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.example.akka.directdebit.fileimport.download.FilesystemFileDownloadSource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ImportFileGenerator {

    public static void main(String[] args) throws Exception{

        System.out.println(System.getProperty("user.dir"));
        var location = "payment/filestore/import-%s.txt".formatted(UUID.randomUUID().toString());
        ActorSystem as = ActorSystem.create("FileGenerator");
        Materializer mat = Materializer.matFromSystem(as);
        var numberOfPayments = 2;

        var numberOfTransactions = 3;
        var debitAmount = 10;

        var paymentPrefix = UUID.randomUUID().toString();

        var generatedPaymentsSource = FilesystemFileDownloadSource.generate(paymentPrefix, numberOfPayments,numberOfTransactions,debitAmount);
        FilesystemFileDownloadSource.storeToFile(generatedPaymentsSource,location,mat).toCompletableFuture().get(3, TimeUnit.SECONDS);
        as.terminate();

        System.out.println("File: " + location);
    }
}
