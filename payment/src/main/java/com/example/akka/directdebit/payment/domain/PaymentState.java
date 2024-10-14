package com.example.akka.directdebit.payment.domain;

import com.example.akka.directdebit.payment.api.PaymentCommand.*;
import com.example.akka.directdebit.payment.api.PaymentCommandError;
import com.example.akka.directdebit.payment.domain.PaymentEvent;
import com.example.akka.directdebit.payment.domain.PaymentEvent.*;
import com.example.akka.directdebit.util.StateCommandProcessResult;

import java.time.Instant;
import java.util.Optional;

import static com.example.akka.directdebit.payment.api.PaymentCommandError.*;
import static com.example.akka.directdebit.util.StateCommandProcessResult.error;
import static com.example.akka.directdebit.util.StateCommandProcessResult.result;

public record PaymentState(String paymentId, Integer creditAmountCents, Optional<Integer> scheduledForSec , Optional<String> failReason, PaymentStatus status) {

    public enum PaymentStatus {
        UNKNOWN,
        CREATED,
        INITIALIZED,
        CREDIT_STARTED,
        CREDITED,
        CREDIT_FAILED
    }

    public static PaymentState empty() {
        return new PaymentState(null,0,Optional.empty(),Optional.empty(), PaymentStatus.UNKNOWN);
    }

    public StateCommandProcessResult<PaymentEvent, PaymentCommandError> handleCreate(String paymentId, Create command){
        return switch (status){
            case UNKNOWN -> result(new Created(paymentId, command.creditAmountCents(), Instant.now()));
            case CREATED -> error(DUPLICATED_COMMAND);
            default -> error(PAYMENT_ALREADY_EXISTS);
        };
    }
    public StateCommandProcessResult<PaymentEvent, PaymentCommandError> handleInitialize(String paymentId, int paymentCreditDelaySeconds){
        return switch (status){
            case UNKNOWN ->  error(PAYMENT_NOT_FOUND);
            case CREATED -> result(new Initialized(paymentId,paymentCreditDelaySeconds,Instant.now()));
            case INITIALIZED -> error(DUPLICATED_COMMAND);
            default -> error(PAYMENT_IN_WRONG_STATUS);
        };
    }
    public StateCommandProcessResult<PaymentEvent, PaymentCommandError> handleStartCredit(String paymentId){
        return switch (status){
            case UNKNOWN ->  error(PAYMENT_NOT_FOUND);
            case INITIALIZED -> result(new CreditStarted(paymentId,creditAmountCents,Instant.now()));
            case CREDIT_STARTED -> error(DUPLICATED_COMMAND);
            default -> error(PAYMENT_IN_WRONG_STATUS);
        };
    }

    public StateCommandProcessResult<PaymentEvent, PaymentCommandError> handleSetCreditFailed(String paymentId, SetCreditFailed command){
        return switch (status){
            case UNKNOWN ->  error(PAYMENT_NOT_FOUND);
            case CREDIT_STARTED -> result(new CreditFailed(paymentId,command.reason(),Instant.now()));
            case CREDIT_FAILED -> error(DUPLICATED_COMMAND);
            default -> error(PAYMENT_IN_WRONG_STATUS);
        };
    }
    public StateCommandProcessResult<PaymentEvent, PaymentCommandError> handleSetCredited(String paymentId){
        return switch (status){
            case UNKNOWN ->  error(PAYMENT_NOT_FOUND);
            case CREDIT_STARTED -> result(new Credited(paymentId,Instant.now()));
            case CREDITED -> error(DUPLICATED_COMMAND);
            default -> error(PAYMENT_IN_WRONG_STATUS);
        };
    }



    public boolean isEmpty() {
        return status == PaymentStatus.UNKNOWN;
    }

    public PaymentState applyEvent(PaymentEvent event){
        return switch (event){
            case Created evt -> new PaymentState(evt.paymentId(),  evt.creditAmountCents(),Optional.empty(),Optional.empty(), PaymentStatus.CREATED);
            case Initialized evt ->  new PaymentState(paymentId, creditAmountCents,Optional.of(evt.scheduledAfterSec()),Optional.empty(), PaymentStatus.INITIALIZED);
            case CreditStarted evt -> new PaymentState(paymentId, creditAmountCents,Optional.empty(),Optional.empty(), PaymentStatus.CREDIT_STARTED);
            case Credited evt -> new PaymentState(paymentId, creditAmountCents,Optional.empty(),Optional.empty(), PaymentStatus.CREDITED);
            case CreditFailed evt -> new PaymentState(paymentId, creditAmountCents,Optional.empty(),Optional.ofNullable(evt.reason()),PaymentStatus.CREDIT_FAILED);
        };
    }
}
