package com.example.akka.directdebit.importer.domain;


import java.util.List;
import java.util.Optional;

public record ImportState(String fileName, List<Payment> payments, ImportStatus status){


    enum ImportFsmStatus {
        UNKNOWN,
        READY_FOR_IMPORT,
        IN_CREATE,
        CREATED,
        CREATE_ERROR,
        IN_INITIALIZE,
        INITIALIZE_ERROR,
        INITIALIZED

    }
    record Transaction(String transId, Integer debitAmount, ImportFsmStatus status, Optional<String> error){
        public static Transaction of(String transId, Integer debitAmount){
            return new Transaction(transId, debitAmount, ImportFsmStatus.READY_FOR_IMPORT, Optional.empty());
        }

        public Transaction error(ImportFsmStatus status, String error){
            return new Transaction(transId, debitAmount, status, Optional.of(error));
        }
        public Transaction ok(ImportFsmStatus status){
            return new Transaction(transId, debitAmount, status, Optional.empty());
        }
    }

    record Payment(String paymentId, Integer creditAmount, List<Transaction> transactions, ImportFsmStatus status, Optional<String> error){
        public static Payment of(String paymentId, Integer creditAmount, List<Transaction> transaction){
            return new Payment(paymentId, creditAmount, transaction, ImportFsmStatus.READY_FOR_IMPORT, Optional.empty());
        }
        public Payment error(ImportFsmStatus status, String error){
            return new Payment(paymentId, creditAmount, transactions, status, Optional.of(error));
        }
        public Payment ok(ImportFsmStatus status){
            return new Payment(paymentId, creditAmount, transactions, status, Optional.empty());
        }
        public Payment errorTrans(String transId, ImportFsmStatus transStatus, String error){
            transactions.stream().filter(t -> t.transId() == transId).findFirst().ifPresent(t -> t.error(status,error));
            ImportFsmStatus paymentTrans = switch (status){
                case CREATED -> ImportFsmStatus.CREATED;
            }
            return new Payment(paymentId, creditAmount, transactions, Im, Optional.empty());
        }
        public Payment okTrans(String transId, ImportFsmStatus transStatus){
            transactions.stream().filter(t -> t.transId() == transId).findFirst().ifPresent(t -> t.ok(status));
            return new Payment(paymentId, creditAmount, transactions, status, Optional.empty());
        }

    }
    enum ImportStatus {
        UNKNOWN,
        STARTED,
        COMPLETED,
        COMPLETED_WITH_ERROR
    }

    public static ImportState empty(){
        return null;
    }
    public static ImportState start(String fileName, List<Payment> payments) {
        return new ImportState(fileName, payments, ImportStatus.STARTED);
    }
}
