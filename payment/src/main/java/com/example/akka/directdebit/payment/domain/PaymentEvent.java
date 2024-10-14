package com.example.akka.directdebit.payment.domain;

import java.time.Instant;

public sealed interface PaymentEvent {
    record Created(String paymentId, Integer creditAmountCents, Instant timestamp) implements PaymentEvent {}
    record Initialized(String paymentId, Integer scheduledAfterSec, Instant timestamp) implements PaymentEvent {}
    record CreditStarted(String paymentId, Integer creditAmountCents, Instant timestamp) implements PaymentEvent {}
    record Credited(String paymentId, Instant timestamp) implements PaymentEvent {}
    record CreditFailed(String paymentId, String reason, Instant timestamp) implements PaymentEvent {}
}
