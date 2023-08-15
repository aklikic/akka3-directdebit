# kalix-directdebit

## Unit test
Unit tests covers isolated tests of each Kalix component, in our case `Entity` and `Action` (Action tests are not implemented yet). <br>
Run unit test:
```
mvn test
```
## Integration test
`it/java/com/example/directdebit/SystemIntegrationTest` simulates file importer and uses `payment` and `transaction` services.<br>
Run integration test:
```
mvn -Pit verify
```
## Run locally
Start `Kalix proxy` and `UF` (user function)
```
mvn kalix:runAll
```
GRPC server is exposed on `localhost:9000`<br>
Local run uses in memory database so when run is complete data is lost. 

## Test locally
Example how to create payment with two transactions.<br>
1. Create payment:
```
grpcurl -d '{"payment_id":"pay1","credit_amount":200,"transactions":[{"trans_id":"trans1"},{"trans_id":"trans2"}]}' -plaintext localhost:9000 com.example.directdebit.payment.PaymentService/Create
```
2. Create transaction #1:
```
grpcurl -d '{"trans_id":"trans1", "payment_id":"pay1","debit_amount":100}' -plaintext localhost:9000 com.example.directdebit.transaction.TransactionService/Create
```
3. Create transaction #2:
```
grpcurl -d '{"trans_id":"trans2", "payment_id":"pay1","debit_amount":100}' -plaintext localhost:9000 com.example.directdebit.transaction.TransactionService/Create
```
4. Initialize payment:
```
grpcurl -d '{"payment_id":"pay1"}' -plaintext localhost:9000 com.example.directdebit.payment.PaymentService/Initialize
```
5. Query transactions status by payment:
```
grpcurl -d '{"payment_id":"pay1","status_id":4}' -plaintext localhost:9000 com.example.directdebit.transaction.TransactionByPaymentAndStatusView/GetTransactionByPaymentAndStatus
```
### Help grpcurls:
- Get payment state
```
grpcurl -d '{"payment_id":"pay1"}' -plaintext localhost:9000 com.example.directdebit.payment.PaymentService/GetPaymentState
```
- Get transaction state
```
grpcurl -d '{"trans_id":"trans1"}' -plaintext localhost:9000 com.example.directdebit.transaction.TransactionService/GetTransactionState
```