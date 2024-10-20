package com.example.akka.directdebit.importer.domain;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record FileListState(String folder, List<FileState> files, boolean running, Instant lastListTimestamp){

    public boolean isEmpty(){
        return folder == null;
    }
    public static FileListState empty(){
        return new FileListState(null, new ArrayList<>(), false,null);
    }
    public FileListState initialise(String folder){
        return new FileListState(folder, files, false,null);
    }
    public FileListState start(){
        return new FileListState(folder, files, true,Instant.now());
    }
    public FileListState end(){
        return new FileListState(folder, files, false,lastListTimestamp);
    }

    public boolean anythingToProcess(List<String> fileNames){
        return fileNames.stream().filter(fileName -> !files.stream().filter(fn -> fileName.equals(fn.fileName())).findAny().isPresent()).count() > 0;
    }

    public FileListState addFiles(List<String> fileNames){
        var listToAdd = fileNames.stream().filter(fileName -> !files.stream().filter(fn -> fileName.equals(fn.fileName())).findAny().isPresent()).map(FileState::inProcess).toList();
        files.addAll(listToAdd);
        return this;
    }

    public FileListState filesProcessStatus(Map<String, Optional<String>> filesProcessStatus){
        var updatedFiles = files.stream().map(file -> filesProcessStatus.get(file.fileName()).map(error -> file.processError(error).deleting()).orElse(file.deleting())).toList();
        return new FileListState(folder, updatedFiles, running, lastListTimestamp);
    }

    public FileListState filesDeleted(){
        return new FileListState(folder, new ArrayList<>(), running, lastListTimestamp);
    }

    public FileListState filesDeleteError(String error){
        var updatedFiles = files.stream()
                .map(file -> file.deleteError(error)).toList();
        return new FileListState(folder, updatedFiles, running, lastListTimestamp);
    }

}
