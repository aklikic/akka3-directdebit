package com.example.akka.directdebit.importer.api;

public sealed interface ImportTopicPublicMessage {
    record FileToImport(String s3fileLocation) implements ImportTopicPublicMessage {}
}
