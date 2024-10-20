package com.example.akka.directdebit.fileimport.serialize;

import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.util.ByteString;
import com.example.akka.directdebit.fileimport.datamodel.Payment;
import com.example.akka.directdebit.fileimport.datamodel.Transaction;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record FileContentSerializationFlow() {
    public Flow<ByteString, Payment,?> serialize(){
        return Flow.<ByteString>create()
                .via(Framing.delimiter(ByteString.fromString("\n"),1024, FramingTruncation.ALLOW))
                .map(ByteString::utf8String)
                .map(line -> {
                    var p = line.split("\\#");
                    var pd = p[0].split("\\/");

                    var ts = p[1].split(";");
                    var trans = Stream.of(ts).map(t -> {
                        var td = t.split("\\/");
                        return new Transaction(td[0],Integer.parseInt(td[1]));
                    }).collect(Collectors.toList());
                    return new Payment(pd[0],Integer.parseInt(pd[1]),trans);
                });
    }

}
