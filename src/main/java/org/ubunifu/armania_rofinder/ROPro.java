package org.ubunifu.armania_rofinder;

import java.util.List;

public abstract class ROPro {
    private final ROContext context;
    public ROPro(ROContext context) { this.context = context; }

    public abstract ROResult process();
    public abstract List<ROBranch> createBranches();
    public abstract ROStatus getStatus();
}
