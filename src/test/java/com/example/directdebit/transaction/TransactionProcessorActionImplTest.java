package com.example.directdebit.transaction;

import akka.stream.javadsl.Source;
import com.example.directdebit.transaction.TransactionDomain;
import com.example.directdebit.transaction.TransactionProcessorActionImpl;
import com.example.directdebit.transaction.TransactionProcessorActionImplTestKit;
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

public class TransactionProcessorActionImplTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    TransactionProcessorActionImplTestKit service = TransactionProcessorActionImplTestKit.of(TransactionProcessorActionImpl::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onInitializedTest() {
    TransactionProcessorActionImplTestKit testKit = TransactionProcessorActionImplTestKit.of(TransactionProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.onInitialized(TransactionDomain.Initialized.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void onDebitStartedTest() {
    TransactionProcessorActionImplTestKit testKit = TransactionProcessorActionImplTestKit.of(TransactionProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.onDebitStarted(TransactionDomain.DebitStarted.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    TransactionProcessorActionImplTestKit testKit = TransactionProcessorActionImplTestKit.of(TransactionProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}
