package com.nibbling.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // "a", "b", ... stable id the client sends back as an answer.
    @Column(name = "option_key", length = 10, nullable = false)
    private String optionKey;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    protected QuestionOption() {
        // required by JPA
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public String getOptionKey() { return optionKey; }
    public int getPosition() { return position; }
    public String getLabel() { return label; }
    public boolean isCorrect() { return correct; }
}
