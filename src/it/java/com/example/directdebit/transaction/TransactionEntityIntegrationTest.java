package com.example.directdebit.transaction;

import com.example.directdebit.Main;
import com.google.protobuf.Empty;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class TransactionEntityIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final TransactionService client;

  public TransactionEntityIntegrationTest() {
    client = testKit.getGrpcClient(TransactionService.class);
  }

  @Test
  @Ignore("to be implemented")
  public void createOnNonExistingEntity() throws Exception {

    // TODO: set fields in command, and provide assertions to match replies
    // client.create(PaymentApi.CreateCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  @Ignore("to be implemented")
  public void initializeOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.initialize(PaymentApi.InitializeCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  @Ignore("to be implemented")
  public void triggerDebitOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.triggerDebit(PaymentApi.TriggerDebitCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  @Ignore("to be implemented")
  public void debitSuccessfulOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.debitSuccessful(PaymentApi.DebitSuccessfulCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  @Ignore("to be implemented")
  public void debitFailOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.debitFail(PaymentApi.DebitFailCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  @Ignore("to be implemented")
  public void getTransactionStateOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.getTransactionState(PaymentApi.GetTransactionStateCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }
}
