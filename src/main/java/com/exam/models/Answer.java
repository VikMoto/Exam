package com.exam.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@ToString(exclude = "question") // This will exclude the question field from the generated toString() method
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
