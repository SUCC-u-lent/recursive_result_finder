package org.ubunifu.armania_rofinder.example;

import org.ubunifu.armania_rofinder.ROContext;

public class CombatContext extends ROContext {
    final int actorHealth, allyHealth, enemyHealth;
    public CombatContext(int actorHealth, int allyHealth, int enemyHealth) {
        this.actorHealth = actorHealth;
        this.allyHealth = allyHealth;
        this.enemyHealth = enemyHealth;
    }

    public CombatContext hurtEnemy(int amount) {
        return new CombatContext(
                this.actorHealth,
                this.allyHealth,
                this.enemyHealth - amount
        );
    }

    public CombatContext healAlly(int amount) {
        return new CombatContext(
                this.actorHealth,
                this.allyHealth + amount,
                this.enemyHealth
        );
    }

    public CombatContext healSelf(int amount) {
        return new CombatContext(
                this.actorHealth + amount,
                this.allyHealth,
                this.enemyHealth
        );
    }
}
