package com.example.akka.directdebit.importer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TestWorkflowId {

    @Test
    public void test(){
        var fileName = "import-%s.txt".formatted(UUID.randomUUID().toString());
        var folder = "importer/src/it/resources/";
        var filesListWorkflowId = WorkflowId.filesListWorkflowId(folder);
        var fileImportWorkflowId = WorkflowId.fileImportWorkflowId(folder, fileName);

        assertEquals(filesListWorkflowId, WorkflowId.filesListWorkflowId(folder));
        assertEquals(fileImportWorkflowId, WorkflowId.fileImportWorkflowId(folder, fileName));

    }
}
