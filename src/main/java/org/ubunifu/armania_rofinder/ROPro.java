package org.ubunifu.armania_rofinder;

import java.util.HashMap;

public abstract class ROPro {
    public HashMap<ROPro, ROContext> createBranches(ROContext input) {
        return null;
    }

    public abstract RecursionResult process(ROContext input);
}
