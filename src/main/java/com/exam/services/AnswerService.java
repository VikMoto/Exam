package com.exam.services;

import com.exam.models.Answer;
import com.exam.repo.AnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AnswerService {
    private final AnswerRepository answerRepository;
    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }
    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }
    public void deleteAnswer(Long answerId) {
        answerRepository.deleteById(answerId);
    }
    public Optional<Object> getAnswerById(Long answerId) {
        return Optional.of(answerRepository.findById(answerId));
    }
}
