package com.example.directdebit.payment;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your com/example/directdebit/payment/payment_processor_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class PaymentProcessorActionImpl extends AbstractPaymentProcessorAction {

  private static Logger logger = LoggerFactory.getLogger(PaymentProcessorActionImpl.class);
  public PaymentProcessorActionImpl(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onInitialized(PaymentDomain.Initialized initialized) {
    logger.info("onInitialized: {}",initialized);
    var triggerCreditCall = components().paymentEntity().triggerCredit(PaymentApi.TriggerCreditCommand.newBuilder().setPaymentId(initialized.getPaymentId()).build());
    var asyncReply = timers().startSingleTimer(initialized.getPaymentId(), Duration.of(initialized.getScheduledAfterSec(), ChronoUnit.SECONDS),triggerCreditCall)
            .thenApply(t-> Empty.getDefaultInstance());
    return effects().asyncReply(asyncReply);
  }
  @Override
  public Effect<Empty> onCreditStarted(PaymentDomain.CreditStarted creditStarted) {
    logger.info("onCreditStarted: {}",creditStarted);
    var asyncReply = components().paymentEntity().creditSuccessful(PaymentApi.CreditSuccessfulCommand.newBuilder().setPaymentId(creditStarted.getPaymentId()).build())
            .execute()
            .thenApply(r -> Empty.getDefaultInstance());
    return effects().asyncReply(asyncReply);
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
