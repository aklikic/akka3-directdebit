package com.example.akka.directdebit.payment.fileimport;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface ImportFileUtil {
    static Source<ImportProcessFlow.Payment,?> loadFromFile(String location, Materializer materializer){
        final Path file = Paths.get(location);
        return FileIO.fromPath(file)
                .via(parse());

    }

    static Flow<ByteString, ImportProcessFlow.Payment,?> parse(){
        return Flow.<ByteString>create()
                .via(Framing.delimiter(ByteString.fromString("\n"),1024, FramingTruncation.ALLOW))
                .map(ByteString::utf8String)
                .map(line -> {
                    var p = line.split("\\#");
                    var pd = p[0].split("\\/");

                    var ts = p[1].split(";");
                    var trans = Stream.of(ts).map(t -> {
                        var td = t.split("\\/");
                        return new ImportProcessFlow.Transaction(td[0],Integer.parseInt(td[1]));
                    }).collect(Collectors.toList());
                    return new ImportProcessFlow.Payment(pd[0],Integer.parseInt(pd[1]),trans);
                });
    }

    static CompletionStage<Done> storeToFile(Source<ImportProcessFlow.Payment,?> source, String location, Materializer materializer){

//        var content = IntStream.range(0,numberOfPayments)
//                .mapToObj(pInx -> {
//                    var trans = IntStream.range(0,numberOfTransactions).mapToObj(tInx -> "T-%s/%s".formatted(tInx,debitAmount)).collect(Collectors.joining(";"));
//                    return "P-%s/%s#%s".formatted(pInx,creditAmount,trans);
//                }).collect(Collectors.joining("\n"));

        return source.map(payment -> {
                    var trans = payment.trans().stream().map(t -> "%s/%s".formatted(t.transId(),t.debitAmount())).collect(Collectors.joining(";"));
                    var line = "%s/%s#%s\n".formatted(payment.paymentId(),payment.creditAmount(),trans);
//                    System.out.println(line);
                    return line;
                }).map(ByteString::fromString)
                .toMat(FileIO.toPath(Path.of(location)),Keep.right())
                .run(materializer)
                .thenApply(r -> Done.getInstance());
    }

    static Source<ImportProcessFlow.Payment,?> generate(String paymentPrefix, int numOfPayment, int numOfTransactions, int tranDebitAmount){
        return Source.range(1,numOfPayment).map(paymentIndex -> {
            var paymentId = "p%s-%s".formatted(paymentIndex,paymentPrefix);
            var trans = IntStream.range(1,numOfTransactions+1).mapToObj(transIndex -> new ImportProcessFlow.Transaction("t%s-%s".formatted(transIndex,paymentId),tranDebitAmount)).collect(Collectors.toList());
            return new ImportProcessFlow.Payment(paymentId,tranDebitAmount*trans.size(),trans);
        });
    }

}
