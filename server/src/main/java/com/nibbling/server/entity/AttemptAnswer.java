package com.nibbling.server.entity;

import jakarta.persistence.*;

import java.util.UUID;

// One selected option in an attempt's saved draft. The whole draft is replaced
// on every save, and the rows are what gets graded at submit time.
@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_key", length = 10, nullable = false)
    private String optionKey;

    protected AttemptAnswer() {
        // required by JPA
    }

    public AttemptAnswer(UUID attemptId, Long questionId, String optionKey) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.optionKey = optionKey;
    }

    public UUID getAttemptId() { return attemptId; }
    public Long getQuestionId() { return questionId; }
    public String getOptionKey() { return optionKey; }
}
