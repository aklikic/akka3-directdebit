package com.example.akka.directdebit.payment.application;


import akka.javasdk.testkit.EventSourcedTestKit;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.api.PaymentCommand.Create;
import com.example.akka.directdebit.payment.domain.PaymentEvent;
import com.example.akka.directdebit.payment.domain.PaymentEvent.*;
import com.example.akka.directdebit.payment.domain.PaymentState;
import com.example.akka.directdebit.payment.domain.PaymentState.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PaymentEntityTest {

  @Test
  public void happyPathTest() {
    var paymentCreditDelaySeconds = 1;

    var paymentId = "p1";
    var creditAmount = 10;
    EventSourcedTestKit<PaymentState, PaymentEvent, PaymentEntity> testKit = EventSourcedTestKit.of(paymentId, ctx -> new PaymentEntity(ctx, new MySettings(paymentCreditDelaySeconds,1,1,null)));


    //create
    var createCmd = new Create(creditAmount);
    var createRes = testKit.call(e -> e.create(createCmd));
    var createdEvent = createRes.getNextEventOfType(Created.class);
    var state = (PaymentState)createRes.getUpdatedState();

    assertEquals(paymentId,createdEvent.paymentId());
    assertEquals(PaymentStatus.CREATED, state.status());

    //initialize
    var initRes = testKit.call(e -> e.initialize());
    var initEvent = initRes.getNextEventOfType(Initialized.class);
    state = (PaymentState) initRes.getUpdatedState();

    assertEquals(paymentId,initEvent.paymentId());
    assertEquals(PaymentStatus.INITIALIZED, state.status());

    //trigger debit
    var triggerRes =  testKit.call(e -> e.startCredit());
    var debitStartedEvent = triggerRes.getNextEventOfType(CreditStarted.class);
    state = (PaymentState) triggerRes.getUpdatedState();

    assertEquals(paymentId,initEvent.paymentId());
    assertEquals(PaymentStatus.CREDIT_STARTED, state.status());

    //credited
    var creditSucRes = testKit.call(e -> e.setCredited());
    var debitedEvent = creditSucRes.getNextEventOfType(Credited.class);
    state = (PaymentState) creditSucRes.getUpdatedState();

    assertEquals(paymentId,initEvent.paymentId());
    assertEquals(PaymentStatus.CREDITED, state.status());
  }

  //TODO do more tests

}
