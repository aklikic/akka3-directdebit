package com.example.akka.directdebit.fileimport;

import akka.stream.javadsl.Source;

public interface FileLoader {
    Source<ImportProcessFlow.Payment,?> load(String location);
}