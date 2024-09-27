package com.example.akka.directdebit.payment.api;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;

import java.io.IOException;

public sealed interface ImportTopicPublicMessage {
    String IMPORT_TOPIC_NAME = "import";
    record FileToImport(String s3fileLocation) implements ImportTopicPublicMessage {
        public static FileToImport serialize(ByteString rawMessage) throws IOException {
            return JsonSupport.parseBytes(rawMessage.toByteArray(), FileToImport.class);
        }
        @JsonIgnore
        public ByteString deSerialize() throws JsonProcessingException {
            return JsonSupport.encodeToBytes(this);
        }
    }


}
