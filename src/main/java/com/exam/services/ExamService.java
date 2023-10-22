package com.exam.services;

import com.exam.models.Answer;
import com.exam.models.Question;
import com.exam.models.User;
import com.exam.repo.AnswerRepository;
import com.exam.repo.QuestionRepository;
import com.exam.repo.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExamService {


    private final UserRepository userRepository;


    private final QuestionRepository questionRepository;


    private final AnswerRepository answerRepository;

    public ExamService(UserRepository userRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question getQuestion(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    public Answer getAnswer(Long id) {
        return answerRepository.findById(id).orElse(null);
    }

    public void saveSelectedAnswerForUser(Long userId, Long answerId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Answer answer = answerRepository.findById(answerId).orElseThrow(() -> new RuntimeException("Answer not found"));

        user.getAnswers().add(answer);  // Assuming you have a getAnswers() method in the User entity that returns a List<Answer>.
        userRepository.save(user);
    }

    public int calculateScoreForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        int score = 0;
        for (Answer answer : user.getAnswers()) {  // Again, assuming the getAnswers() method.
            if (answer.isCorrect()) {
                score++;
            }
        }

        user.setScore(score);
        userRepository.save(user);

        return score;
    }
}

