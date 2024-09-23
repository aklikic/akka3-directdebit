package com.example.akka.directdebit.payment.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.akka.directdebit.payment.api.TransactionByPaymentAndStatusViewModel.*;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse;
import com.example.akka.directdebit.payment.domain.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Record;

@ComponentId("view_transactions_by_payment_and_status")
public class TransactionByPaymentAndStatusView extends View {

    private static final Logger logger = LoggerFactory.getLogger(TransactionByPaymentAndStatusView.class);

    @Table("transaction_by_payment_and_status")
    @Consume.FromEventSourcedEntity(TransactionEntity.class) // <4>
    public static class FromTransactionEntityUpdater extends TableUpdater<ViewRecord> {
        public Effect<ViewRecord> onEvent(TransactionEvent event) {
            logger.info("onEvent: {}",event);
           return switch (event){
               case TransactionEvent.Created evt -> effects().updateRow(new ViewRecord(evt.paymentId(),
                       TransactionCommandResponse.ApiTransactionStatus.CREATED.name(),
                       evt.transId(),
                       evt.timestamp()));
               case TransactionEvent.Initialized evt ->  effects().updateRow(new ViewRecord(rowState().paymentId(),
                       TransactionCommandResponse.ApiTransactionStatus.INITIALIZED.name(),
                       rowState().transId(),
                       evt.timestamp()));
               case TransactionEvent.DebitStarted evt ->  effects().updateRow(new ViewRecord(rowState().paymentId(),
                       TransactionCommandResponse.ApiTransactionStatus.DEBIT_STARTED.name(),
                       rowState().transId(),
                       evt.timestamp()));
               case TransactionEvent.Debited evt ->  effects().updateRow(new ViewRecord(rowState().paymentId(),
                       TransactionCommandResponse.ApiTransactionStatus.DEBITED.name(),
                       rowState().transId(),
                       evt.timestamp()));
               case TransactionEvent.DebitFailed evt ->  effects().updateRow(new ViewRecord(rowState().paymentId(),
                       TransactionCommandResponse.ApiTransactionStatus.DEBIT_FAILED.name(),
                       rowState().transId(),
                       evt.timestamp()));
           };
        }
    }

    @Query("SELECT * AS records FROM transaction_by_payment_and_status WHERE paymentId = :paymentId AND statusId = :statusId")
    public QueryEffect<ViewRecordList> getRecordList(QueryRequest queryRequest) {
        return queryResult();
    }
    @Query("SELECT * FROM transaction_by_payment_and_status WHERE paymentId = :paymentId AND statusId = :statusId")
    public QueryStreamEffect<ViewRecord> getRecordStream(QueryRequest queryRequest) {
        return queryStreamResult();
    }
    @Query(value = "SELECT * FROM transaction_by_payment_and_status WHERE paymentId = :paymentId AND statusId = :statusId", streamUpdates = true)
    public QueryStreamEffect<ViewRecord> getRecordContinousStream(QueryRequest queryRequest) {
        return queryStreamResult();
    }
}
