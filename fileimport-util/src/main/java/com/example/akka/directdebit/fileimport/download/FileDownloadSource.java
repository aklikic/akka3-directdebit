package com.example.akka.directdebit.fileimport.download;

import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.fileimport.datamodel.Payment;

public interface FileDownloadSource {
    Source<Payment,?> load(String fileName, String folder, Materializer materializer);
}
