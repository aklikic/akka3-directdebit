package com.example.akka.directdebit.fileimport.datamodel;

import java.util.List;

public record Payment(String paymentId, int creditAmount, List<Transaction> trans) {
}
