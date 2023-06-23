package com.example.directdebit.transaction;

import com.example.directdebit.payment.PaymentApi;
import com.example.directdebit.payment.PaymentProcessorActionImpl;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your com/example/directdebit/transaction/transaction_processor_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionProcessorActionImpl extends AbstractTransactionProcessorAction {
  private static Logger logger = LoggerFactory.getLogger(TransactionProcessorActionImpl.class);
  public TransactionProcessorActionImpl(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onInitialized(TransactionDomain.Initialized initialized) {
    logger.info("onInitialized: {}",initialized);
    var triggerDebitCall = components().transactionEntity().triggerDebit(TransactionApi.TriggerDebitCommand.newBuilder().setTransId(initialized.getTransId()).build());
    var asyncReply = timers().startSingleTimer(initialized.getTransId(), Duration.of(initialized.getScheduledAfterSec(), ChronoUnit.SECONDS),triggerDebitCall)
            .thenApply(t-> Empty.getDefaultInstance());
    return effects().asyncReply(asyncReply);
  }
  @Override
  public Effect<Empty> onDebitStarted(TransactionDomain.DebitStarted debitStarted) {
    logger.info("onDebitStarted: {}",debitStarted);
    var asyncReply = components().transactionEntity().debitSuccessful(TransactionApi.DebitSuccessfulCommand.newBuilder().setTransId(debitStarted.getTransId()).build())
            .execute()
            .thenApply(r -> Empty.getDefaultInstance());
    return effects().asyncReply(asyncReply);
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
