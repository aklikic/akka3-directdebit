package com.example.akka.directdebit.payment.api;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;

import java.io.IOException;

public sealed interface ImportMessage {
    String IMPORT_TOPIC_NAME = "import";
    record FileToImport(String fileLocation) implements ImportMessage {
        public static FileToImport serialize(byte[] rawMessage) throws IOException {
            return JsonSupport.parseBytes(rawMessage, FileToImport.class);
        }
        @JsonIgnore
        public ByteString deSerialize() throws JsonProcessingException {
            return JsonSupport.encodeToBytes(this);
        }
    }


}
