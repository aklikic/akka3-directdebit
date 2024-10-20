package com.example.akka.directdebit.importer.domain;

import java.util.Optional;

public record FileState(String fileName, FileStatus status, Optional<String> error) {

    public enum FileStatus{
        IN_PROCESS,
        PROCESSED,
        PROCESS_ERROR,
        IN_DELETE,
        DELETE_ERROR
    }
    
    public static FileState inProcess(String fileName) {
        return new FileState(fileName, FileStatus.IN_PROCESS, Optional.empty());
    }

    public FileState processError(String error) {
        return new FileState(fileName, FileStatus.PROCESS_ERROR, Optional.of(error));
    }

    public FileState deleting() {
        return new FileState(fileName, FileStatus.IN_DELETE, Optional.empty());
    }

    public FileState deleteError(String error) {
        return new FileState(fileName, FileStatus.DELETE_ERROR, Optional.of(error));
    }
}
