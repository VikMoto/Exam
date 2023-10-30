package com.exam.repo;

import com.exam.models.Answer;
import com.exam.models.AnsweredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnsweredQuestionRepository extends JpaRepository<AnsweredQuestion, Long> {

}
