package com.example.directdebit.payment;

import com.example.directdebit.transaction.TransactionDomain;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your com/example/directdebit/payment/payment_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class PaymentEntity extends AbstractPaymentEntity {

  private static Logger logger = LoggerFactory.getLogger(PaymentEntity.class);
  public static final String ERROR_WRONG_STATUS = "Wrong status";
  @SuppressWarnings("unused")
  private final String entityId;
  private final int paymentCreditDelaySeconds;

  public PaymentEntity(EventSourcedEntityContext context, int paymentCreditDelaySeconds) {
    this.entityId = context.entityId();
    this.paymentCreditDelaySeconds = paymentCreditDelaySeconds;
  }

  @Override
  public PaymentDomain.PaymentState emptyState() {
    return PaymentDomain.PaymentState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> create(PaymentDomain.PaymentState currentState, PaymentApi.CreateCommand createCommand) {
    logger.info("create: {}/{}",entityId,createCommand);
    switch (currentState.getStatus()){
      case CREATED:
        return effects().reply(Empty.getDefaultInstance());
      case UNKNOWN:
        PaymentDomain.Created event = PaymentDomain.Created.newBuilder()
                .setPaymentId(entityId)
                .setCreditAmount(createCommand.getCreditAmount())
                .addAllTransactions(createCommand.getTransactionsList())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().error(ERROR_WRONG_STATUS);
    }

  }

  @Override
  public Effect<Empty> initialize(PaymentDomain.PaymentState currentState, PaymentApi.InitializeCommand initializeCommand) {
    logger.info("initialize: {}/{}",entityId,initializeCommand);
    switch (currentState.getStatus()){
      case INITIALIZED:
        return effects().reply(Empty.getDefaultInstance());
      case CREATED:
        PaymentDomain.Initialized event = PaymentDomain.Initialized.newBuilder()
                .setPaymentId(entityId)
                .setScheduledAfterSec(paymentCreditDelaySeconds)
                .addAllTransIds(currentState.getTransactionsList().stream().map(PaymentDomain.Transaction::getTransId).collect(Collectors.toList()))
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().error(ERROR_WRONG_STATUS);
    }
  }

  @Override
  public Effect<Empty> triggerCredit(PaymentDomain.PaymentState currentState, PaymentApi.TriggerCreditCommand triggerCreditCommand) {
    logger.info("triggerCredit: {}/{}",entityId,triggerCreditCommand);
    switch (currentState.getStatus()){
      case INITIALIZED:
        PaymentDomain.CreditStarted event = PaymentDomain.CreditStarted.newBuilder()
                .setPaymentId(entityId)
                .setCreditAmount(currentState.getCreditAmount())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<Empty> creditSuccessful(PaymentDomain.PaymentState currentState, PaymentApi.CreditSuccessfulCommand creditSuccessfulCommand) {
    logger.info("creditSuccessful: {}/{}",entityId,creditSuccessfulCommand);
    switch (currentState.getStatus()){
      case CREDIT_STARTED:
        PaymentDomain.Credited event = PaymentDomain.Credited.newBuilder()
                .setPaymentId(entityId)
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<Empty> creditFail(PaymentDomain.PaymentState currentState, PaymentApi.CreditFailCommand creditFailCommand) {
    logger.info("creditFail: {}/{}",entityId,creditFailCommand);
    switch (currentState.getStatus()){
      case CREDIT_STARTED:
        PaymentDomain.CreditFailed event = PaymentDomain.CreditFailed.newBuilder()
                .setPaymentId(entityId)
                .setReason(creditFailCommand.getReason())
                .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();
        return effects().emitEvent(event).thenReply(updatedState -> Empty.getDefaultInstance());
      default:
        return effects().reply(Empty.getDefaultInstance());
    }
  }

  @Override
  public Effect<PaymentDomain.PaymentState> getPaymentState(PaymentDomain.PaymentState currentState, PaymentApi.GetPaymentStateCommand getPaymentStateCommand) {
    return effects().reply(currentState());
  }

  @Override
  public PaymentDomain.PaymentState created(PaymentDomain.PaymentState currentState, PaymentDomain.Created created) {
    return PaymentDomain.PaymentState.newBuilder()
            .setCreditAmount(created.getCreditAmount())
            .addAllTransactions(created.getTransactionsList())
            .setStatus(PaymentDomain.PaymentStatus.CREATED).build();
  }
  @Override
  public PaymentDomain.PaymentState initialized(PaymentDomain.PaymentState currentState, PaymentDomain.Initialized initialized) {
    return currentState.toBuilder().setStatus(PaymentDomain.PaymentStatus.INITIALIZED).build();
  }
  @Override
  public PaymentDomain.PaymentState creditStarted(PaymentDomain.PaymentState currentState, PaymentDomain.CreditStarted creditStarted) {
    return currentState.toBuilder().setStatus(PaymentDomain.PaymentStatus.CREDIT_STARTED).build();
  }
  @Override
  public PaymentDomain.PaymentState credited(PaymentDomain.PaymentState currentState, PaymentDomain.Credited credited) {
    return currentState.toBuilder().setStatus(PaymentDomain.PaymentStatus.CREDITED).build();
  }
  @Override
  public PaymentDomain.PaymentState creditFailed(PaymentDomain.PaymentState currentState, PaymentDomain.CreditFailed creditFailed) {
    return currentState.toBuilder().setStatus(PaymentDomain.PaymentStatus.CREDIT_FAILED).build();
  }

}
