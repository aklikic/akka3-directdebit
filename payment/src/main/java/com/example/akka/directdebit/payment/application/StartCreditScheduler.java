package com.example.akka.directdebit.payment.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.example.akka.directdebit.payment.domain.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ComponentId("start-credit-scheduler")
@Consume.FromEventSourcedEntity(PaymentEntity.class)
public class StartCreditScheduler extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ComponentClient componentClient;

    public StartCreditScheduler(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(PaymentEvent event) {
        logger.info("onEvent: {}", event);
        return switch (event) {
            case PaymentEvent.Initialized evt -> {
                var deferredCall = componentClient.forEventSourcedEntity(evt.paymentId()).method(PaymentEntity::startCredit).deferred();
                yield effects().asyncDone(timers().startSingleTimer(
                        getTimerName(evt.paymentId()),
                        Duration.of(evt.scheduledAfterSec(), ChronoUnit.SECONDS),
                        deferredCall));
            }
            case PaymentEvent.CreditStarted evt -> effects().asyncDone(timers().cancel(getTimerName(evt.paymentId())));
            default -> effects().done();
        };
    }

    private String getTimerName(String transId){
        return "timer-" + transId;
    }
}
