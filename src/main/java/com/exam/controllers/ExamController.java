package com.exam.controllers;

import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.models.User;
import com.exam.services.CardService;
import com.exam.services.ExamService;
import com.exam.services.QuestionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Controller
@RequestMapping("/exam")
public class ExamController {

    private final ExamService examService;
    private final CardService cardService;
    private final QuestionService questionService;

    public ExamController(ExamService examService, CardService cardService, QuestionService questionService) {
        this.examService = examService;
        this.cardService = cardService;
        this.questionService = questionService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {
        examService.saveUser(user);
        model.addAttribute("user", user);
        return "step3";
    }

    @GetMapping("/start")
    public String startExam(Model model) {
        User user = examService.getLatestRegisteredUser();
        Card firstCard = examService.getFirstCard(); // New method
        Question firstQuestion = examService.getFirstQuestionFromCard(firstCard); // New method

        // Set the currentQuestionId for the user
        if (firstQuestion != null) {
            updateCurrentQuestionId(user, firstQuestion.getId());
        }

        model.addAttribute("currentCard", firstCard);
        model.addAttribute("question", firstQuestion);
        model.addAttribute("user", user);


        // Check if the first question has an associated imagePath and add to model
        if (firstQuestion != null && firstQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", firstQuestion.getImagePath());
        }

        return "step4";
    }



    @GetMapping("/back")
    public String goBack(@RequestParam Long userId, @RequestParam Long currentCardId, Model model) {
        User currentUser = examService.getUserById(userId);
        Question previousQuestion = null;
        Card previousCard = cardService.getCurrentCard(currentCardId).orElseThrow();

        while (true) {
            List<Question> questionsInCurrentCard = examService.getAllByCardIdOrderByQuestionOrder(currentCardId);

            // Check if the current question is the first in the card
            if (currentUser.getCurrentQuestionId().equals(questionsInCurrentCard.get(0).getId())) {
                previousCard = cardService.getPreviousCard(currentCardId);
                if (previousCard == null) {
                    return "redirect:/exam/start";  // No previous card found
                }

                currentCardId = previousCard.getId();
                questionsInCurrentCard = examService.getAllByCardIdOrderByQuestionOrder(currentCardId);
                // Loop from the end of this card's questions to find the last unanswered one
                for (int i = questionsInCurrentCard.size() - 1; i >= 0; i--) {
                    Question q = questionsInCurrentCard.get(i);
                    if (!isQuestionAnswered(currentUser, q)) {
                        previousQuestion = q;
                        break;
                    }
                }

                if (previousQuestion != null) {
                    break; // Found an unanswered question
                }
            } else {
                // If the current question is not the first in the card
                for (int i = questionsInCurrentCard.indexOf(examService.getQuestionById(currentUser.getCurrentQuestionId())) - 1; i >= 0; i--) {
                    Question q = questionsInCurrentCard.get(i);
                    if (!isQuestionAnswered(currentUser, q)) {
                        previousQuestion = q;
                        break;
                    }
                }

                if (previousQuestion != null) {
                    break; // Found an unanswered question in the current card
                }
            }
        }

        // Update the user's current question
        updateCurrentQuestionId(currentUser, previousQuestion.getId());

        // Add attributes to the model for the view
        model.addAttribute("currentCard", previousCard);
        model.addAttribute("question", previousQuestion);
        model.addAttribute("user", currentUser);

        if (previousQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", previousQuestion.getImagePath());
        }

        return "step4";
    }



    private boolean isQuestionAnswered(User user, Question question) {
        return user.getAnsweredQuestions().stream().anyMatch(aq -> aq.getQuestionId().equals(question.getId()));
    }






    @GetMapping("/next")
    public String goNext(@RequestParam Long userId, @RequestParam Long currentCardId, Model model) {
        User currentUser = examService.getUserById(userId);
        Question nextQuestion = null;
        Card currentCard = cardService.getCurrentCard(currentCardId).orElseThrow();

        while (true) {
            List<Question> questionsInCurrentCard = examService.getAllByCardIdOrderByQuestionOrder(currentCardId);

            // Check if the current question is the last in the card
            if (currentUser.getCurrentQuestionId().equals(questionsInCurrentCard.get(questionsInCurrentCard.size() - 1).getId())) {
                currentCard = cardService.getNextCard(currentCardId);
                if (currentCard == null) {
                    return "redirect:/exam/result/" + userId;  // No next card found
                }

                currentCardId = currentCard.getId();
                questionsInCurrentCard = examService.getAllByCardIdOrderByQuestionOrder(currentCardId);

                // Loop from the beginning of this card's questions to find the first unanswered one
                for (Question q : questionsInCurrentCard) {
                    if (!isQuestionAnswered(currentUser, q)) {
                        nextQuestion = q;
                        break;
                    }
                }

                if (nextQuestion != null) {
                    break; // Found an unanswered question
                }
            } else {
                // If the current question is not the last in the card
                for (int i = questionsInCurrentCard.indexOf(examService.getQuestionById(currentUser.getCurrentQuestionId())) + 1; i < questionsInCurrentCard.size(); i++) {
                    Question q = questionsInCurrentCard.get(i);
                    if (!isQuestionAnswered(currentUser, q)) {
                        nextQuestion = q;
                        break;
                    }
                }

                if (nextQuestion != null) {
                    break; // Found an unanswered question in the current card
                }
            }
        }

        // Update the user's current question
        updateCurrentQuestionId(currentUser, nextQuestion.getId());

        // Add attributes to the model for the view
        model.addAttribute("currentCard", currentCard);
        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", currentUser);

        if (nextQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", nextQuestion.getImagePath());
        }

        return "step4";
    }






    @PostMapping("/submit")
    public String submitAnswer(@RequestParam(name = "userId") Long userId,
                               @RequestParam(name = "currentCardId") Long currentCardId,
                               @RequestParam MultiValueMap<String, String> allRequestParams,
                               Model model) {
        User currentUser = examService.getUserById(userId);
        String action = allRequestParams.getFirst("action");

        if ("Back".equals(action)) {
            return goBack(userId, currentCardId, model);
        } else if ("Next".equals(action)) {
            return goNext(userId, currentCardId, model);
        }

        List<String> answerKeys = extractAnswerKeys(allRequestParams);

        if (!answerKeys.isEmpty()) {
            processUserAnswers(currentUser, answerKeys, allRequestParams);
        }

        // Mark the question as answered for the user
        examService.addUserAnsweredQuestion(currentUser, currentUser.getCurrentQuestionId());

        return navigateToNextQuestionOrCard(currentUser, currentCardId, model);
    }

    private List<String> extractAnswerKeys(MultiValueMap<String, String> allRequestParams) {
        return allRequestParams.keySet().stream()
                .filter(k -> k.startsWith("answerForQuestion_"))
                .toList();
    }

    private void processUserAnswers(User currentUser, List<String> answerKeys, MultiValueMap<String, String> allRequestParams) {
        String[] parts = answerKeys.get(0).split("_");
        Long currentQuestionId = Long.parseLong(parts[1]);
        updateCurrentQuestionId(currentUser, currentQuestionId);

        int correctAnswersCount = 0;
        for (String answerKey : answerKeys) {
            correctAnswersCount += countCorrectAnswersForQuestion(allRequestParams, currentQuestionId, answerKey);
        }

        if (correctAnswersCount == examService.getNumberOfCorrectAnswers(currentQuestionId)) {
            examService.incrementUserScore(currentUser.getId());
        }
    }

    private int countCorrectAnswersForQuestion(MultiValueMap<String, String> allRequestParams, Long currentQuestionId, String answerKey) {
        List<String> submittedAnswers = allRequestParams.get(answerKey);
        if (submittedAnswers == null || submittedAnswers.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String submittedAnswer : submittedAnswers) {
            if (examService.isAnswerCorrect(currentQuestionId, submittedAnswer)) {
                count++;
            }
        }
        return count;
    }

    private String navigateToNextQuestionOrCard(User currentUser, Long currentCardId, Model model) {
        Question nextQuestion = examService.getNextQuestion(currentUser.getId(), currentCardId);
        if (nextQuestion == null) {
            Card nextCard = cardService.getNextCard(currentCardId);
            return handleCardEnd(currentUser, nextCard, model);
        }
        updateAndSetAttributesForNextQuestion(currentUser, nextQuestion, model, currentCardId);
        return "step4";
    }

    private String handleCardEnd(User currentUser, Card nextCard, Model model) {
        if (nextCard == null) {
            return "redirect:/exam/result/" + currentUser.getId();
        }

        model.addAttribute("currentCard", nextCard);
        Question nextQuestion = examService.getFirstQuestionFromCard(nextCard);

        if (nextQuestion == null) {
            return "redirect:/exam/result/" + currentUser.getId();
        }

        updateCurrentQuestionId(currentUser, nextQuestion.getId());
        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", currentUser);
        if (nextQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", nextQuestion.getImagePath());
        }

        return "step4";
    }

    private void updateAndSetAttributesForNextQuestion(User currentUser, Question nextQuestion, Model model, Long currentCardId) {
        model.addAttribute("currentCard", examService.getCardById(currentCardId));
        updateCurrentQuestionId(currentUser, nextQuestion.getId());
        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", currentUser);
        if (nextQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", nextQuestion.getImagePath());
        }
    }

    private void updateCurrentQuestionId(User user, Long currentQuestionId) {
        user.setCurrentQuestionId(currentQuestionId);
        examService.updateUser(user);
    }


    @GetMapping("/result/{userId}")
    public String displayResult(@PathVariable Long userId, Model model) {
        User user = examService.getUserById(userId); // You'd need to have such a method in your service
        if (user == null) {
            // handle the case where the user is not found
            return "errorPage"; // replace with your error page/view
        }

        int score = examService.getScoreByUserId(userId);
        String rating = determineRating(score);

        model.addAttribute("user", user);  // <-- This is the missing part
        model.addAttribute("score", score);
        model.addAttribute("rating", rating);

        return "step17";
    }

    private String determineRating(int score) {
        if (score >= 56 && score <= 60) return "12";
        if (score >= 51 && score <= 55) return "11";
        if (score >= 46 && score <= 50) return "10";
        if (score >= 41 && score <= 45) return "9";
        if (score >= 36 && score <= 40) return "8";
        if (score >= 31 && score <= 35) return "7";
        if (score >= 26 && score <= 30) return "6";
        if (score >= 21 && score <= 25) return "5";
        if (score >= 16 && score <= 20) return "4";
        if (score >= 11 && score <= 15) return "3";
        if (score >= 6 && score <= 10) return "2";
        if (score >= 1 && score <= 5) return "1";
        if (score == 0) return "0";
        return "Invalid Score"; // This is just to handle any scores that might fall outside the ranges provided.
    }

}


