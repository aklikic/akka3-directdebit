package com.example.akka.directdebit.importer;

import com.example.akka.directdebit.payment.api.TransactionByPaymentAndStatusViewModel;
import com.example.akka.directdebit.payment.api.TransactionClient;
import com.example.akka.directdebit.payment.api.TransactionCommand;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public record MockTransactionClient() implements TransactionClient {
    @Override
    public CompletionStage<TransactionCommandResponse.Ack> create(String transId, TransactionCommand.Create command) {
        return CompletableFuture.completedFuture(TransactionCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<TransactionCommandResponse.Ack> initialize(String transId) {
        return CompletableFuture.completedFuture(TransactionCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<TransactionCommandResponse.Ack> setDebited(String transId) {
        return CompletableFuture.completedFuture(TransactionCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<TransactionCommandResponse.Ack> setDebitFailed(String transId, TransactionCommand.SetDebitFailed command) {
        return CompletableFuture.completedFuture(TransactionCommandResponse.Ack.ok());
    }

    @Override
    public CompletionStage<TransactionCommandResponse.GetTransactionStateReply> getTransactionState(String transId) {
        return null;
    }

    @Override
    public CompletionStage<TransactionByPaymentAndStatusViewModel.ViewRecordList> queryByPaymentAndStatus(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest) {
        return null;
    }
}
