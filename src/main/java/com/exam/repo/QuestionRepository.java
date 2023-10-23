package com.exam.repo;

import com.exam.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question findFirstByOrderByQuestionOrderAsc();
    List<Question> findByQuestionOrderGreaterThanOrderByQuestionOrderAsc(Integer order);

    Question findFirstByQuestionOrderGreaterThanOrderByQuestionOrderAsc(Integer orderValue);

}
