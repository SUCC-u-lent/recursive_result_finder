package org.ubunifu.armania_rofinder;

public record ROBranch<C extends ROContext>(String label, ROPro<C> processor) {

    public ROBranch(ROPro<C> processor) {
        this("Branch", processor);
    }
}
