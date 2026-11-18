package org.ubunifu.armania_rofinder;

import java.util.*;
import java.util.concurrent.*;

public class RecursionFinder implements AutoCloseable {
    private final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final Set<RecursionResult> RESULTS = ConcurrentHashMap.newKeySet();
    private final Set<CompletableFuture<Void>> TASKS = ConcurrentHashMap.newKeySet();
    private RecursionFinder(ROContext input, ROPro processor) {
        HashMap<ROPro, ROContext> contexts = processor.createBranches(input);
        contexts.forEach((proc, ctx)-> TASKS.add(CompletableFuture.runAsync(()->getIdealOutcome(ctx,proc,RESULTS), EXECUTOR_SERVICE)));
    }
    public static RecursionResult findResultSync(ROContext input, ROPro processor) {
        return findResultAsync(input, processor).join();
    }
    public static CompletableFuture<RecursionResult> findResultAsync(ROContext input, ROPro processor) {
        try(RecursionFinder finder = new RecursionFinder(input, processor)){
            return CompletableFuture.allOf(finder.TASKS.toArray(CompletableFuture[]::new))
                    .thenApply((a)-> finder.RESULTS
                            .stream()
                            .min(Comparator.comparingDouble(e->e.heuristic))
                            .orElseThrow());
        }catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private void getIdealOutcome(ROContext input, ROPro processor, Set<RecursionResult> resultList) {
        RecursionResult result = processor.process(input);
        resultList.add(result);
        HashMap<ROPro, ROContext> contexts = processor.createBranches(input);
        contexts.forEach((proc,ctx)->getIdealOutcome(ctx, proc, resultList));
    }

    @Override
    public void close() {
        EXECUTOR_SERVICE.shutdownNow();
        EXECUTOR_SERVICE.close();
    }
}
