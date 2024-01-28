package com.exam.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class QuestionDTO {
    private String content;
    private List<String> answers = new ArrayList<>();
    private List<Boolean> isCorrect = new ArrayList<>();

    public void addAnswer(String answer) {
        answers.add(answer);
    }

    public void addIsCorrect(Boolean correct) {
        isCorrect.add(correct);
    }
}


