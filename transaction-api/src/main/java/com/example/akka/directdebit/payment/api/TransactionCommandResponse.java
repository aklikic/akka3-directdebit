package com.example.akka.directdebit.payment.api;

import java.util.Optional;

public interface TransactionCommandResponse {

    record GetTransactionStateReply(Optional<ApiTransactionState> state, TransactionCommandError error)implements TransactionCommandResponse{
        public static GetTransactionStateReply ok(ApiTransactionState state) {
            return new GetTransactionStateReply(Optional.ofNullable(state), TransactionCommandError.NO_ERROR);
        }
        public static GetTransactionStateReply error(TransactionCommandError error) {
            return new GetTransactionStateReply(Optional.empty(),error);
        }
    }

    record Ack(TransactionCommandError error)implements TransactionCommandResponse{
        public static Ack ok() {
            return new Ack(TransactionCommandError.NO_ERROR);
        }
        public static Ack error(TransactionCommandError error) {
            return new Ack(error);
        }
    }
    record ApiTransactionState(String transId, String paymentId, Integer debitAmount, ApiTransactionStatus status){}
    enum ApiTransactionStatus {
        UNKNOWN,
        CREATED,
        INITIALIZED,
        DEBIT_STARTED,
        DEBITED,
        DEBIT_FAILED
    }
}
