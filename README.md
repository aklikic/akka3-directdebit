# Direct Debit 
# Install
```shell
mvn install 
```
# Test locally
Generate file:
```shell
mvn exec:java -Dexec.mainClass="com.example.directdebit.importer.FileGenerator"
```
Trigger endpoint import:
```shell
curl -XPOST -d '{
  "fileName": "import-532df49a-e91f-413e-a84a-324a56f2d314.txt",
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/import -H "Content-Type: application/json"
```

Trigger consumer import. Produce message to kafka-ui import topic. 
```json
{
  "fileName": "import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt",
  "folder": "akka3-direct-debit"
}
```

Query transactions by payment and status:
```shell
curl -XPOST -d '{
  "paymentId": "p1-5b11d096-e6d2-403b-8efb-508daa6d98be",
  "statusId": "DEBIT_STARTED"
}' http://localhost:9003/transaction/query-by-payment-and-status -H "Content-Type: application/json"
```

Start s3 bucket listing:
```shell
curl -XPOST -d '{
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/listing/initialise -H "Content-Type: application/json"
```

# Deploy
## Push image and deploy
In each Maven module/project (`transaction`, `payment`, `importer`) configure these properties in `pom.xml` (`dockerImage`):
```shell
mvn deploy
```
Copy the image URL and deploy service to Kalix:
```shell
kalix service deploy --with-embedded-runtime <service name> <pushed image url>
```

# Multi-region demo
Create transaction (GCP region):
```shell
curl -XPOST -d '{
  "paymentId": "p123456",
  "debitAmountCents":"100"
}' https://floral-brook-7298.gcp-us-east1.apps.akka.dev/transaction/t123456/create -H "Content-Type: application/json"
```
Get transaction (AWS region):
```shell
curl -XGET https://floral-brook-7298.aws-us-east-2.apps.akka.dev/transaction/t123456 -H "Content-Type: application/json"
```
Get transaction (GCP region):
```shell
curl -XGET https://floral-brook-7298.gcp-us-east1.apps.akka.dev/transaction/t123456 -H "Content-Type: application/json"
```
Initialize transaction (AWS region):
```shell
curl -XPATCH https://floral-brook-7298.aws-us-east-2.apps.akka.dev/transaction/t123456/initialize -H "Content-Type: application/json"
```