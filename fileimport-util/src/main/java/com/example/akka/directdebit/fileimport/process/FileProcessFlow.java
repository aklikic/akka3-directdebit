package com.example.akka.directdebit.fileimport.process;

import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.example.akka.directdebit.fileimport.datamodel.Payment;

public interface FileProcessFlow {
    Flow<Payment, Payment, ?> flow(int parallelismPayment, int parallelismTransactions, Materializer materializer);
}
