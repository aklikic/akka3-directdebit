package com.example.akka.directdebit.payment.api;

import java.util.concurrent.CompletionStage;

public interface TransactionClient {
    CompletionStage<TransactionCommandResponse.Ack> create(String transId, TransactionCommand.Create command);
    CompletionStage<TransactionCommandResponse.Ack> initialize(String transId);
    CompletionStage<TransactionCommandResponse.Ack> setDebited(String transId);
    CompletionStage<TransactionCommandResponse.Ack> setDebitFailed(String transId, TransactionCommand.SetDebitFailed command);
    CompletionStage<TransactionCommandResponse.GetTransactionStateReply> getTransactionState (String transId);
    CompletionStage<TransactionByPaymentAndStatusViewModel.ViewRecordList> queryByPaymentAndStatus(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest);
}
