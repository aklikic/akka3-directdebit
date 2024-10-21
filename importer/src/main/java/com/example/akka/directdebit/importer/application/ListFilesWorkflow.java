package com.example.akka.directdebit.importer.application;


import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.akka.directdebit.fileimport.list.FileListSource;
import com.example.akka.directdebit.importer.MySettings;
import com.example.akka.directdebit.importer.WorkflowId;
import com.example.akka.directdebit.importer.api.ImportCommand;
import com.example.akka.directdebit.importer.api.ImportCommandResponse;
import com.example.akka.directdebit.importer.domain.FileListState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ComponentId("list-files-workflow")
public class ListFilesWorkflow extends Workflow<FileListState> {

  public static final String LIST = "list";
  public static final String SEND_FILES_FOR_IMPORT = "send-files-for-import";
  public static final String DELETE = "delete";
  public static final String SCHEDULE_NEXT_LIST = "schedule-next-list";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final FileListSource fileListSource;
  private final Materializer materializer;
  private final MySettings settings;
  private final ComponentClient componentClient;

  public ListFilesWorkflow(FileListSource fileListSource, Materializer materializer, MySettings settings, ComponentClient componentClient) {
    this.fileListSource = fileListSource;
    this.materializer = materializer;
    this.settings = settings;
    this.componentClient = componentClient;
  }

  @Override
  public FileListState emptyState() {
    return FileListState.empty();
  }

  private String locationId() {
    return commandContext().workflowId();
  }


    public Effect<ImportCommandResponse.Ack> initialise(ImportCommand.InitialiseFilesList command) {
        logger.info("initialise locationId=[{}]: {}",locationId(),command);
        if(currentState().isEmpty()){
            return effects()
                    .updateState(currentState().initialise(command.folder()).start())
                    .transitionTo(LIST)
                    .thenReply(ImportCommandResponse.Ack.ok());
        }else{
            logger.info("listFiles: locationId=[{}] already initialised!",locationId());
            return effects().reply(ImportCommandResponse.Ack.error("Already initialised!"));
        }
    }

  public Effect<ImportCommandResponse.Ack> listFiles() {
    logger.info("listFiles locationId=[{}]",locationId());
    if(!currentState().running()){
      return effects()
              .updateState(currentState().start())
              .transitionTo(LIST)
              .thenReply(ImportCommandResponse.Ack.ok());
    }else{
      logger.info("listFiles: locationId=[{}] already running!",locationId());
      return effects().reply(ImportCommandResponse.Ack.error("Already running!"));
    }
  }

  public Effect<FileListState> getState() {
    logger.info("getState: locationId[{}]",locationId());
    return effects().reply(currentState());
  }

  @Override
  public WorkflowDef<FileListState> definition() {

    var listStep = step(LIST)
      .asyncCall(()->
        fileListSource.list(currentState().folder(), materializer).toMat(Sink.seq(), Keep.right()).run(materializer)
                .thenApply(ListedFileNames::new)
                .exceptionally(e-> {
                    logger.error("Error listing files from location [{}]", locationId(), e);
                    return new ListedFileNames(List.of());
                 })
      )
      .andThen(ListedFileNames.class, res ->{
          logger.info("files listed [{}]: {}",locationId(),res);
          if(!res.fileNames().isEmpty()){
            if(currentState().anythingToProcess(res.fileNames())) {
                logger.info("Adding files for process [{}]: {}",locationId(),res);
                return effects()
                        .updateState(currentState().addFiles(res.fileNames()))
                        .transitionTo(SEND_FILES_FOR_IMPORT);
            }else{
                var msg = currentState().files().stream().map(f -> "%s/%s".formatted(f.fileName(),f.status())).collect(Collectors.joining(";"));
                logger.info("[{}] Nothing to add from listing. listing[{}], in process[{}]",locationId(),res,msg);
                return effects()
                        .updateState(currentState().end())
                        .transitionTo(SCHEDULE_NEXT_LIST);
            }
          }else{
            logger.info("[{}] Nothing to add",locationId());
            return effects()
                    .updateState(currentState().end())
                    .transitionTo(SCHEDULE_NEXT_LIST);
          }
      });

    var sendFilesForImport = step(SEND_FILES_FOR_IMPORT)
      .asyncCall(()->
          Source.from(currentState().files())
                  .mapAsync(currentState().files().size(),
                          file -> componentClient.forWorkflow(WorkflowId.fileImportWorkflowId(file.fileName(),currentState().folder()))
                                                   // COMMENT: just a heads-up that there is a limit on the number of
                                                   // concurrent workflows. Make sure you don't exceed it during a
                                                   // demo. Otherwise, you can throttle the workflow creation.
                                                  .method(FileImportWorkflow::start)
                                                  .invokeAsync(new ImportCommand.StartFileImport(file.fileName(),currentState().folder()))
                                                  .thenApply(ack -> new SendRes(file.fileName(),ack.error()))
                                                  .exceptionally(e -> {
                                                      //TODO move error handling via workflow failover
                                                      logger.error("Error importing file [{}]",locationId(), e);
                                                      return new SendRes(file.fileName(), Optional.of(e.getMessage()));
                                                  })
                  ).toMat(Sink.seq(), Keep.right())
                  .run(materializer)
                  .thenApply(SendResList::new)
      )
      .andThen(SendResList.class, resList -> {
          var update = effects().updateState(currentState().filesProcessStatus(resList.map()));
          if(settings.disableFileDelete){
              return update.transitionTo(SCHEDULE_NEXT_LIST);
          }else{
              return update.transitionTo(DELETE);
          }

      });

    var delete = step(DELETE)
    .asyncCall(()->
        Source.from(currentState().files())
                .flatMapConcat(file -> fileListSource.delete(file.fileName(), currentState().folder(), materializer))
                .toMat(Sink.head(),Keep.right())
                .run(materializer)
                .thenApply(list -> new DeleteRes(Optional.empty()))
                .exceptionally(e-> {
                    //TODO move error handling via workflow failover
                    logger.error("Error deleting files [{}]", locationId(), e);
                    return new DeleteRes(Optional.of(e.getMessage()));
                })
    )
    .andThen(DeleteRes.class, deleteRes ->
            effects()
                    .updateState(deleteRes.error().map(error -> currentState().filesDeleteError(error)).orElse(currentState().filesDeleted()))
                    .transitionTo(SCHEDULE_NEXT_LIST)
    );


    var scheduleNextList = step(SCHEDULE_NEXT_LIST)
            .asyncCall(()-> {
              var mySelf = componentClient.forWorkflow(locationId()).method(ListFilesWorkflow::listFiles).deferred();
              return timers().startSingleTimer("timer-%s".formatted(locationId()), settings.s3BucketListIntervalSeconds, mySelf);
            })
            .andThen(Done.class, d ->
                    effects()
                            .updateState(currentState().end())
                            .pause()
            );


    return workflow()
      .addStep(listStep, maxRetries(3).failoverTo(SCHEDULE_NEXT_LIST))
      .addStep(sendFilesForImport, maxRetries(3).failoverTo(SCHEDULE_NEXT_LIST))
      .addStep(delete, maxRetries(3).failoverTo(SCHEDULE_NEXT_LIST))
      .addStep(scheduleNextList);
  }

  private record ListedFileNames(List<String> fileNames){}
  private record SendRes(String fileName, Optional<String> error){}
  private record SendResList(List<SendRes> sendRes){
    public Map<String, Optional<String>> map(){
      return sendRes.stream().collect(Collectors.toMap(SendRes::fileName, SendRes::error));
    }
  }
  private record DeleteRes(Optional<String> error){}



}
