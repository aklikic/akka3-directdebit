package com.example.directdebit.transaction;

import com.google.protobuf.Any;
import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the View Service described in your com/example/directdebit/transaction/transaction_by_payment_and_status_view.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionByPaymentAndStatusViewImpl extends AbstractTransactionByPaymentAndStatusView {

  private static Logger logger = LoggerFactory.getLogger(TransactionByPaymentAndStatusViewImpl.class);
  public TransactionByPaymentAndStatusViewImpl(ViewContext context) {}

  @Override
  public TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord emptyState() {
    return TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> onCreated(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      TransactionDomain.Created created) {
    logger.info("onCreated: {}",created);
    return effects().updateState(
            TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord.newBuilder()
            .setPaymentId(created.getPaymentId())
            .setStatus(TransactionDomain.TransactionStatus.CREATED)
            .setStatusId(TransactionDomain.TransactionStatus.CREATED.getNumber())
            .setTransId(created.getTransId())
            .setLastUpdateTimestamp(created.getEventTimestamp())
            .build()
    );
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> onInitialized(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      TransactionDomain.Initialized initialized) {
    logger.info("onInitialized: {}",initialized);
    return effects().updateState(
            state.toBuilder()
                    .setStatus(TransactionDomain.TransactionStatus.INITIALIZED)
                    .setStatusId(TransactionDomain.TransactionStatus.INITIALIZED.getNumber())
                    .build()
    );
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> onDebitStarted(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      TransactionDomain.DebitStarted debitStarted) {
    logger.info("onDebitStarted: {}",debitStarted);
    return effects().updateState(
            state.toBuilder()
                    .setStatus(TransactionDomain.TransactionStatus.DEBIT_STARTED)
                    .setStatusId(TransactionDomain.TransactionStatus.DEBIT_STARTED.getNumber())
                    .build()
    );
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> onDebited(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      TransactionDomain.Debited debited) {
    logger.info("onDebited: {}",debited);
    return effects().updateState(
            state.toBuilder()
                    .setStatus(TransactionDomain.TransactionStatus.DEBITED)
                    .setStatusId(TransactionDomain.TransactionStatus.DEBITED.getNumber())
                    .build()
    );
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> onDebitFailed(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      TransactionDomain.DebitFailed debitFailed) {
    logger.info("onDebitFail: {}",debitFailed);
    return effects().updateState(
            state.toBuilder()
                    .setStatus(TransactionDomain.TransactionStatus.DEBIT_FAILED)
                    .setStatusId(TransactionDomain.TransactionStatus.DEBIT_FAILED.getNumber())
                    .build()
    );
  }

  @Override
  public View.UpdateEffect<TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord> ignoreOtherEvents(
      TransactionByPaymentAndStatus.TransactionByPaymentAndStatusRecord state,
      Any any) {
    return effects().ignore();
  }

}

