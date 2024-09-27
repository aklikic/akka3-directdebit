package com.example.akka.directdebit.payment.api;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.http.StrictResponse;
import com.example.akka.directdebit.payment.api.PaymentCommand.*;
import com.example.akka.directdebit.payment.api.PaymentCommandResponse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public record PaymentClient(HttpClient httpClient) {
    private static final Logger logger = LoggerFactory.getLogger(PaymentClient.class);

    public PaymentClient(HttpClientProvider httpClientProvider){
        this(httpClientProvider.httpClientFor("payment"));
    }
    public CompletionStage<Ack> create(String paymentId, Create command){
        logger.info("create [{}]:{}",paymentId,command);
        return httpClient.POST("/payment/"+paymentId+"/create")
                .withRequestBody(command)
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<Ack> initialize(String paymentId){
        logger.info("initialize [{}]",paymentId);
        return httpClient.PATCH("/payment/"+paymentId+"/initialize")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<Ack> setCredited(String paymentId){
        logger.info("setCredited [{}]",paymentId);
        return httpClient.PATCH("/payment/"+paymentId+"/set-credited")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<Ack> setCreditFailed(String paymentId, SetCreditFailed command){
        logger.info("setCreditFailed [{}]",paymentId);
        return httpClient.PATCH("/payment/"+paymentId+"/set-credit-failed")
                .responseBodyAs(Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<GetPaymentStateReply> getPaymentState (String paymentId){
        logger.info("get [{}]",paymentId);
        return httpClient.GET("/payment/"+paymentId)
                .responseBodyAs(GetPaymentStateReply.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
}
