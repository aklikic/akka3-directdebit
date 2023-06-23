package com.example.directdebit.fileprocessor;

import com.google.protobuf.Empty;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.javasdk.testkit.EventSourcedResult;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FileProcessorEntityTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    FileProcessorEntityTestKit service = FileProcessorEntityTestKit.of(FileProcessorEntity::new);
    // // use the testkit to execute a command
    // // of events emitted, or a final updated state:
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // EventSourcedResult<SomeResponse> result = service.someOperation(command);
    // // verify the emitted events
    // ExpectedEvent actualEvent = result.getNextEventOfType(ExpectedEvent.class);
    // assertEquals(expectedEvent, actualEvent);
    // // verify the final state after applying the events
    // assertEquals(expectedState, service.getState());
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void startTest() {
    FileProcessorEntityTestKit service = FileProcessorEntityTestKit.of(FileProcessorEntity::new);
    // StartCommand command = StartCommand.newBuilder()...build();
    // EventSourcedResult<Empty> result = service.start(command);
  }


  @Test
  @Ignore("to be implemented")
  public void finishTest() {
    FileProcessorEntityTestKit service = FileProcessorEntityTestKit.of(FileProcessorEntity::new);
    // FinishCommand command = FinishCommand.newBuilder()...build();
    // EventSourcedResult<Empty> result = service.finish(command);
  }


  @Test
  @Ignore("to be implemented")
  public void getFileProcessorStateTest() {
    FileProcessorEntityTestKit service = FileProcessorEntityTestKit.of(FileProcessorEntity::new);
    // GetFileProcessorStateCommand command = GetFileProcessorStateCommand.newBuilder()...build();
    // EventSourcedResult<FileProcessorState> result = service.getFileProcessorState(command);
  }

}
