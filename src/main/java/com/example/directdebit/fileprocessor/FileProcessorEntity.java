package com.example.directdebit.fileprocessor;

import com.google.protobuf.Empty;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your com/example/directdebit/fileprocessor/fileprocessor_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FileProcessorEntity extends AbstractFileProcessorEntity {

  @SuppressWarnings("unused")
  private final String entityId;

  public FileProcessorEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public FileProcessorDomain.FileProcessorState emptyState() {
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty entity state");
  }

  @Override
  public Effect<Empty> start(FileProcessorDomain.FileProcessorState currentState, FileProcessorApi.StartCommand startCommand) {
    return effects().error("The command handler for `Start` is not implemented, yet");
  }

  @Override
  public Effect<Empty> finish(FileProcessorDomain.FileProcessorState currentState, FileProcessorApi.FinishCommand finishCommand) {
    return effects().error("The command handler for `Finish` is not implemented, yet");
  }

  @Override
  public Effect<FileProcessorDomain.FileProcessorState> getFileProcessorState(FileProcessorDomain.FileProcessorState currentState, FileProcessorApi.GetFileProcessorStateCommand getFileProcessorStateCommand) {
    return effects().error("The command handler for `GetFileProcessorState` is not implemented, yet");
  }

  @Override
  public FileProcessorDomain.FileProcessorState started(FileProcessorDomain.FileProcessorState currentState, FileProcessorDomain.Started started) {
    throw new RuntimeException("The event handler for `Started` is not implemented, yet");
  }
  @Override
  public FileProcessorDomain.FileProcessorState finished(FileProcessorDomain.FileProcessorState currentState, FileProcessorDomain.Finished finished) {
    throw new RuntimeException("The event handler for `Finished` is not implemented, yet");
  }

}
