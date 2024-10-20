package com.example.akka.directdebit.importer.api;

import com.example.akka.directdebit.importer.domain.FileImportState;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ImportCommandResponse {

    record GetImportStateReply(Optional<ApiFileListState> state, Optional<String> error)implements ImportCommandResponse {
        public static GetImportStateReply ok(ApiFileListState state) {
            return new GetImportStateReply(Optional.ofNullable(state), Optional.empty());
        }
        public static GetImportStateReply error(String error) {
            return new GetImportStateReply(Optional.empty(),Optional.of(error));
        }
    }
    record Ack(Optional<String> error)implements ImportCommandResponse {
        public static Ack ok() {
            return new Ack(Optional.empty());
        }
        public static Ack error(String error) {
            return new Ack(Optional.of(error));
        }
    }
    record ApiFileListState(List<String> fileNames, boolean running, Instant lastListTimestamp){}


    record ApiFileImportState(ApiImportStatus status, Optional<String> error){}
    enum ApiImportStatus{
        UNKNOWN,
        IN_PROCESS,
        PROCESSED,
        PROCESS_ERROR
    }
}
