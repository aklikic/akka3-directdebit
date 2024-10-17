package com.example.akka.directdebit.importer.api;

import com.example.akka.directdebit.payment.api.ImportMessage;

public sealed interface ImportCommand {
    record Start(ImportMessage message) implements ImportCommand {}

}
