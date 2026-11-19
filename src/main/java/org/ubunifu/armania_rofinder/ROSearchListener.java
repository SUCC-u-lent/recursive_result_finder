package org.ubunifu.armania_rofinder;

/**
 * Receives live notifications as the recursive finder visits processors.
 * Implementations should be thread-safe; callbacks may arrive from worker threads.
 */
public interface ROSearchListener {

    ROSearchListener NONE = new ROSearchListener() {};

    default void onNodeCreated(
            long nodeId,
            Long parentId,
            String branchLabel,
            ROPro<?> processor) {}

    default void onNodeCompleted(
            long nodeId,
            ROStatus status,
            ROResult result) {}

    /** Called after the full search has completed and its best result is known. */
    default void onSearchCompleted(ROResultPath bestResult) {}
}
