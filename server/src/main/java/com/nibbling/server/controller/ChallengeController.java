package com.nibbling.server.controller;

import com.nibbling.server.dto.SaveDraftRequest;
import com.nibbling.server.dto.SaveDraftResponse;
import com.nibbling.server.dto.SubmitRequest;
import com.nibbling.server.dto.TodayResponse;
import com.nibbling.server.security.UserPrincipal;
import com.nibbling.server.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// All routes need a valid JWT (SecurityConfig: anyRequest().authenticated()).
@RestController
@RequestMapping("/api/challenges/today")
public class ChallengeController {

    private final QuizService quizService;

    public ChallengeController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<TodayResponse> today(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizService.getToday(principal.getId()));
    }

    @PostMapping("/start")
    public ResponseEntity<TodayResponse> start(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizService.start(principal.getId()));
    }

    @PutMapping("/draft")
    public ResponseEntity<SaveDraftResponse> saveDraft(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody SaveDraftRequest request) {
        return ResponseEntity.ok(quizService.saveDraft(principal.getId(), request.answers()));
    }

    @PostMapping("/submit")
    public ResponseEntity<TodayResponse> submit(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestBody(required = false) SubmitRequest request) {
        return ResponseEntity.ok(quizService.submit(principal.getId(),
                request == null ? null : request.answers()));
    }
}
