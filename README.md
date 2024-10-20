# Direct Debit 
# Test locally
Generate file:
```shell
mvn exec:java -Dexec.mainClass="com.example.directdebit.transaction.api.ImportFileGenerator"
```
Trigger import:
```shell
curl -XPOST -d '{
  "fileLocation": "s3://import-38da4ff6-bfee-4f3e-9577-f31db7984c9c.txt"
}' http://localhost:9001/payment/import -v -H "Content-Type: application/json"
```

# Deploy
## Configure KCR (Kalix Container Registry)
https://docs.kalix.io/operations/container-registries.html#_kalix_container_registry

## Push image to KCR and deploy
In each Maven module/project (`transaction`, `payment`) configure these properties in `pom.xml` (`container.registry`, `organization`) and run:
```shell
mvn deploy
```
Copy the image URL and deploy service to Kalix:
```shell
kalix service deploy --with-embedded-runtime <service name> <pushed image url>
```

# Demo (test) in Cloud runtime
## Set proxies:
```shell
kalix service proxy transaction --port 9003
```
```shell
kalix service proxy payment --port 9001
```
### Test (demo)
Run the same commands as with local test.

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