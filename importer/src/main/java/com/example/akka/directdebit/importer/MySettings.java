package com.example.akka.directdebit.importer;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MySettings {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public final int paymentCreditDelaySeconds;
    public final int importPaymentParallelism;
    public final int importTransactionParallelism;
    public final String s3BucketName;
    public final Duration s3BucketListIntervalSeconds;
    public final boolean disableFileDelete;

    public MySettings(Config config) {
        this.paymentCreditDelaySeconds = config.getInt("payment.credit-delay-seconds");
        logger.info("paymentCreditDelaySeconds: {}", this.paymentCreditDelaySeconds);
        this.importPaymentParallelism = config.getInt("import.payment.parallelism");
        logger.info("importPaymentParallelism: {}", this.importPaymentParallelism);
        this.importTransactionParallelism = config.getInt("import.transaction.parallelism");
        logger.info("importTransactionParallelism: {}", this.importTransactionParallelism);
        this.s3BucketName = config.getString("import.s3.bucket-name");
        logger.info("s3BucketName: {}", this.s3BucketName);
        this.s3BucketListIntervalSeconds = Duration.of(config.getLong("import.s3.list-interval-seconds"), ChronoUnit.SECONDS);
        logger.info("s3BucketListIntervalSeconds: {}", this.s3BucketListIntervalSeconds);
        this.disableFileDelete = config.getBoolean("import.s3.disable-delete");
        logger.info("disableFileDelete: {}", this.disableFileDelete);
    }

    public MySettings(int paymentCreditDelaySeconds, int importPaymentParallelism, int importTransactionParallelism, String s3BucketName, Duration s3BucketListIntervalSeconds, boolean disableFileDelete) {
        this.paymentCreditDelaySeconds = paymentCreditDelaySeconds;
        this.importPaymentParallelism = importPaymentParallelism;
        this.importTransactionParallelism = importTransactionParallelism;
        this.s3BucketName = s3BucketName;
        this.s3BucketListIntervalSeconds = s3BucketListIntervalSeconds;
        this.disableFileDelete = disableFileDelete;
    }
}
