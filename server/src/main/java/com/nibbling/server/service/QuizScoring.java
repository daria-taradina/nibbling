package com.nibbling.server.service;

import com.nibbling.server.entity.Tier;

import java.util.List;

// Pure scoring rules, no Spring or database, so they're trivial to unit test.
//
//   score = sum(weight of correct questions) / sum(weight of all questions)
//   weight = question difficulty (1, 2 or 3)
//
//   score >= feastMinPercent  -> FEAST
//   score >= mealMinPercent   -> MEAL
//   otherwise                 -> SNACK   (answering anything earns at least this)
//   no answers at all         -> no tier (Miss), 0 XP
//
// Percent thresholds are compared with integer math to avoid floating-point edge cases.
public final class QuizScoring {

    public record Item(int difficulty, boolean correct) {}

    public record Outcome(int correctCount, int total, double score, Tier tier, int xp) {}

    private final int feastMinPercent;
    private final int mealMinPercent;
    private final int feastXp;
    private final int mealXp;
    private final int snackXp;

    public QuizScoring(int feastMinPercent, int mealMinPercent, int feastXp, int mealXp, int snackXp) {
        if (mealMinPercent > feastMinPercent) {
            throw new IllegalArgumentException("meal threshold must not exceed feast threshold");
        }
        this.feastMinPercent = feastMinPercent;
        this.mealMinPercent = mealMinPercent;
        this.feastXp = feastXp;
        this.mealXp = mealXp;
        this.snackXp = snackXp;
    }

    public Outcome score(List<Item> items, boolean anyAnswered) {
        int correctCount = 0;
        int correctWeight = 0;
        int totalWeight = 0;
        for (Item item : items) {
            totalWeight += item.difficulty();
            if (item.correct()) {
                correctCount++;
                correctWeight += item.difficulty();
            }
        }
        double score = totalWeight == 0 ? 0.0 : (double) correctWeight / totalWeight;

        if (!anyAnswered) {
            return new Outcome(correctCount, items.size(), score, null, 0);
        }
        Tier tier;
        if (correctWeight * 100 >= feastMinPercent * totalWeight) {
            tier = Tier.FEAST;
        } else if (correctWeight * 100 >= mealMinPercent * totalWeight) {
            tier = Tier.MEAL;
        } else {
            tier = Tier.SNACK;
        }
        return new Outcome(correctCount, items.size(), score, tier, xpFor(tier));
    }

    private int xpFor(Tier tier) {
        return switch (tier) {
            case FEAST -> feastXp;
            case MEAL -> mealXp;
            case SNACK -> snackXp;
        };
    }
}
