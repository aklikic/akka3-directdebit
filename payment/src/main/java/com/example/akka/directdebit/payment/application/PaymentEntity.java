package com.example.akka.directdebit.payment.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.api.PaymentCommandError;
import com.example.akka.directdebit.payment.api.PaymentCommandResponse.*;
import com.example.akka.directdebit.payment.api.PaymentCommand.*;
import com.example.akka.directdebit.payment.domain.PaymentEvent;
import com.example.akka.directdebit.payment.domain.PaymentState;
import com.example.akka.directdebit.util.StateCommandProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("payment")
public class PaymentEntity extends EventSourcedEntity<PaymentState, PaymentEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String paymentId;
    private int paymentCreditDelaySeconds;

    public PaymentEntity(EventSourcedEntityContext context, MySettings myConfig) {
        this.paymentId = context.entityId();
        this.paymentCreditDelaySeconds = myConfig.paymentCreditDelaySeconds;
    }

    @Override
    public PaymentState emptyState() {
        return PaymentState.empty();
    }

    @Override
    public PaymentState applyEvent(PaymentEvent transactionEvent) {
        return currentState().applyEvent(transactionEvent);
    }

    public Effect<Ack> create(Create command){
        logger.info("create [{}]: {}",paymentId, command);
        return handleStateCommandProcessResult(currentState().handleCreate(paymentId,command));
    }

    public Effect<Ack> initialize(){
        logger.info("initialize [{}]",paymentId);
        return handleStateCommandProcessResult(currentState().handleInitialize(paymentId,paymentCreditDelaySeconds));
    }
    public Effect<Ack> startCredit(){
        logger.info("startCredit [{}]",paymentId);
        return handleStateCommandProcessResult(currentState().handleStartCredit(paymentId));
    }
    public Effect<Ack> setCreditFailed(SetCreditFailed command){
        logger.info("setCreditFailed [{}]: {}",paymentId, command);
        return handleStateCommandProcessResult(currentState().handleSetCreditFailed(paymentId,command));
    }
    public Effect<Ack> setCredited(){
        logger.info("setCredited [{}]",paymentId);
        return handleStateCommandProcessResult(currentState().handleSetCredited(paymentId));
    }

    public Effect<GetPaymentStateReply> getPaymentState(){
        logger.info("getPaymentState [{}]",paymentId);
        if(currentState().isEmpty())
            return effects().reply(GetPaymentStateReply.error(PaymentCommandError.PAYMENT_NOT_FOUND));
        else
            return effects().reply(GetPaymentStateReply.ok(new ApiPaymentState(paymentId, currentState().creditAmountCents(), toApi(currentState().status()))));
    }

    private static ApiPaymentStatus toApi(PaymentState.PaymentStatus status){
        return switch (status){
            case UNKNOWN -> ApiPaymentStatus.UNKNOWN;
            case CREATED -> ApiPaymentStatus.CREATED;
            case INITIALIZED -> ApiPaymentStatus.INITIALIZED;
            case CREDIT_STARTED -> ApiPaymentStatus.CREDIT_STARTED;
            case CREDITED -> ApiPaymentStatus.CREDITED;
            case CREDIT_FAILED -> ApiPaymentStatus.CREDIT_FAILED;
        };
    }

    private Effect<Ack> handleStateCommandProcessResult(StateCommandProcessResult<PaymentEvent, PaymentCommandError> result){
        if(!result.events().isEmpty()){
            return effects().persistAll(result.events())
                    .thenReply(updateState ->
                            result.error()
                                    .map(error -> Ack.error(error))
                                    .orElse(Ack.ok())
                    );
        }else{
            return effects().reply(
                    result.error()
                            .map(error -> Ack.error(error))
                            .orElse(Ack.ok())
            );
        }
    }
}
