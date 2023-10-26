package com.exam.repo;

import com.exam.models.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question findFirstByOrderByQuestionOrderAsc();
    List<Question> findByQuestionOrderGreaterThanOrderByQuestionOrderAsc(Integer order);

    Question findFirstByQuestionOrderGreaterThanOrderByQuestionOrderAsc(Integer orderValue);

    @Query("SELECT q FROM Question q WHERE q.card.id = :cardId ORDER BY q.id ASC")
    List<Question> findQuestionsByCardId(@Param("cardId") Long cardId, Pageable pageable);

}
