package com.example.akka.directdebit.payment.api;

public enum TransactionCommandError {
    NO_ERROR,
    TRANSACTION_ALREADY_EXISTS,
    TRANSACTION_NOT_FOUND,
    DUPLICATED_COMMAND,
    TRANSACTION_IN_WRONG_STATUS
}
