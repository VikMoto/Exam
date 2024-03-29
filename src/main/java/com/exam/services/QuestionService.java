package com.exam.services;


import com.exam.models.Answer;
import com.exam.models.Question;
import com.exam.repo.AnswerRepository;
import com.exam.repo.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public void saveQuestion(Question question) {
        Question savedQuestion = questionRepository.save(question);
        // In reality, you'd persist this question to a database
        // Iterate through the answers and set their associated question
        for (Answer answer : question.getAnswers()) {
            answer.setQuestion(savedQuestion);
            answerRepository.save(answer); // Persist each answer to the database
        }
        System.out.println("Saving question: " + question.getContent());
    }

    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public Question getQuestionById(Long questionId) {

        Question question = questionRepository.findById(questionId).orElseThrow();
        List<Answer> sortedAnswers = question.getAnswers()
                .stream()
                .sorted(Comparator.comparing(Answer::getId))
                .toList();
        // Now set the sorted list back to the question
        question.setAnswers(sortedAnswers);

        return question;
    }


}

