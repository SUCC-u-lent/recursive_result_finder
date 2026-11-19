package org.ubunifu.armania_rofinder.example;

import org.ubunifu.armania_rofinder.ROContext;

public class CombatContext extends ROContext {
    final int usedTurns;
    final int actorHealth, allyHealth, enemyHealth;
    private final boolean guarding;

    public CombatContext(int usedTurns, int actorHealth, int allyHealth, int enemyHealth) {
        this(null, usedTurns, actorHealth, allyHealth, enemyHealth, false);
    }

    public CombatContext(
            CombatContext previousContext,
            int usedTurns,
            int actorHealth,
            int allyHealth,
            int enemyHealth,
            boolean guarding) {
        super(previousContext);
        this.usedTurns = usedTurns;
        this.actorHealth = actorHealth;
        this.allyHealth = allyHealth;
        this.enemyHealth = enemyHealth;
        this.guarding = guarding;
    }

    @Override
    protected ROContext deepClone() {
        return new CombatContext(null, usedTurns, actorHealth, allyHealth, enemyHealth, guarding);
    }

    public CombatContext(int actorHealth, int allyHealth, int enemyHealth) {
        this(0, actorHealth, allyHealth, enemyHealth);
    }

    public CombatContext hurtEnemy(int amount) {
        return next(actorHealth, allyHealth, enemyHealth - amount, false);
    }

    public CombatContext healAlly(int amount) {
        return next(actorHealth, allyHealth + amount, enemyHealth, false);
    }

    public CombatContext healSelf(int amount) {
        return next(actorHealth + amount, allyHealth, enemyHealth, false);
    }

    /** Arms a one-use guard against the next incoming attack on the actor. */
    public CombatContext guard() {
        return next(actorHealth, allyHealth, enemyHealth, true);
    }

    /** Applies the next incoming attack, consuming guard if it is active. */
    public CombatContext receiveAttack(int damage) {
        int resultingHealth = guarding ? actorHealth : actorHealth - damage;
        return next(resultingHealth, allyHealth, enemyHealth, false);
    }

    public boolean isGuarding() {
        return guarding;
    }

    private CombatContext next(int actorHealth, int allyHealth, int enemyHealth, boolean guarding) {
        return new CombatContext(this, usedTurns + 1, actorHealth, allyHealth, enemyHealth, guarding);
    }

    @Override
    public String toString() {
        return "Turn " + usedTurns
                + " | Actor HP: " + actorHealth
                + " | Ally HP: " + allyHealth
                + " | Enemy HP: " + enemyHealth
                + " | Guarding: " + guarding;
    }
}
