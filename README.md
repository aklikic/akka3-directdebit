# Direct Debit 
Akka official docs: [doc.akka.io](https://doc.akka.io/)
# Prerequisites
Check for information [here](https://doc.akka.io/java/index.html#_prerequisites)
# Setup
1. Create a S3 bucket with read `Everyone (public access)`.
2. Install [Akka CLI](https://doc.akka.io/reference/cli/installation.html)
# Install
In project root:
```shell
mvn install -DskipTests
```
# Test locally
## Setup:

Upload files from `s3files` to S3. Allow read `Everyone (public access)`.

## Run
In each `transaction`, `payment` and `importer`:
```shell
akka local run
```
## Test endpoint based import
Trigger based endpoint import (replace `folder` with S3 bucket name):
```shell
curl -XPOST -d '{
  "fileName": "import-1c860cd4-b161-4c4e-adb6-6d5ba006e196.txt",
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/import -H "Content-Type: application/json"
```

Query transactions by payment and status (replace `paymentId` with one payment from the generated file):
```shell
curl -XPOST -d '{
  "paymentId": "p1-9b025ed2-d699-4d5e-a70d-dfd8d9762e56",
  "statusId": "DEBIT_STARTED"
}' http://localhost:9003/transaction/query-by-payment-and-status -H "Content-Type: application/json"
```

## Test workflow based import:
Start s3 bucket listing (replace `folder` with S3 bucket name):
```shell
curl -XPOST -d '{
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/listing/initialise -H "Content-Type: application/json"
```
Get s3 bucket listing status (replace `folder` with S3 bucket name):
```shell
curl -XGET -d '{
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/get-file-list-state -H "Content-Type: application/json"
```
# Deploy
## Configure container registry
Check for instructions [here](https://doc.akka.io/snapshots/akka-documentation/operations/projects/container-registries.html).
## Build images
In project root:
```shell
mvn install -DskipTests
```
## Check local build images:
```shell
docker images | grep -v latest | head -4
```
## Deploy each service (with image push)
In each module execute
```shell
akka service deploy <service name> <service name>:tag --push
```
### Example: 
```shell
akka service deploy transaction transaction:1.0-SNAPSHOT-20241106100859 --push
```
```shell
akka service deploy payment payment:1.0-SNAPSHOT-20241106100859 --push
```
```shell
akka service deploy importer importer:1.0-SNAPSHOT-20241106100859 --push
```
# Import demo
Proxy access to import service (make sure you don't have local service running to conflict with ports):
```shell
akka service proxy importer --port 9002
```
Start s3 bucket listing (replace `folder` with S3 bucket name)::
```shell
curl -XPOST -d '{
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/listing/initialise -H "Content-Type: application/json"
```

Get s3 bucket listing status (replace `folder` with S3 bucket name)::
```shell
curl -XGET -d '{
  "folder": "akka3-direct-debit"
}' http://localhost:9002/importer/get-file-list-state -H "Content-Type: application/json"
```

Proxy access to transaction service (make sure you don't have local service running to conflict with ports):
```shell
akka service proxy transaction --port 9003
```

Query transactions by payment and status (replace `paymentId` with one payment from the generated file):
```shell
curl -XPOST -d '{
  "paymentId": "p1-9b025ed2-d699-4d5e-a70d-dfd8d9762e56",
  "statusId": "DEBIT_STARTED"
}' http://localhost:9003/transaction/query-by-payment-and-status -H "Content-Type: application/json"
```

# Multi-region demo
## Expose routes to transaction service

```shell
akka service expose transaction --once-per-region
```

## Test
**Note**: replace hostnames in the urls with exposed routes<br>
Create transaction (GCP region):
```shell
curl -XPOST -d '{
  "paymentId": "p1234567",
  "debitAmountCents":"100"
}' https://patient-frog-2417.gcp-us-east1.apps.akka.dev/transaction/t1234567/create -H "Content-Type: application/json"
```
Get transaction (AWS region):
```shell
curl -XGET https://bold-shape-4713.aws-us-east-2.apps.akka.dev/transaction/t1234567 -H "Content-Type: application/json"
```
Get transaction (GCP region):
```shell
curl -XGET https://patient-frog-2417.gcp-us-east1.apps.akka.dev/transaction/t1234567 -H "Content-Type: application/json"
```
Initialize transaction (AWS region):
```shell
curl -XPATCH https://bold-shape-4713.aws-us-east-2.apps.akka.dev/transaction/t1234567/initialize -H "Content-Type: application/json"
```