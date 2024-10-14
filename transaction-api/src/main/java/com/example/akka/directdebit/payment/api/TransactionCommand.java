package com.example.akka.directdebit.payment.api;

public sealed interface TransactionCommand {
    record Create(String paymentId, Integer debitAmountCents) implements TransactionCommand{}
    record SetDebitFailed(String reason) implements TransactionCommand{}

}
