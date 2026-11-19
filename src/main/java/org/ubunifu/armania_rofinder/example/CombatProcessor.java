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
                - (context.enemyHealth * 2); // enemy weight more.

        return new ROResult(this, context, weight, weight, 0);
    }

    @Override
    public ROStatus getStatus() {
        if (context.actorHealth <= 0 || context.usedTurns >= 5) {
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
                return new ROBranch<>(name(), new CombatProcessor(context.hurtEnemy(1)));
            }
        },
        HEAL_SELF {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                return new ROBranch<>(name(), new CombatProcessor(context.healSelf(1)));
            }
        },
        HEAL_OTHER {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                return new ROBranch<>(name(), new CombatProcessor(context.healAlly(1)));
            }
        },
        GUARD {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                return new ROBranch<>(name(), new CombatProcessor(context.guard()));
            }
        },
        INCOMING_ATTACK {
            @Override
            public ROBranch<CombatContext> createBranch(CombatContext context) {
                return new ROBranch<>(name(), new CombatProcessor(context.receiveAttack(1)));
            }
        };

        public abstract ROBranch<CombatContext> createBranch(CombatContext context);
    }

    @Override
    public List<ROBranch<CombatContext>> createBranches() {
        return Arrays.stream(Move.values())
                .map(move -> move.createBranch(context))
                .toList();
    }
}
