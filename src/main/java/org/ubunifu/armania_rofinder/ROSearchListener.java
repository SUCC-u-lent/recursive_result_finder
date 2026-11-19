package org.ubunifu.armania_rofinder;

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

    default void onSearchCompleted(ROResultPath bestResult) {}
}
