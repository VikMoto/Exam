package com.exam.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    private Long currentQuestionId;

    private Integer score = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnsweredQuestion> answeredQuestions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Answer> answers;

//    @Override
//    public String toString() {
//        // Assuming User has a field 'id' and a collection of 'answeredQuestions'
//        return "User [id=" + id + ", answeredQuestionsCount=" + answeredQuestions.size() + "]";
//    }



}