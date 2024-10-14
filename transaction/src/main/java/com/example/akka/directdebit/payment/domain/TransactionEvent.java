package com.example.akka.directdebit.payment.domain;

import java.time.Instant;

public sealed interface TransactionEvent {
    record Created(String transId, String paymentId, Integer debitAmountCents, Instant timestamp) implements TransactionEvent{}
    record Initialized(String transId, Integer scheduledAfterSec, Instant timestamp) implements TransactionEvent{}
    record DebitStarted(String transId, String paymentId, Integer debitAmountCents, Instant timestamp) implements TransactionEvent{}
    record Debited(String transId, Instant timestamp) implements TransactionEvent{}
    record DebitFailed(String transId, String reason, Instant timestamp) implements TransactionEvent{}
}
