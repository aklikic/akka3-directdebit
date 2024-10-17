package com.example.akka.directdebit.importer.api;

import java.util.List;
import java.util.Optional;

public interface ImportCommandResponse {

    record GetImportStateReply(Optional<ApiImportState> state, ImportCommandError error)implements ImportCommandResponse {
        public static GetImportStateReply ok(ApiImportState state) {
            return new GetImportStateReply(Optional.ofNullable(state), ImportCommandError.NO_ERROR);
        }
        public static GetImportStateReply error(ImportCommandError error) {
            return new GetImportStateReply(Optional.empty(),error);
        }
    }

    record Ack(ImportCommandError error)implements ImportCommandResponse {
        public static Ack ok() {
            return new Ack(ImportCommandError.NO_ERROR);
        }
        public static Ack error(ImportCommandError error) {
            return new Ack(error);
        }
    }
    record ApiTransaction(String transId, Integer debitAmount){};
    record ApiPayment(String paymentId, Integer creditAmount, List<ApiTransaction> transactions){};
    record ApiImportState(String fileName, List<ApiPayment> payments){}
    enum ApiPaymentStatus {
        UNKNOWN,
        CREATED,
        INITIALIZED
    }
}
