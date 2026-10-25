package com.nibbling.server.dto;

import java.util.List;

// A question as shown to the player. Deliberately has no correct answer or explanation.
public record QuestionView(
        long id,
        int position,
        String topic,
        int difficulty,
        String kind,
        String promptMd,
        String codeSnippet,
        List<OptionView> options
) {}
