package com.example.akka.directdebit.importer.api;

public sealed interface ImportCommand {
    record StartFileImport(String fileName, String folder) implements ImportCommand {}
    record InitialiseFilesList(String folder) implements ImportCommand {}
    record GetFileListState(String folder) implements ImportCommand {}
    record GetFileImportState(String fileName, String folder) implements ImportCommand {}
}
