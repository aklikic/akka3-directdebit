package com.example.akka.directdebit.importer.fileimport;

import akka.stream.javadsl.Source;

public interface FileLoader {
    Source<ImportProcessFlow.Payment,?> load(String location);
}
