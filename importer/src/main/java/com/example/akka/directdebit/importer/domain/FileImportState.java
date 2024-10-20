package com.example.akka.directdebit.importer.domain;

import java.util.Optional;

public record FileImportState(String fileName, String folder, ImportStatus status, Optional<String> error) {

    public enum ImportStatus{
        UNKNOWN,
        IN_PROCESS,
        PROCESSED,
        PROCESS_ERROR
    }
    public static FileImportState empty() {
        return new FileImportState(null, null, ImportStatus.UNKNOWN, Optional.empty());
    }
    public FileImportState start(String fileName, String folder){
        return new FileImportState(fileName, folder, ImportStatus.IN_PROCESS, Optional.empty());
    }
    public FileImportState processError(String error){
        return new FileImportState(fileName, folder, ImportStatus.PROCESS_ERROR, Optional.of(error));
    }
    public FileImportState processed(){
        return new FileImportState(fileName, folder, ImportStatus.PROCESSED, Optional.empty());
    }
}
