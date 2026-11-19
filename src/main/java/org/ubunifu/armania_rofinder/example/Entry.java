package org.ubunifu.armania_rofinder.example;

import org.jetbrains.annotations.Nullable;
import org.ubunifu.armania_rofinder.*;

import java.util.concurrent.*;

public class Entry {
    public static void main(String[] args) {
        System.out.println("Beginning Processing..");
        CombatContext context = new CombatContext(100,100,100);
        CombatProcessor processor = new CombatProcessor(context);
        CompletableFuture<@Nullable ROResult> resultCompletableFuture = RecursionFinder.findResult(processor);
        @Nullable ROResult result = resultCompletableFuture.orTimeout(1, TimeUnit.MINUTES).join();
        if (result == null) return;
        System.out.println("Weight: "+result.getWeight()+" Heuristics: "+result.getEffectiveHeuristic()+" error: "+result.getError());
    }
}
