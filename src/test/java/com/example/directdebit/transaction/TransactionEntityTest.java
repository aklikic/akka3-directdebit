package com.example.directdebit.transaction;


import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionEntityTest {

  @Test
  public void exampleTest() {
    var transactionDebitDelaySeconds = 1;

    var paymentId = "p1";
    var transId = "t1#%s".formatted(paymentId);
    var debitAmount = 10;
    TransactionEntityTestKit service = TransactionEntityTestKit.of(transId, ctx -> new TransactionEntity(ctx, transactionDebitDelaySeconds));



    //create
    var createCmd = TransactionApi.CreateCommand.newBuilder()
            .setTransId(transId)
            .setPaymentId(paymentId)
            .setDebitAmount(debitAmount)
            .build();
    var createRes = service.create(createCmd);
    var createdEvent = createRes.getNextEventOfType(TransactionDomain.Created.class);
    var state = (TransactionDomain.TransactionState) createRes.getUpdatedState();

    assertEquals(transId,createdEvent.getTransId());
    assertEquals(TransactionDomain.TransactionStatus.CREATED, state.getStatus());

    //initialize
    var initCmd = TransactionApi.InitializeCommand.newBuilder()
            .setTransId(transId)
            .build();
    var initRes = service.initialize(initCmd);
    var initEvent = initRes.getNextEventOfType(TransactionDomain.Initialized.class);
    state = (TransactionDomain.TransactionState) initRes.getUpdatedState();

    assertEquals(transId,initEvent.getTransId());
    assertEquals(TransactionDomain.TransactionStatus.INITIALIZED, state.getStatus());

    //trigger debit
    var triggerCmd = TransactionApi.TriggerDebitCommand.newBuilder()
            .setTransId(transId)
            .build();
    var triggerRes = service.triggerDebit(triggerCmd);
    var debitStartedEvent = triggerRes.getNextEventOfType(TransactionDomain.DebitStarted.class);
    state = (TransactionDomain.TransactionState) triggerRes.getUpdatedState();

    assertEquals(transId,initEvent.getTransId());
    assertEquals(TransactionDomain.TransactionStatus.DEBIT_STARTED, state.getStatus());

    //credited
    var debitSucCmd = TransactionApi.DebitSuccessfulCommand.newBuilder()
            .setTransId(transId)
            .build();
    var creditSucRes = service.debitSuccessful(debitSucCmd);
    var debitedEvent = creditSucRes.getNextEventOfType(TransactionDomain.Debited.class);
    state = (TransactionDomain.TransactionState) creditSucRes.getUpdatedState();

    assertEquals(transId,initEvent.getTransId());
    assertEquals(TransactionDomain.TransactionStatus.DEBITED, state.getStatus());
  }

}
