package com.example.akka.directdebit.payment.api;

public sealed interface PaymentCommand {
    record Create(Integer creditAmountCents) implements PaymentCommand {}
    record SetCreditFailed(String reason) implements PaymentCommand {}

}
