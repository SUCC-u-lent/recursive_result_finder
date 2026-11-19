package org.ubunifu.armania_rofinder.example;

import org.jetbrains.annotations.Nullable;
import org.ubunifu.armania_rofinder.*;
import org.ubunifu.armania_rofinder.example.visualizer.ROSearchGraphVisualizer;

import java.util.concurrent.*;

public class Entry {
    public static void main(String[] args) {
        System.out.println("Beginning Processing..");

        CombatContext context = new CombatContext(100, 100, 3);
        CombatProcessor processor = new CombatProcessor(context);

        ROSearchGraphVisualizer visualizer = new ROSearchGraphVisualizer();
        visualizer.showWindow();

        CompletableFuture<@Nullable ROResultPath> resultCompletableFuture =
                RecursionFinder.findBestResultWithPath(processor, visualizer);

        @Nullable ROResultPath best = resultCompletableFuture
                .orTimeout(1, TimeUnit.MINUTES)
                .join();

        if (best == null) {
            System.out.println("No resolved result found.");
            return;
        }

        ROResult result = best.result();
        System.out.println("Weight: " + result.getWeight()
                + " Heuristics: " + result.getEffectiveHeuristic()
                + " error: " + result.getError());

        System.out.println("Winning route:");
        for (int i = 1; i < best.path().size(); i++) {
            System.out.println(i + ". " + best.path().get(i).branchLabel());
        }
    }
}
