package com.example.akka.directdebit.importer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface WorkflowId {
    static String fileImportWorkflowId(String fileName, String folder){
        var s = "%s#%s".formatted(fileName,folder);
        return UUID.nameUUIDFromBytes(s.getBytes(StandardCharsets.UTF_8)).toString();
    }
    static String filesListWorkflowId(String folder){
        return UUID.nameUUIDFromBytes(folder.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
