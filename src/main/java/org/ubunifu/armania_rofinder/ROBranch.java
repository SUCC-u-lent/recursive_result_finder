package org.ubunifu.armania_rofinder;

public class ROBranch<C extends ROContext> {
    final String label;
    final ROPro<C> processor;
    public ROBranch(String label, ROPro<C> processor) {
        this.label = label;
        this.processor = processor;
    }
    public ROBranch(ROPro<C> processor) {
        this("Branch", processor);
    }

    public final String getLabel() {
        return label;
    }

    public final ROPro<C> getProcessor() {
        return processor;
    }
}
