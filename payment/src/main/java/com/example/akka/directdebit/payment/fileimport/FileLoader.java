package com.example.akka.directdebit.payment.fileimport;

import akka.stream.javadsl.Source;

public interface FileLoader {
    Source<ImportProcessFlow.Payment,?> load(String location);
}
