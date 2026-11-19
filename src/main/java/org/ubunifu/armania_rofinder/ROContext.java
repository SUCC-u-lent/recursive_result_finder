package org.ubunifu.armania_rofinder;

import java.util.ArrayList;

public abstract class ROContext {
    ROResultPath paths;
    private final ROContext previousContext;

    public ROContext() { this(null, new ROResultPath(null, new ArrayList<>())); }

    public ROContext(ROContext previousContext, ROResultPath paths) {
        this.previousContext = previousContext;
        this.paths = paths;
    }

    protected abstract ROContext deepClone();

    public ROContext getPreviousContext() {
        return previousContext;
    }

    public ROResultPath getPaths() {
        return paths;
    }
}
