package com.exam.services;


import com.exam.models.Answer;
import com.exam.models.Question;
import com.exam.repo.AnswerRepository;
import com.exam.repo.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
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
//            System.out.println("answer = " + answer);
            answerRepository.save(answer); // Persist each answer to the database
        }
        System.out.println("Saving question: " + question.getContent());
    }
}

