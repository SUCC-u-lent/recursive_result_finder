package org.ubunifu.armania_rofinder;

import java.util.List;
import java.util.Objects;

/**
 * A resolved result together with the exact branch route that produced it.
 * The route starts with ROOT and then lists each selected branch in order.
 */
public record ROResultPath(ROResult result, List<ROPathStep> path) {
    public ROResultPath {
        Objects.requireNonNull(result, "result");
        path = List.copyOf(path);
    }
}
