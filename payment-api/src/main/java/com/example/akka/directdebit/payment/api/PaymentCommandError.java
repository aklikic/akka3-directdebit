package com.example.akka.directdebit.payment.api;

public enum PaymentCommandError {
    NO_ERROR,
    PAYMENT_ALREADY_EXISTS,
    PAYMENT_NOT_FOUND,
    DUPLICATED_COMMAND,
    PAYMENT_IN_WRONG_STATUS
}
