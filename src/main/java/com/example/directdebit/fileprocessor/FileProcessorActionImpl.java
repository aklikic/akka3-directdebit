package com.example.directdebit.fileprocessor;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import kalix.javasdk.action.ActionCreationContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your com/example/directdebit/fileprocessor/file_processor_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FileProcessorActionImpl extends AbstractFileProcessorAction {

  public FileProcessorActionImpl(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onStarted(FileProcessorDomain.Started started) {
    throw new RuntimeException("The command handler for `OnStarted` is not implemented, yet");
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    throw new RuntimeException("The command handler for `IgnoreOtherEvents` is not implemented, yet");
  }
}
