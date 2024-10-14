package com.example.akka.directdebit.importer;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileGenerator {

    public static void main(String[] args) throws Exception{

        var location = "importer/src/it/resources/import-%s.txt".formatted(UUID.randomUUID().toString());
        ActorSystem as = ActorSystem.create("FileGenerator");
        Materializer mat = Materializer.matFromSystem(as);
        var numberOfPayments = 1;

        var numberOfTransactions = 1;
        var debitAmount = 10;
        var creditAmount = debitAmount * numberOfTransactions;

        var content = IntStream.range(0,numberOfPayments)
            .mapToObj(pInx -> {
                var trans = IntStream.range(0,numberOfTransactions).mapToObj(tInx -> "T-%s/%s".formatted(tInx,debitAmount)).collect(Collectors.joining(";"));
                return "P-%s/%s#%s".formatted(pInx,creditAmount,trans);
            }).collect(Collectors.joining("\n"));

        var res = Source.single(content).map(ByteString::fromString).toMat(FileIO.toPath(Path.of(location)),Keep.right()).run(mat).toCompletableFuture().get(3, TimeUnit.SECONDS);
        as.terminate();
    }
}
