package com.example.akka.directdebit.fileimport.list;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

public interface FileListSource {
    Source<String,?> list(Materializer materializer);
    Source<Done,?> delete(String fileName, Materializer materializer);
}
