package com.example.akka.directdebit.payment.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.example.akka.directdebit.payment.domain.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ComponentId("start-debit-scheduler")
@Consume.FromEventSourcedEntity(TransactionEntity.class)
public class StartDebitScheduler extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ComponentClient componentClient;

    public StartDebitScheduler(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(TransactionEvent event) {
        logger.info("onEvent: {}", event);
        return switch (event) {
            case TransactionEvent.Initialized evt -> {
                var deferredCall = componentClient.forEventSourcedEntity(evt.transId()).method(TransactionEntity::startDebit).deferred();
                yield effects().asyncDone(timers().startSingleTimer(
                        getTimerName(evt.transId()),
                        Duration.of(evt.scheduledAfterSec(), ChronoUnit.SECONDS),
                        deferredCall));
            }
            case TransactionEvent.DebitStarted evt -> effects().asyncDone(timers().cancel(getTimerName(evt.transId())));
            default -> effects().done();
        };
    }

    private String getTimerName(String transId){
        return "timer-" + transId;
    }
}
