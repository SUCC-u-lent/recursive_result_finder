package org.ubunifu.armania_rofinder;

import java.util.*;

public abstract class ROContext {
    protected final int usedTurns;
    protected final Map<ROContext, ROResult> previousContexts;

    public ROContext(int usedTurns, Map<ROContext, ROResult> previousContexts) {
        this.usedTurns = usedTurns;
        this.previousContexts = previousContexts;
    }

    public int getUsedTurns() {
        return usedTurns;
    }

    protected final Map<ROContext, ROResult> addContext(ROContext context, ROResult result) {
        HashMap<ROContext, ROResult> copied = new HashMap<>(this.previousContexts);
        copied.put(context, result);
        return new HashMap<>(copied);
    }

    public Map<ROContext, ROResult> getPreviousContexts() {
        return previousContexts;
    }
}
