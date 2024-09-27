package com.example.directdebit.transaction.api;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.example.akka.directdebit.payment.misc.ImportFileUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ImportFileGenerator {

    public static void main(String[] args) throws Exception{

        var location = "payment/src/it/resources/import-%s.txt".formatted(UUID.randomUUID().toString());
        ActorSystem as = ActorSystem.create("FileGenerator");
        Materializer mat = Materializer.matFromSystem(as);
        var numberOfPayments = 2;

        var numberOfTransactions = 3;
        var debitAmount = 10;

        var paymentPrefix = UUID.randomUUID().toString();

        var generatedPaymentsSource = ImportFileUtil.generate(paymentPrefix, numberOfPayments,numberOfTransactions,debitAmount);
        ImportFileUtil.storeToFile(generatedPaymentsSource,location,mat).toCompletableFuture().get(3, TimeUnit.SECONDS);
        as.terminate();
    }
}
