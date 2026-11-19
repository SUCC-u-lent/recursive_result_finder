package org.ubunifu.armania_rofinder;

public final class ROResult {
    final double weight, heuristic;
    final double error;

    public double getEffectiveHeuristic() {
        return this.heuristic + this.error;
    }

    public ROResult(double weight, double heuristic, double error) {
        this.weight = weight;
        this.heuristic = heuristic;
        this.error = error;
    }
}
