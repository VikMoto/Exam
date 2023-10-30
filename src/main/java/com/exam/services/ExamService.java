package com.exam.services;

import com.exam.models.*;
import com.exam.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ExamService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final QuestionRepository questionRepository;
    private final AnsweredQuestionRepository answeredQuestionRepo;
    private final AnswerRepository answerRepository;
    private final CardService cardService;

    public ExamService(UserRepository userRepository, CardRepository cardRepository, QuestionRepository questionRepository, AnsweredQuestionRepository answeredQuestionRepo, AnswerRepository answerRepository, CardService cardService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.questionRepository = questionRepository;
        this.answeredQuestionRepo = answeredQuestionRepo;
        this.answerRepository = answerRepository;
        this.cardService = cardService;
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
        return questionRepository.findFirstByOrderByQuestionOrderAsc();
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
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found!"));
        Long currentQuestionId = user.getCurrentQuestionId();

        if (currentQuestionId == null) {
            return questionRepository.findTopByOrderByQuestionOrderDesc().orElse(null);
        } else {
            Question currentQuestion = questionRepository.findById(currentQuestionId).orElse(null);
            if (currentQuestion != null) {
                Question prevQuestion = getPreviousQuestionFromSameCard(currentCardId, currentQuestion.getQuestionOrder());

                // If there's no previous question on the same card, try to get the last question from the previous card
                if(prevQuestion == null) {
                    Card prevCard = cardService.getPreviousCard(currentCardId); // Assuming you have this method
                    if(prevCard != null) {
                        prevQuestion = getLastQuestionFromCard(prevCard);
                    }
                }
                return prevQuestion;
            }
        }
        return null;
    }


    public List<Question> getAllByCardIdOrderByQuestionOrder(Long currentCardId) {
        return questionRepository.findAllByCardIdOrderByQuestionOrder(currentCardId);
    }





    public Question getPreviousQuestionFromSameCard(Long cardId, Integer currentQuestionOrder) {
        return questionRepository.findFirstByCardIdAndQuestionOrderLessThanOrderByQuestionOrderDesc(cardId, currentQuestionOrder).orElse(null);
    }








    public boolean isAnswerCorrect(Long questionId, String submittedAnswer) {
        // Fetch all the correct answers for the given question from the database
        List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrectTrue(questionId);

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


    public void addUserAnsweredQuestion(User user, Long questionId) {
        AnsweredQuestion answeredQuestion = new AnsweredQuestion();
        answeredQuestion.setUser(user);
        answeredQuestion.setQuestionId(questionId);
        answeredQuestionRepo.save(answeredQuestion);
    }

    public Question getQuestionById(Long currentQuestionId) {
        return questionRepository.findById(currentQuestionId).orElseThrow();
    }
}

