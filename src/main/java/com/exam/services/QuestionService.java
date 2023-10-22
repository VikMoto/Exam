package com.exam.services;


import com.exam.models.Question;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    public void saveQuestion(Question question) {
        // In reality, you'd persist this question to a database
        System.out.println("Saving question: " + question.getContent());
    }
}

