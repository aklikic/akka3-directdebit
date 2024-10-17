package com.example.akka.directdebit.payment.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Patch;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.example.akka.directdebit.payment.fileimport.ImportFileProcessor;
import com.example.akka.directdebit.payment.api.PaymentCommand.*;
import com.example.akka.directdebit.payment.api.PaymentCommandResponse.*;
import com.example.akka.directdebit.payment.application.PaymentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/payment")
public class PaymentEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEndpoint.class);

    private final ComponentClient componentClient;
    private final ImportFileProcessor importTopicMessageProcessor;

    public PaymentEndpoint(ComponentClient componentClient, ImportFileProcessor importTopicMessageProcessor) {
        this.componentClient = componentClient;
        this.importTopicMessageProcessor = importTopicMessageProcessor;
    }

    @Post("/{id}/create")
    public CompletionStage<Ack> create(String paymentId, Create command){
        logger.info("create [{}]: {}",paymentId, command);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::create).invokeAsync(command);
    }
    @Patch("/{id}/initialize")
    public CompletionStage<Ack> initialize(String paymentId){
        logger.info("initialize [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::initialize).invokeAsync();
    }

    @Patch("/{id}/set-credit-failed")
    public CompletionStage<Ack> setCreditFailed(String paymentId, SetCreditFailed command){
        logger.info("setCreditFailed [{}]: {}",paymentId, command);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::setCreditFailed).invokeAsync(command);
    }
    @Patch("/{id}/set-credited")
    public CompletionStage<Ack> setCredited(String paymentId){
        logger.info("setCredited [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::setCredited).invokeAsync();
    }
    @Get("/{id}")
    public CompletionStage<GetPaymentStateReply> getTransactionState(String paymentId){
        logger.info("get [{}]",paymentId);
        return componentClient.forEventSourcedEntity(paymentId).method(PaymentEntity::getPaymentState).invokeAsync();
    }
    @Post("/import")
    public CompletionStage<Ack> importFile(ImportMessage.FileToImport message){
        return importTopicMessageProcessor.process(message)
                .thenApply(ack -> Ack.ok())
                .exceptionally(ex -> {
                    logger.error("importFile: {}",ex);
                    return Ack.error(PaymentCommandError.IMPORT_FILE_PROCESSING_ERROR);
                });
    }

}
