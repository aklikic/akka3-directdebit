package com.example.akka.directdebit.payment.stream;

import akka.stream.javadsl.Source;

public interface FileLoader {
    Source<ImportProcessFlow.Payment,?> load(String location);
}
