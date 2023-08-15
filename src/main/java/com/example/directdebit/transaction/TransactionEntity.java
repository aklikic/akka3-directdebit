package com.example.directdebit.transaction;

import com.example.directdebit.payment.PaymentDomain;
import com.example.directdebit.payment.PaymentEntity;
import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your com/example/directdebit/transaction/transaction_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionEntity extends AbstractTransactionEntity {
  private static Logger logger = LoggerFactory.getLogger(TransactionEntity.class);
  public static final String ERROR_WRONG_STATUS = "Wrong status";

  @SuppressWarnings("unused")
  private final String entityId;
  private final int transactionDebitDelaySeconds;

  public TransactionEntity(EventSourcedEntityContext context, int transactionDebitDelaySeconds) {
    this.entityId = context.entityId();
    this.transactionDebitDelaySeconds = transactionDebitDelaySeconds;
  }

  @Override
  public TransactionDomain.TransactionState emptyState() {
    return TransactionDomain.TransactionState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> create(TransactionDomain.TransactionState currentState, TransactionApi.CreateCommand createCommand) {
    logger.info("create: {}/{}",entityId,createCommand);
    switch (currentState.getStatus()){
      case CREATED:
        return effects().reply(Empty.getDefaultInstance());
      case UNKNOWN:
        TransactionDomain.Created event = TransactionDomain.Created.newBuilder()
                .setTransId(entityId)
                .setPaymentId(createCommand.getPaymentId())
                .setDebitAmount(createCommand.getDebitAmount())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().error(ERROR_WRONG_STATUS);
    }
  }

  @Override
  public Effect<Empty> initialize(TransactionDomain.TransactionState currentState, TransactionApi.InitializeCommand initializeCommand) {
    logger.info("initialize: {}/{}",entityId,initializeCommand);
    switch (currentState.getStatus()){
      case INITIALIZED:
        return effects().reply(Empty.getDefaultInstance());
      case CREATED:
        TransactionDomain.Initialized event = TransactionDomain.Initialized.newBuilder()
                .setTransId(entityId)
                .setScheduledAfterSec(transactionDebitDelaySeconds)
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().error(ERROR_WRONG_STATUS);
    }
  }

  @Override
  public Effect<Empty> triggerDebit(TransactionDomain.TransactionState currentState, TransactionApi.TriggerDebitCommand triggerDebitCommand) {
    logger.info("triggerDebit: {}/{}",entityId,triggerDebitCommand);
    switch (currentState.getStatus()){
      case INITIALIZED:
        TransactionDomain.DebitStarted event = TransactionDomain.DebitStarted.newBuilder()
                .setTransId(entityId)
                .setPaymentId(currentState.getPaymentId())
                .setDebitAmount(currentState.getDebitAmount())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<Empty> debitSuccessful(TransactionDomain.TransactionState currentState, TransactionApi.DebitSuccessfulCommand debitSuccessfulCommand) {
    logger.info("debitSuccessful: {}/{}",entityId,debitSuccessfulCommand);
    switch (currentState.getStatus()){
      case DEBIT_STARTED:
        TransactionDomain.Debited event = TransactionDomain.Debited.newBuilder()
                .setTransId(entityId)
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<Empty> debitFail(TransactionDomain.TransactionState currentState, TransactionApi.DebitFailCommand debitFailCommand) {
    logger.info("debitFail: {}/{}",entityId,debitFailCommand);
    switch (currentState.getStatus()){
      case DEBIT_STARTED:
        TransactionDomain.DebitFailed event = TransactionDomain.DebitFailed.newBuilder()
                .setTransId(entityId)
                .setReason(debitFailCommand.getReason())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<TransactionDomain.TransactionState> getTransactionState(TransactionDomain.TransactionState currentState, TransactionApi.GetTransactionStateCommand getTransactionStateCommand) {
    return effects().reply(currentState);
  }

  @Override
  public TransactionDomain.TransactionState created(TransactionDomain.TransactionState currentState, TransactionDomain.Created created) {
    return TransactionDomain.TransactionState.newBuilder().setPaymentId(created.getPaymentId()).setDebitAmount(created.getDebitAmount()).setStatus(TransactionDomain.TransactionStatus.CREATED).build();
  }
  @Override
  public TransactionDomain.TransactionState initialized(TransactionDomain.TransactionState currentState, TransactionDomain.Initialized initialized) {
    return currentState.toBuilder().setStatus(TransactionDomain.TransactionStatus.INITIALIZED).build();
  }
  @Override
  public TransactionDomain.TransactionState debitStarted(TransactionDomain.TransactionState currentState, TransactionDomain.DebitStarted debitStarted) {
    return currentState.toBuilder().setStatus(TransactionDomain.TransactionStatus.DEBIT_STARTED).build();
  }
  @Override
  public TransactionDomain.TransactionState debited(TransactionDomain.TransactionState currentState, TransactionDomain.Debited debited) {
    return currentState.toBuilder().setStatus(TransactionDomain.TransactionStatus.DEBITED).build();
  }
  @Override
  public TransactionDomain.TransactionState debitFailed(TransactionDomain.TransactionState currentState, TransactionDomain.DebitFailed debitFailed) {
    return currentState.toBuilder().setStatus(TransactionDomain.TransactionStatus.DEBIT_FAILED).build();
  }

}
