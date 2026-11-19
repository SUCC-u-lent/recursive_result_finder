package org.ubunifu.armania_rofinder;

public abstract class ROContext {
    ROResultPath paths;
    private final ROContext previousContext;
    private ROResult result;

    public ROContext() {
        this(null, null);
    }

    public ROContext(ROContext previousContext) {
        this(previousContext, null);
    }

    public ROContext(ROContext previousContext, ROResultPath paths) {
        this.previousContext = previousContext;
        this.paths = paths;
    }

    protected abstract ROContext deepClone();

    /**
     * Returns the result produced by the processor that created this context.
     * The root context has no previous result, so this returns null there.
     */
    public ROResult getPreviousResult() {
        return previousContext == null ? null : previousContext.result;
    }

    /**
     * Stores this context's processor result so child contexts can read it.
     * Called by RecursionFinder after process() completes and before branches
     * are created.
     */
    void setResult(ROResult result) {
        this.result = result;
    }

    public ROContext getPreviousContext() {
        return previousContext;
    }

    public ROResultPath getPaths() {
        return paths;
    }
}
