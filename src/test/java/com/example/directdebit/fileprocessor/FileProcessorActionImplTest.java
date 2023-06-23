package com.example.directdebit.fileprocessor;

import akka.stream.javadsl.Source;
import com.example.directdebit.fileprocessor.FileProcessorActionImpl;
import com.example.directdebit.fileprocessor.FileProcessorActionImplTestKit;
import com.example.directdebit.fileprocessor.FileProcessorDomain;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FileProcessorActionImplTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    FileProcessorActionImplTestKit service = FileProcessorActionImplTestKit.of(FileProcessorActionImpl::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onStartedTest() {
    FileProcessorActionImplTestKit testKit = FileProcessorActionImplTestKit.of(FileProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.onStarted(FileProcessorDomain.Started.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    FileProcessorActionImplTestKit testKit = FileProcessorActionImplTestKit.of(FileProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}
