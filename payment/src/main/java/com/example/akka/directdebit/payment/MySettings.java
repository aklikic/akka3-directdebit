package com.example.akka.directdebit.payment;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySettings {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public final int paymentCreditDelaySeconds;
    public MySettings(Config config) {
        this.paymentCreditDelaySeconds = config.getInt("payment.credit-delay-seconds");
        logger.info("paymentCreditDelaySeconds: {}", this.paymentCreditDelaySeconds);
    }

    public MySettings(int paymentCreditDelaySeconds) {
        this.paymentCreditDelaySeconds = paymentCreditDelaySeconds;
    }
}
