package com.example.akka.directdebit.fileimport.download;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.akka.directdebit.fileimport.datamodel.Payment;
import com.example.akka.directdebit.fileimport.datamodel.Transaction;
import com.example.akka.directdebit.fileimport.serialize.FileContentSerializationFlow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record FilesystemFileDownloadSource(FileContentSerializationFlow fileContentSerializerFlow) implements FileDownloadSource {
    @Override
    public Source<Payment, ?> load(String fileName, String folder, Materializer materializer) {
        final Path file = Paths.get("%s/%s", folder, fileName);
        return FileIO.fromPath(file)
                .via(fileContentSerializerFlow.serialize());
    }
    public static CompletionStage<Done> storeToFile(Source<Payment,?> source, String location, Materializer materializer){
        return source.map(payment -> {
                    var trans = payment.trans().stream().map(t -> "%s/%s".formatted(t.transId(),t.debitAmount())).collect(Collectors.joining(";"));
                    var line = "%s/%s#%s\n".formatted(payment.paymentId(),payment.creditAmount(),trans);
                    return line;
                }).map(ByteString::fromString)
                .toMat(FileIO.toPath(Path.of(location)), Keep.right())
                .run(materializer)
                .thenApply(r -> Done.getInstance());
    }

    public static Source<Payment,?> generate(String paymentPrefix, int numOfPayment, int numOfTransactions, int tranDebitAmount){
        return Source.range(1,numOfPayment).map(paymentIndex -> {
            var paymentId = "p%s-%s".formatted(paymentIndex,paymentPrefix);
            var trans = IntStream.range(1,numOfTransactions+1).mapToObj(transIndex -> new Transaction("t%s-%s".formatted(transIndex,paymentId),tranDebitAmount)).collect(Collectors.toList());
            return new Payment(paymentId,tranDebitAmount*trans.size(),trans);
        });
    }
}
