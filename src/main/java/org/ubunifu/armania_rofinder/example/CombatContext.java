package org.ubunifu.armania_rofinder.example;

import org.ubunifu.armania_rofinder.ROContext;

public class CombatContext extends ROContext {
    final int usedTurns;
    final int actorHealth, allyHealth, enemyHealth;

    public CombatContext(int usedTurns, int actorHealth, int allyHealth, int enemyHealth) {
        super();
        this.usedTurns = usedTurns;
        this.actorHealth = actorHealth;
        this.allyHealth = allyHealth;
        this.enemyHealth = enemyHealth;
    }

    public CombatContext(CombatContext previousContext, int usedTurns, int actorHealth, int allyHealth, int enemyHealth) {
        super(previousContext);
        this.usedTurns = usedTurns;
        this.actorHealth = actorHealth;
        this.allyHealth = allyHealth;
        this.enemyHealth = enemyHealth;
    }

    @Override
    protected ROContext deepClone() {
        return new CombatContext(usedTurns, actorHealth, allyHealth, enemyHealth);
    }

    public CombatContext(int actorHealth, int allyHealth, int enemyHealth) {
        this(0, actorHealth, allyHealth, enemyHealth);
    }

    public CombatContext hurtEnemy(int amount) {
        return new CombatContext(this, usedTurns + 1, actorHealth, allyHealth, enemyHealth - amount);
    }

    public CombatContext healAlly(int amount) {
        return new CombatContext(this, usedTurns + 1, actorHealth, allyHealth + amount, enemyHealth);
    }

    public CombatContext healSelf(int amount) {
        return new CombatContext(this, usedTurns + 1, actorHealth + amount, allyHealth, enemyHealth);
    }

    public CombatContext guard() {
        return new CombatContext(this, usedTurns + 1, actorHealth, allyHealth, enemyHealth);
    }

    @Override
    public String toString() {
        return "Turn " + usedTurns
                + " | Actor HP: " + actorHealth
                + " | Ally HP: " + allyHealth
                + " | Enemy HP: " + enemyHealth;
    }
}
