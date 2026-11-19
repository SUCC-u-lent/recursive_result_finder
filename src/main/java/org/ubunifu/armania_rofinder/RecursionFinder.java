package org.ubunifu.armania_rofinder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class RecursionFinder {

    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    private RecursionFinder() {}

    public static <C extends ROContext> CompletableFuture<@Nullable ROResult> findResult(
            ROPro<C> processor) {
        return findResult(processor, ROSearchListener.NONE);
    }

    public static <C extends ROContext> CompletableFuture<@Nullable ROResult> findResult(
            ROPro<C> processor,
            ROSearchListener listener) {

        return findResultsAsync(processor, listener)
                .thenApply(results -> results.stream()
                        .min(Comparator.comparing(ROResult::getEffectiveHeuristic))
                        .orElse(null));
    }

    public static <C extends ROContext> CompletableFuture<Set<ROResult>> findResultsAsync(
            ROPro<C> processor) {
        return findResultsAsync(processor, ROSearchListener.NONE);
    }

    public static <C extends ROContext> CompletableFuture<Set<ROResult>> findResultsAsync(
            ROPro<C> processor,
            ROSearchListener listener) {

        Objects.requireNonNull(listener, "listener");

        Set<ROResult> results = ConcurrentHashMap.newKeySet();
        AtomicLong nextNodeId = new AtomicLong(1);
        long rootId = 0;

        notifyCreated(listener, rootId, null, "ROOT", processor);

        return CompletableFuture
                .supplyAsync(processor::process, FORK_JOIN_POOL)
                .thenCompose(result -> continueExploring(
                        processor, result, results, listener, nextNodeId, rootId));
    }

    private static <C extends ROContext> CompletableFuture<Void> exploreAsync(
            ROPro<C> processor,
            String branchLabel,
            long parentId,
            Set<ROResult> results,
            ROSearchListener listener,
            AtomicLong nextNodeId) {

        long nodeId = nextNodeId.getAndIncrement();
        notifyCreated(listener, nodeId, parentId, branchLabel, processor);

        return CompletableFuture
                .supplyAsync(processor::process, FORK_JOIN_POOL)
                .thenCompose(result -> continueExploring(
                        processor, result, results, listener, nextNodeId, nodeId));
    }

    private static <C extends ROContext> CompletableFuture<Void> continueExploring(
            ROPro<C> processor,
            ROResult result,
            Set<ROResult> results,
            ROSearchListener listener,
            AtomicLong nextNodeId,
            long nodeId) {

        ROStatus status = processor.getStatus();
        notifyCompleted(listener, nodeId, status, result);

        return switch (status) {
            case FAILED -> CompletableFuture.completedFuture(null);

            case RESOLVED -> {
                results.add(result);
                yield CompletableFuture.completedFuture(null);
            }

            case PROCESSING -> {
                List<CompletableFuture<Void>> tasks = processor.createBranches().stream()
                        .map(branch -> exploreAsync(
                                branch.processor(),
                                branch.label(),
                                nodeId,
                                results,
                                listener,
                                nextNodeId))
                        .toList();

                yield CompletableFuture.allOf(
                        tasks.toArray(CompletableFuture[]::new));
            }
        };
    }

    private static void notifyCreated(
            ROSearchListener listener,
            long nodeId,
            Long parentId,
            String label,
            ROPro<?> processor) {
        try {
            listener.onNodeCreated(nodeId, parentId, label, processor);
        } catch (RuntimeException exception) {
            System.err.println("Search listener failed while creating node: " + exception);
        }
    }

    private static void notifyCompleted(
            ROSearchListener listener,
            long nodeId,
            ROStatus status,
            ROResult result) {
        try {
            listener.onNodeCompleted(nodeId, status, result);
        } catch (RuntimeException exception) {
            System.err.println("Search listener failed while completing node: " + exception);
        }
    }
}
