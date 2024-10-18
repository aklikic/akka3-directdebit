package com.example.akka.directdebit.payment.api;

import akka.NotUsed;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.CacheControl;
import akka.http.javadsl.model.headers.CacheDirectives;
import akka.http.javadsl.model.headers.Connection;
import akka.javasdk.JsonSupport;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Patch;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.example.akka.directdebit.payment.api.TransactionCommand.Create;
import com.example.akka.directdebit.payment.api.TransactionCommand.SetDebitFailed;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.Ack;
import com.example.akka.directdebit.payment.api.TransactionCommandResponse.GetTransactionStateReply;
import com.example.akka.directdebit.payment.application.TransactionByPaymentAndStatusView;
import com.example.akka.directdebit.payment.application.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@HttpEndpoint("/transaction")
public class TransactionEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TransactionEndpoint.class);

    private final ComponentClient componentClient;

    public TransactionEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/{id}/create")
    public CompletionStage<Ack> create(String transId, Create command){
        logger.info("create [{}]: {}",transId, command);
        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::create).invokeAsync(command);
    }
    @Patch("/{id}/initialize")
    public CompletionStage<Ack> initialize(String transId){
        logger.info("initialize [{}]",transId);
        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::initialize).invokeAsync();
    }
//    @Patch("/{id}/start-debit")
//    public CompletionStage<Ack> startDebit(String transId){
//        logger.info("startDebit [{}]",transId);
//        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::startDebit).invokeAsync();
//    }
    @Patch("/{id}/set-debit-failed")
    public CompletionStage<Ack> setDebitFailed(String transId, SetDebitFailed command){
        logger.info("setDebitFailed [{}]: {}",transId, command);
        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::setDebitFailed).invokeAsync(command);
    }
    @Patch("/{id}/set-debited")
    public CompletionStage<Ack> setDebited(String transId){
        logger.info("setDebited [{}]",transId);
        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::setDebited).invokeAsync();
    }
    @Get("/{id}")
    public CompletionStage<GetTransactionStateReply> getTransactionState(String transId){
        logger.info("get [{}]",transId);
        return componentClient.forEventSourcedEntity(transId).method(TransactionEntity::getTransactionState).invokeAsync();
    }
    @Post("/query-by-payment-and-status")
    public CompletionStage<TransactionByPaymentAndStatusViewModel.ViewRecordList> queryByPaymentAndStatus(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest){
        logger.info("queryByPaymentAndStatus [{}]",queryRequest);
        return componentClient.forView().method(TransactionByPaymentAndStatusView::getRecordList).invokeAsync(queryRequest);
    }
    @Post("/query-by-payment-and-status-stream-csv")
    public HttpResponse queryByPaymentAndStatusCsv(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest){
        logger.info("queryByPaymentAndStatusCsv [{}]",queryRequest);
        var querySource = componentClient.forView().stream(TransactionByPaymentAndStatusView::getRecordContinousStream).source(queryRequest);

        Source<ByteString, NotUsed> csvByteChunkStream =
                Source.single("payment_id,trans_id,status\n").concat(querySource.map(record ->
                        String.format("%s,%s,%s\n",record.paymentId(), record.transId(), record.statusId())
                )).map(ByteString::fromString);

        return HttpResponse.create()
                .withStatus(StatusCodes.OK)
                .withEntity(HttpEntities.create(ContentTypes.TEXT_CSV_UTF8, csvByteChunkStream));
    }
    @Post("/query-by-payment-and-status-stream-sse")
    public HttpResponse queryByPaymentAndStatusServerSentEvents(TransactionByPaymentAndStatusViewModel.QueryRequest queryRequest){
        logger.info("queryByPaymentAndStatusServerSentEvents [{}]",queryRequest);

        final var eventPrefix = ByteString.fromString("data: ");
        final var eventEnd = ByteString.fromString("\n\n");

        Source<ByteString, NotUsed> querySource = componentClient.forView().stream(TransactionByPaymentAndStatusView::getRecordContinousStream).source(queryRequest)
                .map(record -> eventPrefix.concat(JsonSupport.encodeToAkkaByteString(record)).concat(eventEnd));

        return HttpResponse.create()
                .withStatus(StatusCodes.OK)
                .withHeaders(Arrays.asList(
                        CacheControl.create(CacheDirectives.NO_CACHE),
                        Connection.create("keep-alive")
                ))
                .withEntity(HttpEntities.create(ContentTypes.parse("text/event-stream"),querySource));
    }

}
