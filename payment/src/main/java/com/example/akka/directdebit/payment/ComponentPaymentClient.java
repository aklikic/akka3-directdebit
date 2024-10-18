package com.example.akka.directdebit.payment;

import akka.javasdk.client.ComponentClient;
import com.example.akka.directdebit.payment.api.*;
import com.example.akka.directdebit.payment.application.PaymentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

//inject ComponentClient componentClient
public record ComponentPaymentClient(ComponentClient componentClient) implements PaymentClient {
    private static final Logger logger = LoggerFactory.getLogger(ComponentPaymentClient.class);

    public CompletionStage<PaymentCommandResponse.Ack> create(String paymentId, PaymentCommand.Create command){
        logger.info("create [{}]:{}",paymentId,command);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::create).invokeAsync(command);
    }
    public CompletionStage<PaymentCommandResponse.Ack> initialize(String paymentId){
        logger.info("initialize [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::initialize).invokeAsync();
    }
    public CompletionStage<PaymentCommandResponse.Ack> setCredited(String paymentId){
        logger.info("setCredited [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::setCredited).invokeAsync();
    }
    public CompletionStage<PaymentCommandResponse.Ack> setCreditFailed(String paymentId, PaymentCommand.SetCreditFailed command){
        logger.info("setCreditFailed [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::setCreditFailed).invokeAsync(command);
    }
    public CompletionStage<PaymentCommandResponse.GetPaymentStateReply> getPaymentState (String paymentId){
        logger.info("get [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::getPaymentState).invokeAsync();
    }
}
