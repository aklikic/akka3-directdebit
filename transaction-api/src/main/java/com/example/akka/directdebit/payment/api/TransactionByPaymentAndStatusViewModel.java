package com.example.akka.directdebit.payment.api;

import java.time.Instant;
import java.util.List;

public interface TransactionByPaymentAndStatusViewModel {

    record ViewRecord(
        String paymentId,
        String statusId,
        String transId,
        Instant timestamp) {
    }

    record ViewRecordList(List<ViewRecord> records){}

    record QueryRequest(String paymentId, String statusId){}


}
