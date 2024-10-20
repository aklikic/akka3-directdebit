package com.example.akka.directdebit.fileimport.list;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

public interface FileListSource {
    Source<String,?> list(String folder, Materializer materializer);
    Source<Done,?> delete(String fileName, String folder, Materializer materializer);
}
