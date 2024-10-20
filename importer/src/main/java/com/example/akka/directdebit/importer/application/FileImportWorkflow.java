package com.example.akka.directdebit.importer.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.workflow.Workflow;
import akka.stream.Materializer;
import com.example.akka.directdebit.fileimport.FileImporter;
import com.example.akka.directdebit.importer.api.ImportCommand;
import com.example.akka.directdebit.importer.api.ImportCommandResponse;
import com.example.akka.directdebit.importer.domain.FileImportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ComponentId("import-file-workflow")
public class FileImportWorkflow extends Workflow<FileImportState> {

    public static final String PROCESS = "process";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileImporter fileImporter;
    private final Materializer materializer;

    public FileImportWorkflow(FileImporter fileImporter, Materializer materializer) {
        this.fileImporter = fileImporter;
        this.materializer = materializer;
    }

    private String processId() {
        return commandContext().workflowId();
    }

    @Override
    public FileImportState emptyState() {
        return FileImportState.empty();
    }

    public Effect<ImportCommandResponse.Ack> start(ImportCommand.StartFileImport command){
        logger.info("importFile processId=[{}]: {}",processId(),command);
        return switch (currentState().status()){
            case UNKNOWN:
            case PROCESS_ERROR:
                yield effects().updateState(currentState().start(command.fileName(), command.folder()))
                    .transitionTo(PROCESS)
                    .thenReply(ImportCommandResponse.Ack.ok());
            default:
                yield effects().reply(ImportCommandResponse.Ack.error("Wrong status: %s".formatted(currentState().status())));
        };
    }

    public Effect<FileImportState> getState() {
        logger.info("getState: processId[{}]",processId());
        return effects().reply(currentState());
    }

    @Override
    public WorkflowDef<FileImportState> definition() {
        var listStep = step(PROCESS)
                .asyncCall(()->
                        fileImporter.process(currentState().fileName(), currentState().folder(), materializer)
                                .thenApply(d -> new ProcessRes(Optional.empty()))
                                .exceptionally(e -> {
                                    logger.error("Error processing file: processId=[{}]",processId());
                                    return new ProcessRes(Optional.of(e.getMessage()));
                                })
                )
                .andThen(ProcessRes.class, res ->
                        effects().updateState(res.error().map(error -> currentState().processError(error)).orElse(currentState().processed()))
                                 .end()
                );
        return workflow()
                .addStep(listStep);
    }

    private record ProcessRes(Optional<String> error){}

}
