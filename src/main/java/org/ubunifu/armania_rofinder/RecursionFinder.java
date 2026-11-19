package org.ubunifu.armania_rofinder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class RecursionFinder {

    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    private RecursionFinder() {}
    public static <C extends ROContext> CompletableFuture<@Nullable ROResultPath>
    findBestResultWithPath(ROPro<C> processor) {
        return findBestResultWithPath(processor, ROSearchListener.NONE);
    }
    public static <C extends ROContext> CompletableFuture<@Nullable ROResultPath> findBestResultWithPath(ROPro<C> processor, ROSearchListener listener) {
        return findResultsWithPathsAsync(processor, listener)
                .thenApply(results -> {
                    ROResultPath best = results.stream()
                        .min(Comparator.comparing(c->c.result().getEffectiveHeuristic()))
                        .orElse(null);
                    notifySearchCompleted(listener, best);
                    return best;
                });
    }
    public static <C extends ROContext> CompletableFuture<Set<ROResultPath>>
    findResultsWithPathsAsync(ROPro<C> processor) {
        return findResultsWithPathsAsync(processor, ROSearchListener.NONE);
    }

    public static <C extends ROContext> CompletableFuture<Set<ROResultPath>>
    findResultsWithPathsAsync(ROPro<C> processor, ROSearchListener listener) {
        Objects.requireNonNull(listener, "listener");

        Set<ROResultPath> results = ConcurrentHashMap.newKeySet();
        AtomicLong nextNodeId = new AtomicLong(1);
        long rootId = 0;
        List<ROPathStep> rootPath = List.of(new ROPathStep(rootId, "ROOT"));

        notifyCreated(listener, rootId, null, "ROOT", processor);

        return CompletableFuture
                .supplyAsync(processor::process, FORK_JOIN_POOL)
                .thenCompose(result -> continueExploring(
                        processor, result, results, listener, nextNodeId, rootId, rootPath))
                .thenApply(ignored -> results);
    }
    public static <C extends ROContext> CompletableFuture<Set<ROResult>> findResultsAsync(
            ROPro<C> processor) {
        return findResultsAsync(processor, ROSearchListener.NONE);
    }

    public static <C extends ROContext> CompletableFuture<Set<ROResult>> findResultsAsync(
            ROPro<C> processor,
            ROSearchListener listener) {
        return findResultsWithPathsAsync(processor, listener)
                .thenApply(paths -> {
                    Set<ROResult> results = ConcurrentHashMap.newKeySet();
                    paths.forEach(path -> results.add(path.result()));
                    return results;
                });
    }

    private static <C extends ROContext> CompletableFuture<Void> exploreAsync(
            ROPro<C> processor,
            String branchLabel,
            long parentId,
            List<ROPathStep> parentPath,
            Set<ROResultPath> results,
            ROSearchListener listener,
            AtomicLong nextNodeId) {

        long nodeId = nextNodeId.getAndIncrement();
        List<ROPathStep> path = new ArrayList<>(parentPath);
        path.add(new ROPathStep(nodeId, branchLabel));
        List<ROPathStep> immutablePath = List.copyOf(path);

        notifyCreated(listener, nodeId, parentId, branchLabel, processor);

        return CompletableFuture
                .supplyAsync(processor::process, FORK_JOIN_POOL)
                .thenCompose(result -> continueExploring(
                        processor, result, results, listener, nextNodeId, nodeId, immutablePath));
    }

    private static <C extends ROContext> CompletableFuture<Void> continueExploring(
            ROPro<C> processor,
            ROResult result,
            Set<ROResultPath> results,
            ROSearchListener listener,
            AtomicLong nextNodeId,
            long nodeId,
            List<ROPathStep> path) {
        processor.getContext().setResult(result);

        ROStatus status = processor.getStatus();
        notifyCompleted(listener, nodeId, status, result);

        return switch (status) {
            case FAILED -> CompletableFuture.completedFuture(null);

            case RESOLVED -> {
                results.add(new ROResultPath(result, path));
                yield CompletableFuture.completedFuture(null);
            }

            case PROCESSING -> {
                List<CompletableFuture<Void>> tasks = processor.createBranches().stream()
                        .map(branch -> exploreAsync(
                                branch.processor(),
                                branch.label(),
                                nodeId,
                                path,
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

    private static void notifySearchCompleted(
            ROSearchListener listener,
            @Nullable ROResultPath bestResult) {
        try {
            listener.onSearchCompleted(bestResult);
        } catch (RuntimeException exception) {
            System.err.println("Search listener failed after search completion: " + exception);
        }
    }
}
