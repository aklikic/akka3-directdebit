package com.example.directdebit;

import com.example.directdebit.fileprocessor.FileProcessorActionImpl;
import com.example.directdebit.fileprocessor.FileProcessorEntity;
import com.example.directdebit.payment.PaymentEntity;
import com.example.directdebit.payment.PaymentProcessorActionImpl;
import com.example.directdebit.transaction.PaymentToTransactionEventingActionImpl;
import com.example.directdebit.transaction.TransactionByPaymentAndStatusViewImpl;
import com.example.directdebit.transaction.TransactionEntity;
import com.example.directdebit.transaction.TransactionProcessorActionImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kalix.javasdk.Kalix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static Kalix createKalix() {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `new Kalix()` instance.
    Config config = ConfigFactory.load();
    return KalixFactory.withComponents(
      FileProcessorEntity::new,
      ctx -> new PaymentEntity(ctx,config.getInt("payment.credit-delay-seconds")),
      ctx -> new TransactionEntity(ctx, config.getInt("transaction.debit-delay-seconds")),
      FileProcessorActionImpl::new,
      PaymentProcessorActionImpl::new,
      PaymentToTransactionEventingActionImpl::new,
      TransactionByPaymentAndStatusViewImpl::new,
      TransactionProcessorActionImpl::new);
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Kalix service");
    createKalix().start();
  }
}
