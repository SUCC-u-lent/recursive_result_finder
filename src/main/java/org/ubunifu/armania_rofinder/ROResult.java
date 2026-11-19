package org.ubunifu.armania_rofinder;

public final class ROResult {
    final ROPro<? extends ROContext> usedProcessor;
    final ROContext usedContext;
    final double weight, heuristic;
    final double error;

    public ROResult(ROPro<? extends ROContext> usedProcessor, ROContext usedContext, double weight, double heuristic, double error) {
        this.usedProcessor = usedProcessor;
        this.usedContext = usedContext;
        this.weight = weight;
        this.heuristic = heuristic;
        this.error = error;
    }

    public double getEffectiveHeuristic() {
        return this.heuristic + this.error;
    }
    public double getEffectiveWeight() {
        return this.weight + this.error;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getHeuristic() {
        return this.heuristic;
    }

    public double getError() {
        return this.error;
    }

    public <C extends ROContext, P extends ROPro<C>> P getUsedProcessor() {
        //noinspection unchecked
        return (P) usedProcessor;
    }

    public <C extends ROContext> C getUsedContext() {
        //noinspection unchecked
        return (C) usedContext;
    }
}
