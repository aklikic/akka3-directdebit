package com.example.akka.directdebit.payment.api;

import java.util.concurrent.CompletionStage;

public interface PaymentClient {
    CompletionStage<PaymentCommandResponse.Ack> create(String paymentId, PaymentCommand.Create command);
    CompletionStage<PaymentCommandResponse.Ack> initialize(String paymentId);
    CompletionStage<PaymentCommandResponse.Ack> setCredited(String paymentId);
    CompletionStage<PaymentCommandResponse.Ack> setCreditFailed(String paymentId, PaymentCommand.SetCreditFailed command);
    CompletionStage<PaymentCommandResponse.GetPaymentStateReply> getPaymentState (String paymentId);
}
