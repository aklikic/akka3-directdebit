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
```shell
mvn deploy
```