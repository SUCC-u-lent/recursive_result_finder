package org.ubunifu.armania_rofinder;

public abstract class ROContext {
    private final ROContext previousContext;
    private ROResult result;

    public ROContext() {
        this(null);
    }

    public ROContext(ROContext previousContext) {
        this.previousContext = previousContext;
    }

    protected abstract ROContext deepClone();

    public ROResult getPreviousResult() {
        return previousContext == null ? null : previousContext.result;
    }

    void setResult(ROResult result) {
        this.result = result;
    }

    public ROContext getPreviousContext() {
        return previousContext;
    }
}
