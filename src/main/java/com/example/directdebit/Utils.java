package com.example.directdebit;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Utils {
    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture cf = CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
        return cf.thenApply((v) -> futuresList.stream().map((future) -> future.join()).collect(Collectors.toList()));
    }
}
