package com.example.akka.directdebit.payment;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySettings {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public final int paymentCreditDelaySeconds;
    public final int importPaymentParallelism;
    public final int importTransactionParallelism;
    public final String s3BucketName;

    public MySettings(Config config) {
        this.paymentCreditDelaySeconds = config.getInt("payment.credit-delay-seconds");
        logger.info("paymentCreditDelaySeconds: {}", this.paymentCreditDelaySeconds);
        this.importPaymentParallelism = config.getInt("import.payment.parallelism");
        logger.info("importPaymentParallelism: {}", this.importPaymentParallelism);
        this.importTransactionParallelism = config.getInt("import.transaction.parallelism");
        logger.info("importTransactionParallelism: {}", this.importTransactionParallelism);
        this.s3BucketName = config.getString("import.s3.bucket-name");
        logger.info("s3BucketName: {}", this.s3BucketName);
    }

    public MySettings(int paymentCreditDelaySeconds, int importPaymentParallelism, int importTransactionParallelism, String s3BucketName) {
        this.paymentCreditDelaySeconds = paymentCreditDelaySeconds;
        this.importPaymentParallelism = importPaymentParallelism;
        this.importTransactionParallelism = importTransactionParallelism;
        this.s3BucketName = s3BucketName;
    }
}
