package org.ubunifu.armania_rofinder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;

public final class RecursionFinder {

    public static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    public static CompletableFuture<@Nullable ROResult> findResult(ROPro processor) {
        return findResultsAsync(processor)
                .thenApply(results->results.stream()
                        .min(Comparator.comparing(ROResult::getEffectiveHeuristic))
                        .orElse(null));
    }
    public static CompletableFuture<Set<ROResult>> findResultsAsync(ROPro processor) {

        Set<ROResult> results = ConcurrentHashMap.newKeySet();

        // Perform the initial check on the calling thread.
        ROResult result = processor.process();
        results.add(result);

        // Start exploring every initial branch asynchronously.
        List<CompletableFuture<Void>> tasks =
                processor.createBranches().stream()
                        .map(branch -> exploreAsync(
                                branch.processor(),
                                results))
                        .toList();

        return CompletableFuture.allOf(
                tasks.toArray(CompletableFuture[]::new)
        ).thenApply(ignored -> results);
    }
    private static CompletableFuture<Void> exploreAsync(ROPro processor, Set<ROResult> results) {

        return CompletableFuture.runAsync(() -> {
            ROResult result = processor.process();
            results.add(result);

            List<CompletableFuture<Void>> tasks =
                    processor.createBranches().stream()
                            .map(branch -> exploreAsync(
                                    branch.processor(),
                                    results))
                            .toList();

            CompletableFuture.allOf(
                    tasks.toArray(CompletableFuture[]::new)
            ).join();

        }, FORK_JOIN_POOL);
    }
}
