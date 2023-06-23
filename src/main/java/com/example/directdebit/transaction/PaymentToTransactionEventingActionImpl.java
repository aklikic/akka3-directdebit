package com.example.directdebit.transaction;

import com.example.directdebit.Utils;
import com.example.directdebit.payment.PaymentDomain;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your com/example/directdebit/transaction/payment_to_transaction_eventing_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class PaymentToTransactionEventingActionImpl extends AbstractPaymentToTransactionEventingAction {

  private static Logger logger = LoggerFactory.getLogger(PaymentToTransactionEventingActionImpl.class);
  public PaymentToTransactionEventingActionImpl(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onInitialized(PaymentDomain.Initialized initialized) {
    logger.info("onInitialized: {}",initialized);
    var initializeAllTransactions = initialized.getTransIdsList().stream()
            .map(transId ->
                    components().transactionEntity().initialize(TransactionApi.InitializeCommand.newBuilder().setTransId(transId).build()).execute()
                            .exceptionally(ex -> {
                              logger.error("onInitialized: {} Error:",initialized.getPaymentId(),ex);
                              throw (RuntimeException)ex;
                            })
                            .toCompletableFuture()).collect(Collectors.toList());
    var zip = Utils.allOf(initializeAllTransactions).thenApply(l -> Empty.getDefaultInstance())
            .thenApply(res -> {
              logger.info("onInitialized: {} DONE!",initialized.getPaymentId());
              return res;
            });
    return effects().asyncReply(zip);
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }


}
