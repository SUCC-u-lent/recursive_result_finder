package org.ubunifu.armania_rofinder.example;

import org.ubunifu.armania_rofinder.*;

import java.util.*;

public class CombatProcessor extends ROPro<CombatContext> {

    private final CombatContext context;

    public CombatProcessor(CombatContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public ROResult process() {
        double weight =
                context.actorHealth
                + context.allyHealth
                - context.enemyHealth;

        return new ROResult(
                weight,
                weight,
                0
        );
    }

    @Override
    public ROStatus getStatus() {

        if (context.actorHealth <= 0) {
            return ROStatus.FAILED;
        }

        if (context.enemyHealth <= 0) {
            return ROStatus.RESOLVED;
        }

        return ROStatus.PROCESSING;
    }

    enum Move {
        ATTACK {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                ROPro<CombatContext> pro = new CombatProcessor(context.hurtEnemy(1));
                return new ROBranch<>(pro);
            }
        },
        HEAL_SELF {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                ROPro<CombatContext> pro = new CombatProcessor(context.healSelf(1));
                return new ROBranch<>(pro);
            }
        },
        HEAL_OTHER {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                ROPro<CombatContext> pro = new CombatProcessor(context.healAlly(1));
                return new ROBranch<>(pro);
            }
        },
        WAIT {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                return new ROBranch<>(new CombatProcessor(context));
            }
        }
        ;
        public abstract ROBranch<CombatContext> createBranch(CombatContext context);
    }

    @Override
    public List<ROBranch<CombatContext>> createBranches() {
        return Arrays.stream(Move.values())
                .map(e->e.createBranch(this.context))
                .toList();
    }
}