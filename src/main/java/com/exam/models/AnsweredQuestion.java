package com.exam.models;

import jakarta.persistence.Entity;

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
public class AnsweredQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long questionId;
    // getters, setters, etc.

    @Override
    public String toString() {
        // Assuming AnsweredQuestion has a field 'questionId' and a reference to 'user'
        return "AnsweredQuestion [questionId=" + questionId + ", userId=" + user.getId() + "]";
    }

}
