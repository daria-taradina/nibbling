package com.nibbling.server.service;

import com.nibbling.server.entity.Tier;
import com.nibbling.server.service.QuizScoring.Item;
import com.nibbling.server.service.QuizScoring.Outcome;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuizScoringTest {

    private final QuizScoring scoring = new QuizScoring(90, 60);

    @Test
    void allCorrectIsFeast() {
        Outcome o = scoring.score(List.of(new Item(1, true), new Item(1, true), new Item(2, true),
                new Item(2, true), new Item(3, true)), true);
        assertEquals(Tier.FEAST, o.tier());
        assertEquals(25, o.xp()); // 5 correct * 5 XP each
        assertEquals(5, o.correctCount());
    }

    @Test
    void missingOnlyAnEasyOneDropsToMeal() {
        // weights 1,1,2,2,3 -> 8/9 = 88.9%, just under the 90% line
        Outcome o = scoring.score(List.of(new Item(1, false), new Item(1, true), new Item(2, true),
                new Item(2, true), new Item(3, true)), true);
        assertEquals(Tier.MEAL, o.tier());
        assertEquals(20, o.xp()); // 4 correct * 5 XP each
    }

    @Test
    void exactlyNinetyPercentIsFeast() {
        List<Item> items = new java.util.ArrayList<>(Collections.nCopies(9, new Item(1, true)));
        items.add(new Item(1, false));
        assertEquals(Tier.FEAST, scoring.score(items, true).tier());
    }

    @Test
    void exactlySixtyPercentIsMeal() {
        Outcome o = scoring.score(List.of(new Item(1, true), new Item(1, true), new Item(1, true),
                new Item(1, false), new Item(1, false)), true);
        assertEquals(Tier.MEAL, o.tier());
        assertEquals(15, o.xp()); // 3 correct * 5 XP each
    }

    @Test
    void justUnderSixtyPercentIsSnack() {
        Outcome o = scoring.score(List.of(new Item(1, true), new Item(1, true), new Item(1, false),
                new Item(1, false), new Item(1, false)), true);
        assertEquals(Tier.SNACK, o.tier());
        assertEquals(10, o.xp()); // 2 correct * 5 XP each
    }

    @Test
    void zeroCorrectButAnsweredIsSnackWithZeroXp() {
        Outcome o = scoring.score(List.of(new Item(1, false), new Item(1, false), new Item(2, false),
                new Item(2, false), new Item(3, false)), true);
        assertEquals(Tier.SNACK, o.tier());
        assertEquals(0, o.xp()); // 0 correct * 5 XP
    }

    @Test
    void harderQuestionsCountMore() {
        // only the hard one right: 3/9 = 33% -> SNACK; hard + both mediums: 7/9 = 78% -> MEAL
        assertEquals(Tier.SNACK, scoring.score(List.of(new Item(1, false), new Item(1, false),
                new Item(2, false), new Item(2, false), new Item(3, true)), true).tier());
        assertEquals(Tier.MEAL, scoring.score(List.of(new Item(1, false), new Item(1, false),
                new Item(2, true), new Item(2, true), new Item(3, true)), true).tier());
    }

    @Test
    void answeringNothingIsAMissWithNoXp() {
        Outcome o = scoring.score(List.of(new Item(1, false), new Item(2, false)), false);
        assertNull(o.tier());
        assertEquals(0, o.xp());
    }
}
