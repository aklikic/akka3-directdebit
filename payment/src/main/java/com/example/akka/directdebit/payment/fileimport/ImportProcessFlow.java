package com.example.akka.directdebit.payment.fileimport;

import akka.stream.javadsl.Flow;

import java.util.List;

public interface ImportProcessFlow {

    Flow<Payment, Payment, ?> flow(int parallelismPayment, int parallelismTransactions);

    record Transaction(String transId, int debitAmount){}

    record Payment(String paymentId, int creditAmount, List<Transaction> trans){}
}
