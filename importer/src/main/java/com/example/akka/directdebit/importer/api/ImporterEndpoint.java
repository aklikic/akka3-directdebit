package com.example.akka.directdebit.importer.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.stream.Materializer;
import com.example.akka.directdebit.fileimport.FileImporter;
import com.example.akka.directdebit.importer.WorkflowId;
import com.example.akka.directdebit.importer.application.FileImportWorkflow;
import com.example.akka.directdebit.importer.application.ListFilesWorkflow;
import com.example.akka.directdebit.importer.domain.FileImportState;
import com.example.akka.directdebit.importer.domain.FileState;

import com.example.akka.directdebit.payment.api.PaymentCommandError;
import com.example.akka.directdebit.importer.api.ImportCommand.*;
import com.example.akka.directdebit.importer.api.ImportCommandResponse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/importer")
public class ImporterEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ImporterEndpoint.class);

    private final ComponentClient componentClient;
    private final FileImporter fileImporter;
    private final Materializer materializer;

    public ImporterEndpoint(ComponentClient componentClient, FileImporter fileImporter, Materializer materializer) {
        this.componentClient = componentClient;
        this.fileImporter = fileImporter;
        this.materializer= materializer;
    }

    @Post("/listing/initialise")
    public CompletionStage<Ack> initialiseFileListing(InitialiseFilesList command){
        logger.info("initialiseFileListing [{}]",command);
        return componentClient.forWorkflow(WorkflowId.filesListWorkflowId(command.folder())).method(ListFilesWorkflow::initialise).invokeAsync(command);
    }

    @Get("/get-file-list-state")
    public CompletionStage<ApiFileListState> getFileListState(ImportCommand.GetFileListState command){
        logger.info("getFileListState [{}]",command);
        return componentClient.forWorkflow(command.folder()).method(ListFilesWorkflow::getState).invokeAsync()
                .thenApply(state -> new ApiFileListState(state.files().stream().map(FileState::fileName).toList(), state.running(), state.lastListTimestamp()));
    }
    @Get("/get-file-import-state")
    public CompletionStage<ApiFileImportState> getFileImportState(ImportCommand.GetFileImportState command){
        logger.info("getFileImportState [{}]",command);
        return componentClient.forWorkflow(command.folder()).method(FileImportWorkflow::getState).invokeAsync()
                .thenApply(state -> new ApiFileImportState(toApi(state.status()),state.error()));
    }

    private static ImportCommandResponse.ApiImportStatus toApi(FileImportState.ImportStatus status){
        return switch (status){
            case UNKNOWN -> ApiImportStatus.UNKNOWN;
            case IN_PROCESS -> ApiImportStatus.IN_PROCESS;
            case PROCESSED -> ApiImportStatus.PROCESSED;
            case PROCESS_ERROR -> ApiImportStatus.PROCESS_ERROR;
        };
    }

    @Post("/import")
    public CompletionStage<Ack> importFile(ImportMessage.FileToImport message){
        logger.info("importFile {}", message);
        return fileImporter.process(message.fileName(), message.folder(), materializer)
                .thenApply(d -> Ack.ok())
                .exceptionally(ex -> {
                    logger.error("importFile: {}",ex);
                    return Ack.error(ex.getMessage());
                });
    }

}
