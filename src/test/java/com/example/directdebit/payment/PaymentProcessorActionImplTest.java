package com.example.directdebit.payment;

import akka.stream.javadsl.Source;
import com.example.directdebit.payment.PaymentDomain;
import com.example.directdebit.payment.PaymentProcessorActionImpl;
import com.example.directdebit.payment.PaymentProcessorActionImplTestKit;
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

public class PaymentProcessorActionImplTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    PaymentProcessorActionImplTestKit service = PaymentProcessorActionImplTestKit.of(PaymentProcessorActionImpl::new);

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
    PaymentProcessorActionImplTestKit testKit = PaymentProcessorActionImplTestKit.of(PaymentProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.onInitialized(PaymentDomain.Initialized.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void onCreditStartedTest() {
    PaymentProcessorActionImplTestKit testKit = PaymentProcessorActionImplTestKit.of(PaymentProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.onCreditStarted(PaymentDomain.CreditStarted.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    PaymentProcessorActionImplTestKit testKit = PaymentProcessorActionImplTestKit.of(PaymentProcessorActionImpl::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}
