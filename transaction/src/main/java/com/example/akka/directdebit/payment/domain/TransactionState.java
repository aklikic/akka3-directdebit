package com.example.akka.directdebit.payment.domain;

import com.example.akka.directdebit.payment.api.TransactionCommandError;
import static com.example.akka.directdebit.payment.api.TransactionCommandError.*;
import static com.example.akka.directdebit.util.StateCommandProcessResult.*;
import com.example.akka.directdebit.util.StateCommandProcessResult;
import com.example.akka.directdebit.payment.api.TransactionCommand.*;
import com.example.akka.directdebit.payment.domain.TransactionEvent.*;

import java.time.Instant;
import java.util.Optional;

public record TransactionState(String transId, String paymentId, Integer debitAmountCents, Optional<Integer> scheduledForSec ,Optional<String> failReason, TransactionStatus status) {

    public enum TransactionStatus {
        UNKNOWN,
        CREATED,
        INITIALIZED,
        DEBIT_STARTED,
        DEBITED,
        DEBIT_FAILED
    }

    public static TransactionState empty() {
        return new TransactionState(null,null,0,Optional.empty(),Optional.empty(), TransactionStatus.UNKNOWN);
    }

    public StateCommandProcessResult<TransactionEvent, TransactionCommandError> handleCreate(String transId, Create command){
        return switch (status){
            case UNKNOWN -> result(new Created(transId, command.paymentId(), command.debitAmountCents(), Instant.now()));
            case CREATED -> error(DUPLICATED_COMMAND);
            default -> error(TRANSACTION_ALREADY_EXISTS);
        };
    }
    public StateCommandProcessResult<TransactionEvent, TransactionCommandError> handleInitialize(String transId, int transactionDebitDelaySeconds){
        return switch (status){
            case UNKNOWN ->  error(TRANSACTION_NOT_FOUND);
            case CREATED -> result(new Initialized(transId,transactionDebitDelaySeconds,Instant.now()));
            case INITIALIZED -> error(DUPLICATED_COMMAND);
            default -> error(TRANSACTION_IN_WRONG_STATUS);
        };
    }
    public StateCommandProcessResult<TransactionEvent, TransactionCommandError> handleStartDebit(String transId){
        return switch (status){
            case UNKNOWN ->  error(TRANSACTION_NOT_FOUND);
            case INITIALIZED -> result(new DebitStarted(transId,paymentId,debitAmountCents,Instant.now()));
            case DEBIT_STARTED -> error(DUPLICATED_COMMAND);
            default -> error(TRANSACTION_IN_WRONG_STATUS);
        };
    }

    public StateCommandProcessResult<TransactionEvent, TransactionCommandError> handleSetDebitFailed(String transId, SetDebitFailed command){
        return switch (status){
            case UNKNOWN ->  error(TRANSACTION_NOT_FOUND);
            case DEBIT_STARTED -> result(new DebitFailed(transId,command.reason(),Instant.now()));
            case DEBIT_FAILED -> error(DUPLICATED_COMMAND);
            default -> error(TRANSACTION_IN_WRONG_STATUS);
        };
    }
    public StateCommandProcessResult<TransactionEvent, TransactionCommandError> handleSetDebited(String transId){
        return switch (status){
            case UNKNOWN ->  error(TRANSACTION_NOT_FOUND);
            case DEBIT_STARTED -> result(new Debited(transId,Instant.now()));
            case DEBITED -> error(DUPLICATED_COMMAND);
            default -> error(TRANSACTION_IN_WRONG_STATUS);
        };
    }



    public boolean isEmpty() {
        return status == TransactionStatus.UNKNOWN;
    }

    public TransactionState applyEvent(TransactionEvent event){
        return switch (event){
            case Created evt -> new TransactionState(evt.transId(), evt.paymentId(), evt.debitAmountCents(),Optional.empty(),Optional.empty(), TransactionStatus.CREATED);
            case Initialized evt ->  new TransactionState(transId, paymentId, debitAmountCents,Optional.of(evt.scheduledAfterSec()),Optional.empty(), TransactionStatus.INITIALIZED);
            case DebitStarted evt -> new TransactionState(transId, paymentId, debitAmountCents,Optional.empty(),Optional.empty(), TransactionStatus.DEBIT_STARTED);
            case Debited evt -> new TransactionState(transId, paymentId, debitAmountCents,Optional.empty(),Optional.empty(), TransactionStatus.DEBITED);
            case DebitFailed evt -> new TransactionState(transId, paymentId, debitAmountCents,Optional.empty(),Optional.ofNullable(evt.reason()),TransactionStatus.DEBIT_FAILED);
        };
    }
}
