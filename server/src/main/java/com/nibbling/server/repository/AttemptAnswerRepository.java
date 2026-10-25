package com.nibbling.server.repository;

import com.nibbling.server.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    List<AttemptAnswer> findByAttemptId(UUID attemptId);

    @Modifying(flushAutomatically = true)
    @Query("delete from AttemptAnswer a where a.attemptId = :attemptId")
    void deleteAllForAttempt(@Param("attemptId") UUID attemptId);
}
