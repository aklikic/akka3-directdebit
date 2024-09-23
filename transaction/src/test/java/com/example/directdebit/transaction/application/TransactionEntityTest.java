package com.example.directdebit.transaction.application;


import akka.javasdk.testkit.EventSourcedTestKit;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.api.TransactionCommand.*;
import com.example.akka.directdebit.payment.application.TransactionEntity;
import com.example.akka.directdebit.payment.domain.TransactionEvent;
import com.example.akka.directdebit.payment.domain.TransactionEvent.*;
import com.example.akka.directdebit.payment.domain.TransactionState;
import com.example.akka.directdebit.payment.domain.TransactionState.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



public class TransactionEntityTest {

  @Test
  public void happyPathTest() {
    var transactionDebitDelaySeconds = 1;

    var paymentId = "p1";
    var transId = "t1#%s".formatted(paymentId);
    var debitAmount = 10;
    EventSourcedTestKit<TransactionState, TransactionEvent, TransactionEntity> testKit = EventSourcedTestKit.of(transId, ctx -> new TransactionEntity(ctx, new MySettings(transactionDebitDelaySeconds)));


    //create
    var createCmd = new Create(paymentId, debitAmount);
    var createRes = testKit.call(e -> e.create(createCmd));
    var createdEvent = createRes.getNextEventOfType(Created.class);
    var state = (TransactionState)createRes.getUpdatedState();

    assertEquals(transId,createdEvent.transId());
    assertEquals(TransactionStatus.CREATED, state.status());

    //initialize
    var initRes = testKit.call(e -> e.initialize());
    var initEvent = initRes.getNextEventOfType(Initialized.class);
    state = (TransactionState) initRes.getUpdatedState();

    assertEquals(transId,initEvent.transId());
    assertEquals(TransactionStatus.INITIALIZED, state.status());

    //trigger debit
    var triggerRes =  testKit.call(e -> e.startDebit());
    var debitStartedEvent = triggerRes.getNextEventOfType(DebitStarted.class);
    state = (TransactionState) triggerRes.getUpdatedState();

    assertEquals(transId,initEvent.transId());
    assertEquals(TransactionStatus.DEBIT_STARTED, state.status());

    //credited
    var creditSucRes = testKit.call(e -> e.setDebited());
    var debitedEvent = creditSucRes.getNextEventOfType(Debited.class);
    state = (TransactionState) creditSucRes.getUpdatedState();

    assertEquals(transId,initEvent.transId());
    assertEquals(TransactionStatus.DEBITED, state.status());
  }

  //TODO do more tests

}
