package com.example.akka.directdebit.payment.domain;

import com.example.akka.directdebit.payment.api.PaymentCommand.Create;
import com.example.akka.directdebit.payment.domain.PaymentState;
import com.example.akka.directdebit.payment.domain.PaymentState.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class PaymentStateTest {

    @Test
    public void shouldCreateTransaction(){

        //given
        var paymentCreditDelaySeconds = 1;
        var paymentId = "p1";
        var debitAmount = 10;
        var state = PaymentState.empty();

        //when
        var result = state.handleCreate(paymentId, new Create(debitAmount));
        assertTrue(result.error().isEmpty());
        assertEquals(1, result.events().size());
        var event = result.events().get(0);
        var updatedState = state.applyEvent(event);

        //then
        assertEquals(paymentId, updatedState.paymentId());
        assertEquals(PaymentStatus.CREATED, updatedState.status());
    }

    //TODO do more tests
}
