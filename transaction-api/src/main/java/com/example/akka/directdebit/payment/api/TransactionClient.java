package com.example.akka.directdebit.payment.api;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.StrictResponse;
import com.example.akka.directdebit.payment.api.TransactionCommand.Create;
import com.example.akka.directdebit.payment.api.TransactionCommand.SetDebitFailed;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.Ack;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.GetTransactionStateReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record TransactionClient(HttpClient httpClient) {
    private static final Logger logger = LoggerFactory.getLogger(TransactionClient.class);

    public CompletionStage<Ack> create(String transId, Create command){
        logger.info("create [{}]:{}",transId,command);
        return httpClient.POST("/transaction/"+transId+"/create")
                .withRequestBody(command)
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<Ack> initialize(String transId){
        logger.info("initialize [{}]",transId);
        return httpClient.PATCH("/transaction/"+transId+"/initialize")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
//    public CompletionStage<Ack> startDebit(String transId){
//        logger.info("startDebit [{}]",transId);
//        return httpClient.PATCH("/transaction/"+transId+"/start-debit")
//                .responseBodyAs(Ack.class)
//                .invokeAsync()
//                .thenApply(StrictResponse::body);
//    }
    public CompletionStage<Ack> setDebited(String transId){
        logger.info("setDebited [{}]",transId);
        return httpClient.PATCH("/transaction/"+transId+"/set-debited")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<Ack> setDebitFailed(String transId, SetDebitFailed command){
        logger.info("setCreditFailed [{}]",transId);
        return httpClient.PATCH("/transaction/"+transId+"/set-debit-failed")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<GetTransactionStateReply> getTransactionState (String transId){
        logger.info("get [{}]",transId);
        return httpClient.GET("/transaction/"+transId)
                .responseBodyAs(GetTransactionStateReply.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

    public CompletionStage<TransactionByPaymentAndStatusViewModel.ViewRecordList> queryByPaymentAndStatus(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest){
        logger.info("queryByPaymentAndStatus [{}]",queryRequest);
        return httpClient.POST("/transaction/query-by-payment-and-status")
                .withRequestBody(queryRequest)
                .responseBodyAs(TransactionByPaymentAndStatusViewModel.ViewRecordList.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
}
