package org.ubunifu.armania_rofinder;

import java.util.List;

public abstract class ROPro<C extends ROContext> {
    private final C context;

    public ROPro(C context) {
        this.context = context;
    }

    public C getContext() {
        return context;
    }

    public abstract ROResult process();
    public abstract List<ROBranch<C>> createBranches();
    public abstract ROStatus getStatus();
}
