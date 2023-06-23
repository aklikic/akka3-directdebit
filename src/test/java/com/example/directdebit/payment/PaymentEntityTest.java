package com.example.directdebit.payment;

import com.google.protobuf.Empty;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.javasdk.testkit.EventSourcedResult;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class PaymentEntityTest {

  @Test
  public void exampleTest() {
    var paymentCreditDelaySeconds = 1;
    var paymentId = "p1";
    var transIds = List.of("t1#%s".formatted(paymentId),"t2#%s".formatted(paymentId));
    var creditAmount = 100;
    var service = PaymentEntityTestKit.of(paymentId, ctx -> new PaymentEntity(ctx, paymentCreditDelaySeconds));
    //create
    var createCmd = PaymentApi.CreateCommand.newBuilder()
            .setPaymentId(paymentId)
            .setCreditAmount(creditAmount)
            .addAllTransactions(transIds.stream().map(transId -> PaymentDomain.Transaction.newBuilder().setTransId(transId).build()).collect(Collectors.toList()))
            .build();
    var createRes = service.create(createCmd);
    var createdEvent = createRes.getNextEventOfType(PaymentDomain.Created.class);
    var state = (PaymentDomain.PaymentState) createRes.getUpdatedState();

    assertEquals(paymentId,createdEvent.getPaymentId());
    assertEquals(transIds.size(),createdEvent.getTransactionsCount());
    assertEquals(PaymentDomain.PaymentStatus.CREATED, state.getStatus());

    //initialize
    var initCmd = PaymentApi.InitializeCommand.newBuilder()
            .setPaymentId(paymentId)
            .build();
    var initRes = service.initialize(initCmd);
    var initEvent = initRes.getNextEventOfType(PaymentDomain.Initialized.class);
    state = (PaymentDomain.PaymentState) initRes.getUpdatedState();

    assertEquals(paymentId,initEvent.getPaymentId());
    assertEquals(PaymentDomain.PaymentStatus.INITIALIZED, state.getStatus());

    //trigger credit
    var triggerCmd = PaymentApi.TriggerCreditCommand.newBuilder()
            .setPaymentId(paymentId)
            .build();
    var triggerRes = service.triggerCredit(triggerCmd);
    var creditStartedEvent = triggerRes.getNextEventOfType(PaymentDomain.CreditStarted.class);
    state = (PaymentDomain.PaymentState) triggerRes.getUpdatedState();

    assertEquals(paymentId,creditStartedEvent.getPaymentId());
    assertEquals(PaymentDomain.PaymentStatus.CREDIT_STARTED, state.getStatus());

    //credited
    var creditSucCmd = PaymentApi.CreditSuccessfulCommand.newBuilder()
            .setPaymentId(paymentId)
            .build();
    var creditSucRes = service.creditSuccessful(creditSucCmd);
    var creditedEvent = creditSucRes.getNextEventOfType(PaymentDomain.Credited.class);
    state = (PaymentDomain.PaymentState) creditSucRes.getUpdatedState();

    assertEquals(paymentId,creditedEvent.getPaymentId());
    assertEquals(PaymentDomain.PaymentStatus.CREDITED, state.getStatus());

  }
}
