package org.ubunifu.armania_rofinder;

import java.util.Objects;

public record ROPathStep(long nodeId, String branchLabel) {
    public ROPathStep {
        Objects.requireNonNull(branchLabel, "branchLabel");
    }
}
