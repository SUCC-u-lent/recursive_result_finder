package org.ubunifu.armania_rofinder.example;

import org.ubunifu.armania_rofinder.*;

import java.util.*;

public class CombatContext extends ROContext {
    final int actorHealth, allyHealth, enemyHealth;

    public CombatContext(int usedTurns, HashMap<ROContext, ROResult> history, int actorHealth, int allyHealth, int enemyHealth) {
        super(usedTurns, history);
        this.actorHealth = actorHealth;
        this.allyHealth = allyHealth;
        this.enemyHealth = enemyHealth;
    }

    public CombatContext(int actorHealth, int allyHealth, int enemyHealth) {
        this(0, new HashMap<>(), actorHealth, allyHealth, enemyHealth);
    }

    public CombatContext hurtEnemy(int amount) {
        return new CombatContext(
                this.getUsedTurns() + 1,
                this.addContext(this, )
                this.actorHealth,
                this.allyHealth,
                this.enemyHealth - amount
        );
    }

    public CombatContext healAlly(int amount) {
        return new CombatContext(
                this.getUsedTurns() + 1,
                this.actorHealth,
                this.allyHealth + amount,
                this.enemyHealth
        );
    }

    public CombatContext healSelf(int amount) {
        return new CombatContext(
                this.getUsedTurns() + 1,
                this.actorHealth + amount,
                this.allyHealth,
                this.enemyHealth
        );
    }

    @Override
    public String toString() {
        return "Turn " + usedTurns
                + " | Actor HP: " + actorHealth
                + " | Ally HP: " + allyHealth
                + " | Enemy HP: " + enemyHealth;
    }
}
