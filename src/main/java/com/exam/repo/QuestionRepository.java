package com.exam.repo;

import com.exam.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question findFirstByOrderByOrderAsc();
    List<Question> findByOrderGreaterThanOrderByOrderAsc(Integer order);

    Question findFirstByOrderGreaterThanOrderByOrderAsc(Integer orderValue);

}
