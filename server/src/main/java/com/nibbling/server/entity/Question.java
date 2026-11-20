package com.nibbling.server.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

// One quiz question. Content is loaded by the R__seed_questions migration, so
// the app only reads these rows. Never send isCorrect/explanation to the client
// before an attempt is submitted.
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", length = 100, nullable = false, unique = true)
    private String slug;

    @Column(name = "topic", length = 50, nullable = false)
    private String topic;

    // 1 = easy, 2 = medium, 3 = hard. Also the question's weight in the score.
    @Column(name = "difficulty", nullable = false)
    private int difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", length = 20, nullable = false)
    private QuestionKind kind;

    @Column(name = "prompt_md", nullable = false)
    private String promptMd;

    @Column(name = "code_snippet")
    private String codeSnippet;

    @Column(name = "explanation_md", nullable = false)
    private String explanationMd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private QuestionStatus status;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<>();

    protected Question() {
        // required by JPA
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTopic() { return topic; }
    public int getDifficulty() { return difficulty; }
    public QuestionKind getKind() { return kind; }
    public String getPromptMd() { return promptMd; }
    public String getCodeSnippet() { return codeSnippet; }
    public String getExplanationMd() { return explanationMd; }
    public QuestionStatus getStatus() { return status; }
    public List<QuestionOption> getOptions() { return options; }
}
