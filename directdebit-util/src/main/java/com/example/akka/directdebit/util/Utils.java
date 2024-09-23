package com.example.akka.directdebit.util;

import akka.Done;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class Utils {
    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture cf = CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
        return cf.thenApply((v) -> futuresList.stream().map((future) -> future.join()).collect(Collectors.toList()));
    }

    public static <T> CompletionStage<Done> doAll(List<CompletionStage<T>> stages) {
        Object var1 = CompletableFuture.completedFuture(Done.getInstance());

        CompletionStage var3;
        for(Iterator var2 = stages.iterator(); var2.hasNext(); var1 = ((CompletionStage)var1).thenCombine(var3, (d1, d2) -> {
            return Done.getInstance();
        })) {
            var3 = (CompletionStage)var2.next();
        }

        return (CompletionStage)var1;
    }
}
