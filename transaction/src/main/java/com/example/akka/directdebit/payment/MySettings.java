package com.example.akka.directdebit.payment;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySettings {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public final int transactionDebitDelaySeconds;

    public MySettings(Config config) {
        this.transactionDebitDelaySeconds = config.getInt("transaction.debit-delay-seconds");
        logger.info("transactionDebitDelaySeconds: {}", this.transactionDebitDelaySeconds);
    }

    public MySettings(int transactionDebitDelaySeconds) {
        this.transactionDebitDelaySeconds = transactionDebitDelaySeconds;
    }
}
