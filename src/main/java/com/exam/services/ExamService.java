package com.exam.services;

import com.exam.models.*;
import com.exam.repo.AnswerRepository;
import com.exam.repo.CardRepository;
import com.exam.repo.QuestionRepository;
import com.exam.repo.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ExamService {

    private final UserRepository userRepository;

    private final CardRepository cardRepository;


    private final QuestionRepository questionRepository;


    private final AnswerRepository answerRepository;
    private final CardService cardService;

    public ExamService(UserRepository userRepository, CardRepository cardRepository, QuestionRepository questionRepository, AnswerRepository answerRepository, CardService cardService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.questionRepository = questionRepository;
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



    public void submitAnswer(Long userId, Long questionId, List<Answer> submittedAnswers) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));

        // Move from unanswered questions to viewed history
        user.getUnansweredQuestions().remove(question);
        user.getViewedQuestionsHistory().add(
                UserQuestionHistory.builder().user(user).question(question).viewedAt(LocalDateTime.now()).build()
        );

        userRepository.save(user);
    }

    public void initializeUnansweredQuestionsForUser(User user) {
        List<Card> allCards = cardRepository.findAllByOrderByIdAsc();
        List<Question> orderedQuestions = new ArrayList<>();

        for (Card card : allCards) {
            orderedQuestions.addAll(questionRepository.findByCardIdOrderByQuestionOrderAsc(card.getId()));
        }

        user.getUnansweredQuestions().addAll(orderedQuestions);
        Question firstQuestion = user.getUnansweredQuestions().isEmpty() ? null : user.getUnansweredQuestions().get(0);
        assert firstQuestion != null;
        user.setCurrentQuestionId(firstQuestion.getId());
        userRepository.save(user);
    }


//    public Question getNextUnansweredQuestion(User user, Question currentQuestion) {
//        List<Question> unansweredQuestions = user.getUnansweredQuestions();
//        Long currentQuestionId = currentQuestion.getId();
//
//        System.out.println("currentQuestionId = in getNextUnansweredQuestion " + currentQuestionId);
//
//        // If the currentQuestionId is not in the list of unanswered questions,
//        // assume the next question in the list should be returned.
//        if (!unansweredQuestions.stream().anyMatch(q -> q.getId().equals(currentQuestionId))) {
//            if (!unansweredQuestions.isEmpty()) {
//                // Just get the first unanswered question
//                return unansweredQuestions.get(0);
//            }
//            // If there are no unanswered questions left, return null
//            return null;
//        }
//
//        // If the currentQuestionId is found in the list,
//        // proceed to get the next question after it
//        int currentIndex = -1;
//        for (int i = 0; i < unansweredQuestions.size(); i++) {
//            if (unansweredQuestions.get(i).getId().equals(currentQuestionId)) {
//                currentIndex = i;
//                break;
//            }
//        }
//
//        System.out.println("currentIndex = " + currentIndex);
//
//        // Get the next question if it exists
//        if (currentIndex != -1 && currentIndex < unansweredQuestions.size() - 1) {
//            return unansweredQuestions.get(currentIndex + 1);
//        }
//
//        // If there are no more unanswered questions, return null
//        return null;
//    }

    public Question getNextUnansweredQuestion(User user, Question currentQuestion) {
        List<Question> unansweredQuestions = user.getUnansweredQuestions();
        Long currentQuestionId = currentQuestion.getId();

        // Maintain a separate mapping of question IDs to their original indices.
        Map<Long, Integer> questionIndexMap = IntStream.range(0, unansweredQuestions.size())
                .boxed()
                .collect(Collectors.toMap(i -> unansweredQuestions.get(i).getId(), i -> i));
        System.out.println("questionIndexMap = " + questionIndexMap);

        // Find the index of the current question based on the map.
        Integer currentQuestionIndex = questionIndexMap.get(currentQuestionId);

        // If the current question is no longer in the list, find the next index where it used to be.
        if (currentQuestionIndex == null) {
            // Find where the current question would fit in the list of remaining question IDs.
            List<Long> sortedQuestionIds = unansweredQuestions.stream()
                    .map(Question::getId)
                    .sorted()
                    .toList();

            int insertPoint = Collections.binarySearch(sortedQuestionIds, currentQuestionId);
            if (insertPoint < 0) {
                // Convert insertPoint to the index where the current ID would be inserted.
                insertPoint = -insertPoint - 1;
            }

            // If the insert point is within the range of the list, return the question at this position.
            if (insertPoint < unansweredQuestions.size()) {
                return unansweredQuestions.get(insertPoint);
            }

            // If there are no more questions after the current position, return null.
            return null;
        }

        // If the current question is in the list, get the next question in the original order.
        if (currentQuestionIndex + 1 < unansweredQuestions.size()) {
            return unansweredQuestions.get(currentQuestionIndex + 1);
        }

        // If there are no more questions after the current one, return null.
        return null;
    }



    public Question getPreviousUnansweredQuestion(User user) {
        List<Question> unansweredQuestions = user.getUnansweredQuestions();

        // The method assumes the currentQuestionId of the user is set to the current question's ID.
        // If it's not set (i.e., null), or the list is empty, then there is no previous question to return.
        if (user.getCurrentQuestionId() == null || unansweredQuestions.isEmpty()) {
            return null;
        }

        // Find the index of the current question
        int currentIndex = -1;
        for (int i = unansweredQuestions.size() - 1; i >= 0; i--) {
            if (unansweredQuestions.get(i).getId().equals(user.getCurrentQuestionId())) {
                currentIndex = i;
                break;
            }
        }

        // Get the previous question if it exists
        // Check if the current index is greater than 0, which means there is a question before it
        if (currentIndex > 0) {
            return unansweredQuestions.get(currentIndex - 1);
        }

        // If the current question is the first in the list or not found, return null
        return null;
    }





    public void processUserAnswers(MultiValueMap<String, String> allRequestParams, User currentUser) {
        List<String> answerKeys = allRequestParams.keySet().stream()
                .filter(k -> k.startsWith("answerForQuestion_"))
                .toList();

        if (answerKeys.isEmpty()) return;

        String[] parts = answerKeys.get(0).split("_");
        Long currentQuestionId = Long.parseLong(parts[1]);

        int correctAnswersCount = 0;
        for (String answerKey : answerKeys) {
            List<String> submittedAnswers = allRequestParams.get(answerKey);

            if (submittedAnswers == null || submittedAnswers.isEmpty()) {
                continue;
            }

            for (String submittedAnswer : submittedAnswers) {
                if (isAnswerCorrect(currentQuestionId, submittedAnswer)) {
                    correctAnswersCount++;
                }
            }
        }

        // Increment score if all answers for the question are correct
        if (correctAnswersCount == getNumberOfCorrectAnswers(currentQuestionId)) {
            incrementUserScore(currentUser.getId());
        }

        // Updating the state of the user (removing the question from unanswered questions)
        currentUser.getUnansweredQuestions().removeIf(q -> q.getId().equals(currentQuestionId));
        List<Long> questIdProcessAnswers = currentUser.getUnansweredQuestions().stream().map(Question::getId).toList();
        System.out.println("longList = " + questIdProcessAnswers);
        currentUser.getViewedQuestionsHistory().add(
                UserQuestionHistory.builder().user(currentUser).question(
                        Question.builder().id(currentQuestionId).build() // using a reference to the question
                ).viewedAt(LocalDateTime.now()).build()
        );

        userRepository.save(currentUser);
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

    public Card getCardByQuestionId(Long questionId) {
        return cardRepository.findCardByQuestionId(questionId);
    }


}

