package com.example.akka.directdebit.payment.misc;

import akka.stream.javadsl.Source;

public interface FileLoader {
    Source<ImportProcessFlow.Payment,?> load(String location);
}
