package org.ubunifu.armania_rofinder;

import java.util.List;
import java.util.Objects;

/**
 * A single step in the route taken by the recursive search.
 * The root step is labelled "ROOT"; subsequent steps use ROBranch labels.
 */
public record ROPathStep(long nodeId, String branchLabel) {
    public ROPathStep {
        Objects.requireNonNull(branchLabel, "branchLabel");
    }
}
