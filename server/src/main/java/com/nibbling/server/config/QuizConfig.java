package com.nibbling.server.config;

import com.nibbling.server.service.QuizScoring;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
public class QuizConfig {

    // One injectable clock so tests can fix "now".
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public QuizScoring quizScoring(
            @Value("${quiz.tier.feast-min-percent:90}") int feastMinPercent,
            @Value("${quiz.tier.meal-min-percent:60}") int mealMinPercent
    ) {
        return new QuizScoring(feastMinPercent, mealMinPercent);
    }
}
