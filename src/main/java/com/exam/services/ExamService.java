package com.exam.services;

import com.exam.models.Answer;
import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.models.User;
import com.exam.repo.AnswerRepository;
import com.exam.repo.CardRepository;
import com.exam.repo.QuestionRepository;
import com.exam.repo.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private Long currentQuestionId = null;
    private final UserRepository userRepository;

    private final CardRepository cardRepository;


    private final QuestionRepository questionRepository;


    private final AnswerRepository answerRepository;

    public ExamService(UserRepository userRepository, CardRepository cardRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
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


    public void initializeUnansweredQuestionsForUser(User user) {
        List<Question> allQuestions = questionRepository.findAll();
        user.getUnansweredQuestions().addAll(allQuestions);
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
        return questionRepository.findFirstByOrderByQuestionOrderAsc();
    }


    public void submitAnswer(Long userId, Long questionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        user.getUnansweredQuestions().remove(question);

        userRepository.save(user);
        // Handle answer logic here
    }


    public Question getNextQuestion(Long userId, Long currentCardId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found!"));  // Assuming you have this method to fetch the user by ID
        Long currentQuestionId = user.getCurrentQuestionId();

        if (currentQuestionId == null) {
            return questionRepository.findFirstByOrderByQuestionOrderAsc();
        } else {
            Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
            if (currentQuestion != null) {
                return getNextQuestionFromSameCard(currentCardId, currentQuestion.getQuestionOrder());
            }
        }
        return null;
    }

    public Question getNextQuestionFromSameCard(Long cardId, Integer currentQuestionOrder) {
        return questionRepository.findFirstByCardIdAndQuestionOrderGreaterThanOrderByQuestionOrderAsc(cardId, currentQuestionOrder).orElse(null);
    }


    public Question getPreviousQuestion(Long userId, Long currentCardId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found!"));  // Assuming you have this method to fetch the user by ID
        Long currentQuestionId = user.getCurrentQuestionId();

        if (currentQuestionId == null) {
            return questionRepository.findTopByOrderByQuestionOrderDesc().orElse(null); // Assuming you might want the last question if no current one is set
        } else {
            Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
            if (currentQuestion != null) {
                return getPreviousQuestionFromSameCard(currentCardId, currentQuestion.getQuestionOrder());
            }
        }
        return null;
    }



    public Question getPreviousQuestionFromSameCard(Long cardId, Integer currentQuestionOrder) {
        return questionRepository.findFirstByCardIdAndQuestionOrderLessThanOrderByQuestionOrderDesc(cardId, currentQuestionOrder).orElse(null);
    }



    public Question getNextUnansweredQuestion(Long userId, Long currentCardId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("user.getUnansweredQuestions().ID = " + user.getUnansweredQuestions()
                .stream().map(Question::getId)
                .collect(Collectors.toSet()));
        // Get the current question's order
        Long currentQuestionId = user.getCurrentQuestionId();
        Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
        Integer currentOrder = (currentQuestion != null) ? currentQuestion.getQuestionOrder() : 0;

        // Fetch the list of answered questions by this user
        Set<Long> answeredQuestions = getAnsweredQuestions(user);

        // Find the next unanswered question in order
        List<Question> orderedQuestions = questionRepository.findByCardIdOrderByQuestionOrderAsc(currentCardId);
        for (Question question : orderedQuestions) {
            if (question.getQuestionOrder() > currentOrder && !answeredQuestions.contains(question.getId())) {
                return question;
            }
        }

        return null; // No next unanswered question found
    }

    public Question getPreviousUnansweredQuestion(Long userId, Long currentCardId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("user.getUnansweredQuestions().ID = " + user.getUnansweredQuestions()
                .stream().map(Question::getId)
                .collect(Collectors.toSet()));
        // Get the current question's order
        Long currentQuestionId = user.getCurrentQuestionId();
        Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
        Integer currentOrder = (currentQuestion != null) ? currentQuestion.getQuestionOrder() : Integer.MAX_VALUE;

        // Fetch the list of answered questions by this user
        Set<Long> answeredQuestions = getAnsweredQuestions(user);

        // Find the previous unanswered question in order
        List<Question> orderedQuestions = questionRepository.findByCardIdOrderByQuestionOrderDesc(currentCardId);
        for (Question question : orderedQuestions) {
            if (question.getQuestionOrder() < currentOrder && !answeredQuestions.contains(question.getId())) {
                return question;
            }
        }

        return null; // No previous unanswered question found
    }

    private Set<Long> getAnsweredQuestions(User user) {
        // Fetch all question IDs
        Set<Long> allQuestionIds = questionRepository.findAll().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        // Fetch all unanswered question IDs
        Set<Long> unansweredQuestionIds = user.getUnansweredQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        // Subtract unanswered from all to get answered question IDs
        allQuestionIds.removeAll(unansweredQuestionIds);

        return allQuestionIds;
    }





    public void setCurrentQuestionId(Long id) {
        this.currentQuestionId = id;
    }

    public boolean isAnswerCorrect(Long questionId, String submittedAnswer) {
        // Fetch all the correct answers for the given question from the database
        List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrectTrue(questionId);
//        System.out.println("correctAnswers = " + correctAnswers);
//        System.out.println("submittedAnswer = " + submittedAnswer);
        // Check if the submittedAnswer matches any of the correct answers for the given question
        for (Answer correctAnswer : correctAnswers) {
            if (correctAnswer.getId()== Long.parseLong(submittedAnswer)) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public void incrementUserScore(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")); // Assuming you have a UserRepository
        Integer currentScore = user.getScore();
        System.out.println("currentScore = " + currentScore);

        if (currentScore == null) {
            currentScore = 0;
        }

        user.setScore(currentScore + 1);
        System.out.println("user = " + user);
        User saved = userRepository.save(user);// Save updated user score
        System.out.println("saved = " + saved);
    }


    public int getNumberOfCorrectAnswers(Long currentQuestionId) {
        return answerRepository.findByQuestionIdAndIsCorrectTrue(currentQuestionId).size();

    }

    public int getScoreByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow().getScore();
    }

    public Question getFirstQuestionFromCard(Card card) {
        if (card == null || card.getId() == null) {
            return null;
        }

        Pageable limit = PageRequest.of(0, 1); // Limit to 1 result
        List<Question> questions = questionRepository.findQuestionsByCardId(card.getId(), limit);

        if (questions.isEmpty()) {
            return null; // No questions found for the card
        }

        return questions.get(0);
    }




    public Card getCardById(Long currentCardId) {
        return cardRepository.findById(currentCardId).orElseThrow();
    }

    public Card getFirstCard() {
        List<Card> cards = cardRepository.findAllOrderedById();

        if (cards.isEmpty()) {
            return null; // No cards found in the database
        }

        return cards.get(0);  // Return the first card
    }

    public void updateUser(User currentUser) {
        userRepository.save(currentUser);
    }

    public Question getLastQuestionFromCard(Card card) {
        return questionRepository.findTopByCardIdOrderByQuestionOrderDesc(card.getId()).orElse(null);
    }
}

