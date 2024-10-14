package com.example.directdebit.transaction.domain;

import com.example.akka.directdebit.payment.api.TransactionCommand.Create;
import com.example.akka.directdebit.payment.domain.TransactionState;
import com.example.akka.directdebit.payment.domain.TransactionState.TransactionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class TransactionStateTest {

    @Test
    public void shouldCreateTransaction(){

        //given
        var transactionDebitDelaySeconds = 1;
        var paymentId = "p1";
        var transId = "t1#%s".formatted(paymentId);
        var debitAmount = 10;
        var state = TransactionState.empty();

        //when
        var result = state.handleCreate(transId, new Create(paymentId,debitAmount));
        assertTrue(result.error().isEmpty());
        assertEquals(1, result.events().size());
        var event = result.events().get(0);
        var updatedState = state.applyEvent(event);

        //then
        assertEquals(transId, updatedState.transId());
        assertEquals(TransactionStatus.CREATED, updatedState.status());
    }

    //TODO do more tests
}
