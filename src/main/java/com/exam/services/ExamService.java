package com.exam.services;

import com.exam.models.Answer;
import com.exam.models.Question;
import com.exam.models.User;
import com.exam.repo.AnswerRepository;
import com.exam.repo.QuestionRepository;
import com.exam.repo.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {

    private Long currentQuestionId = null;
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

    public User getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        throw new RuntimeException("User not found with id: " + userId);  // Or handle this scenario as needed
    }

    public User getLatestRegisteredUser() {
        return userRepository.findTopByOrderByIdDesc().orElse(null);
    }

    public Question getFirstQuestion() {
        return questionRepository.findFirstByOrderByOrderAsc();
    }

    public Question getNextQuestion(Long currentQuestionId) {
        Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
        if (currentQuestion != null) {
            return questionRepository.findByOrderGreaterThanOrderByOrderAsc(currentQuestion.getOrder()).stream()
                            .findFirst()
                            .orElse(null);
        }
        return null;
    }

    public Question getNextQuestion() {
        if (currentQuestionId == null) {
            return questionRepository.findFirstByOrderByOrderAsc();
        } else {
            Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
            if (currentQuestion != null) {
                return questionRepository.findFirstByOrderGreaterThanOrderByOrderAsc(currentQuestion.getOrder());
            }
        }
        return null;
    }

    public void setCurrentQuestionId(Long id) {
        this.currentQuestionId = id;
    }

    public boolean isAnswerCorrect(Long questionId, String submittedAnswer) {
        // Fetch all the correct answers for the given question from the database
        List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrectTrue(questionId);

        // Check if the submittedAnswer matches any of the correct answers for the given question
        for (Answer correctAnswer : correctAnswers) {
            if (correctAnswer.getContent().equalsIgnoreCase(submittedAnswer)) {
                return true;
            }
        }

        return false;
    }

    public void incrementUserScore(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")); // Assuming you have a UserRepository
        user.setScore(user.getScore() + 1);
        userRepository.save(user); // Save updated user score
    }

}

