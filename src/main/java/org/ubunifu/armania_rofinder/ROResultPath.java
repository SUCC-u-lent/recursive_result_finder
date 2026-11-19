package org.ubunifu.armania_rofinder;

import java.util.List;
import java.util.Objects;

public record ROResultPath(ROResult result, List<ROPathStep> path, ROStatus status) {
    public ROResultPath {
        Objects.requireNonNull(result, "result");
        path = List.copyOf(path);
    }
}
