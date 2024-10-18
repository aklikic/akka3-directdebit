package com.example.akka.directdebit.payment.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.akka.directdebit.payment.MySettings;
import com.example.akka.directdebit.payment.api.TransactionCommand.Create;
import com.example.akka.directdebit.payment.api.TransactionCommand.SetDebitFailed;
import com.example.akka.directdebit.payment.api.TransactionCommandError;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.Ack;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.ApiTransactionState;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.ApiTransactionStatus;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.GetTransactionStateReply;
import com.example.akka.directdebit.payment.domain.TransactionEvent;
import com.example.akka.directdebit.payment.domain.TransactionState;
import com.example.akka.directdebit.util.StateCommandProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("transaction")
public class TransactionEntity extends EventSourcedEntity<TransactionState, TransactionEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String transId;
    private int transactionDebitDelaySeconds;

    public TransactionEntity(EventSourcedEntityContext context, MySettings myConfig) {
        this.transId = context.entityId();
        this.transactionDebitDelaySeconds = myConfig.transactionDebitDelaySeconds;
    }

    @Override
    public TransactionState emptyState() {
        return TransactionState.empty();
    }

    @Override
    public TransactionState applyEvent(TransactionEvent transactionEvent) {
        return currentState().applyEvent(transactionEvent);
    }

    public Effect<Ack> create(Create command){
        logger.info("create [{}]: {}",transId, command);
        return handleStateCommandProcessResult(currentState().handleCreate(transId,command));
    }

    public Effect<Ack> initialize(){
        logger.info("initialize [{}]",transId);
        return handleStateCommandProcessResult(currentState().handleInitialize(transId,transactionDebitDelaySeconds));
    }
    public Effect<Ack> startDebit(){
        logger.info("startDebit [{}]",transId);
        return handleStateCommandProcessResult(currentState().handleStartDebit(transId));
    }
    public Effect<Ack> setDebitFailed(SetDebitFailed command){
        logger.info("setDebitFailed [{}]: {}",transId, command);
        return handleStateCommandProcessResult(currentState().handleSetDebitFailed(transId,command));
    }
    public Effect<Ack> setDebited(){
        logger.info("setDebited [{}]",transId);
        return handleStateCommandProcessResult(currentState().handleSetDebited(transId));
    }

    public ReadOnlyEffect<GetTransactionStateReply> getTransactionState(){
        logger.info("getTransactionState [{}]",transId);
        if(currentState().isEmpty())
            return effects().reply(GetTransactionStateReply.error(TransactionCommandError.TRANSACTION_NOT_FOUND));
        else
            return effects().reply(GetTransactionStateReply.ok(new ApiTransactionState(transId, currentState().paymentId(), currentState().debitAmountCents(), toApi(currentState().status()))));
    }

    private static ApiTransactionStatus toApi(TransactionState.TransactionStatus status){
        return switch (status){
            case UNKNOWN -> ApiTransactionStatus.UNKNOWN;
            case CREATED -> ApiTransactionStatus.CREATED;
            case INITIALIZED -> ApiTransactionStatus.INITIALIZED;
            case DEBIT_STARTED -> ApiTransactionStatus.DEBIT_STARTED;
            case DEBITED -> ApiTransactionStatus.DEBITED;
            case DEBIT_FAILED -> ApiTransactionStatus.DEBIT_FAILED;
        };
    }

    private Effect<Ack> handleStateCommandProcessResult(StateCommandProcessResult<TransactionEvent, TransactionCommandError> result){
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
