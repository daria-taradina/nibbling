package com.nibbling.server.repository;

import com.nibbling.server.entity.Question;
import com.nibbling.server.entity.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByStatus(QuestionStatus status);

    // Options are lazy and open-in-view is off, so fetch them in the same query.
    @Query("select distinct q from Question q left join fetch q.options where q.id in :ids")
    List<Question> findWithOptionsByIdIn(@Param("ids") Collection<Long> ids);
}
