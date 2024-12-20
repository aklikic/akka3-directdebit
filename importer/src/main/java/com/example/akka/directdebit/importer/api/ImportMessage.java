package com.example.akka.directdebit.importer.api;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;

import java.io.IOException;

public sealed interface ImportMessage {
    String IMPORT_TOPIC_NAME = "import";
    record FileToImport(String fileName, String folder) implements ImportMessage {
        public static FileToImport deSerialize(byte[] rawMessage) throws IOException {
            return JsonSupport.parseBytes(rawMessage, FileToImport.class);
        }
        @JsonIgnore
        public ByteString serialize() throws JsonProcessingException {
            return JsonSupport.encodeToBytes(this);
        }
    }


}
