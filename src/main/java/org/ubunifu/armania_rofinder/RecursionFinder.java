package org.ubunifu.armania_rofinder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;

public final class RecursionFinder {

    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    public static <C extends ROContext> CompletableFuture<@Nullable ROResult> findResult(
            ROPro<C> processor) {

        return findResultsAsync(processor)
                .thenApply(results -> results.stream()
                        .min(Comparator.comparing(
                                ROResult::getEffectiveHeuristic))
                        .orElse(null));
    }

    public static <C extends ROContext> CompletableFuture<Set<ROResult>> findResultsAsync(
            ROPro<C> processor) {

        Set<ROResult> results = ConcurrentHashMap.newKeySet();
        return CompletableFuture.supplyAsync(processor::process)
                .thenComposeAsync(result-> continueExploring(processor, result, results)
                .thenApply(ignored -> results));
    }

    private static <C extends ROContext> CompletableFuture<Void> exploreAsync(
            ROPro<C> processor,
            Set<ROResult> results) {
        return CompletableFuture
                .supplyAsync(processor::process, FORK_JOIN_POOL)
                .thenCompose(result ->
                        continueExploring(processor, result, results));
    }

    private static <C extends ROContext> CompletableFuture<Void> continueExploring(
            ROPro<C> processor,
            ROResult result,
            Set<ROResult> results) {

        return switch (processor.getStatus()) {

            case FAILED -> CompletableFuture.completedFuture(null);

            case RESOLVED -> {
                results.add(result);
                yield CompletableFuture.completedFuture(null);
            }

            case PROCESSING -> {
                List<CompletableFuture<Void>> tasks =
                        processor.createBranches().stream()
                                .map(branch -> exploreAsync(
                                        branch.processor(),
                                        results))
                                .toList();

                yield CompletableFuture.allOf(
                        tasks.toArray(CompletableFuture[]::new));
            }
        };
    }
}
