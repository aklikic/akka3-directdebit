package com.example.akka.directdebit.payment.api;

import java.util.Optional;

public interface PaymentCommandResponse {

    record GetPaymentStateReply(Optional<ApiPaymentState> state, PaymentCommandError error)implements PaymentCommandResponse {
        public static GetPaymentStateReply ok(ApiPaymentState state) {
            return new GetPaymentStateReply(Optional.ofNullable(state), PaymentCommandError.NO_ERROR);
        }
        public static GetPaymentStateReply error(PaymentCommandError error) {
            return new GetPaymentStateReply(Optional.empty(),error);
        }
    }

    record Ack(PaymentCommandError error)implements PaymentCommandResponse {
        public static Ack ok() {
            return new Ack(PaymentCommandError.NO_ERROR);
        }
        public static Ack error(PaymentCommandError error) {
            return new Ack(error);
        }
    }
    record ApiPaymentState(String paymentId, Integer creditAmount, ApiPaymentStatus status){}
    enum ApiPaymentStatus {
        UNKNOWN,
        CREATED,
        INITIALIZED,
        CREDIT_STARTED,
        CREDITED,
        CREDIT_FAILED
    }
}
