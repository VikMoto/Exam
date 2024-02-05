package com.exam.controllers;

import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.models.User;
import com.exam.services.CardService;
import com.exam.services.ExamService;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;


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
        examService.initializeUnansweredQuestionsForUser(user);
        model.addAttribute("user", user);
        return "step3";
    }


    @GetMapping("/start")
    public String startExam(Model model) {
        User currentUser = examService.getLatestRegisteredUser();
        Question firstQuestion = currentUser.getUnansweredQuestions().isEmpty() ? null : currentUser.getUnansweredQuestions().get(0);
        if (firstQuestion == null) {
            // No more unanswered questions for this user
            // You might want to redirect the user to another page or show a message
            return "someOtherView"; // replace this with a valid view or redirect logic
        }

        boolean isLastQuestion = currentUser.getUnansweredQuestions().indexOf(firstQuestion) == currentUser.getUnansweredQuestions().size() - 1;
         setModelAttributes(model, currentUser, firstQuestion);
        model.addAttribute("isLastQuestion", isLastQuestion);
         return "step4";
    }

    @GetMapping("/back")
    public String goBack(@RequestParam Long userId, Model model) {
        User currentUser = examService.getUserById(userId);

        Question currentQuestion = getCurrentQuestionFromUserInput(currentUser);
        if (currentQuestion == null) {
            model.addAttribute("message", "There is no previous question to go back to.");
            return "step4"; // assuming "step4" is the view for displaying questions
        }

        // Check if the current question is the first in the list of unanswered questions
        boolean isFirstQuestion = currentUser.getUnansweredQuestions().indexOf(currentQuestion) == 0;
        System.out.println("isFirstQuestion = " + isFirstQuestion);

        if (isFirstQuestion) {
            // Stay on the same page since this is the first question
            // You can add a message to the model to inform the user if necessary
            model.addAttribute("message", "You are already at the first question.");
            model.addAttribute("isLastQuestion", Boolean.FALSE);
            setModelAttributes(model, currentUser, currentQuestion); // Re-set attributes if needed
            return "step4"; // assuming "step4" is the view for displaying questions
        }

        Question previousQuestion = examService.getPreviousUnansweredQuestion(currentUser, currentQuestion);
        System.out.println("previousQuestion.getId() = " + previousQuestion.getId());

        if (previousQuestion.getId() == null) return "step4";

        // Update the currentQuestionId for the user
        updateCurrentQuestionId(currentUser, previousQuestion.getId());
        boolean isLastQuestion = currentUser.getUnansweredQuestions().indexOf(previousQuestion) == currentUser.getUnansweredQuestions().size() - 1;
        // Set attributes for the model
        setModelAttributes(model, currentUser, previousQuestion);
        model.addAttribute("isLastQuestion", isLastQuestion);
        return "step4";
    }

    @GetMapping("/next")
    public String goNext(@RequestParam Long userId, Model model) {
        User currentUser = examService.getUserById(userId);
        Question currentQuestion = getCurrentQuestionFromUserInput(currentUser); // You need to implement this
        if (currentQuestion == null) {
            // Handle this case - maybe this is the start of the exam or an error state
        }

        Question nextQuestion = examService.getNextUnansweredQuestion(currentUser, currentQuestion);
        boolean isLastQuestion = currentUser.getUnansweredQuestions().indexOf(nextQuestion) == currentUser.getUnansweredQuestions().size() - 1;

        if (nextQuestion == null) {
            setModelAttributes(model, currentUser, currentQuestion);
            model.addAttribute("isLastQuestion", true); // It's not the last question since we went back
            return "step4"; // Or whatever view you use for displaying the question
        }
        // Update the currentQuestionId for the user
        updateCurrentQuestionId(currentUser, nextQuestion.getId());
        // Set attributes for the model
        setModelAttributes(model, currentUser, nextQuestion);
        model.addAttribute("isLastQuestion", isLastQuestion);
        return "step4";
    }

    private Question getCurrentQuestionFromUserInput(User user) {
        if (user.getCurrentQuestionId() == null) {
            // No current question is set, return null or handle accordingly
            return null;
        }
        // Assuming there is a service or repository method to find a question by its ID
        return questionService.getQuestionById(user.getCurrentQuestionId());
    }


    private void setModelAttributes(Model model, User currentUser, Question question) {
        Card currentCard = examService.getCardByQuestionId(question.getId());
        model.addAttribute("question", question);
        model.addAttribute("user", currentUser);
        model.addAttribute("currentCard", currentCard);
        if (question.getImagePath() != null) {
            model.addAttribute("imagePath", question.getImagePath());
        }
    }


    @PostMapping("/submit")
    public String submitAnswer(@RequestParam Long userId,
                               @RequestParam MultiValueMap<String, String> allRequestParams,
                               Model model) {
        User currentUser = examService.getUserById(userId);
        String action = allRequestParams.getFirst("action");
        if ("Back".equals(action)) return goBack(userId, model);
        if ("Next".equals(action)) return goNext(userId, model);
        if ("End Test".equals(action)) return endTest(userId, model);
        // Process answers before finding the next question
        examService.processUserAnswers(allRequestParams, currentUser);
//        // Get the current question based on the currentQuestionId of the user
        Question currentQuestion = getCurrentQuestionFromUserInput(currentUser);
        System.out.println("currentQuestion.getId() in submit = " + currentQuestion.getId());
        List<Long> longList = currentUser.getUnansweredQuestions().stream().map(Question::getId).toList();
        System.out.println("questId in submit  = " + longList);
        System.out.println("currentUser.getCurrentQuestionId() = " + currentUser.getCurrentQuestionId());

        // Get the next question using the current question
        Question nextQuestion = examService.getNextUnansweredQuestion(currentUser, currentQuestion);

        if (nextQuestion == null) {
            // If there's no next question, this could be the last question that was answered.
            // You may need to implement getPreviousUnansweredQuestion similar to getNextUnansweredQuestion
            Question previousQuestion = examService.getPreviousUnansweredQuestion(currentUser, currentQuestion);
            if (previousQuestion != null) {
                // Go back to the previous unanswered question if it exists
                updateCurrentQuestionId(currentUser, previousQuestion.getId());
                setModelAttributes(model, currentUser, previousQuestion);
                model.addAttribute("isLastQuestion", false); // It's not the last question since we went back
                return "step4"; // Or whatever view you use for displaying the question
            } else {
                // This was the last unanswered question and it has been answered
                return "redirect:/exam/result/" + userId;
            }
        }

        updateCurrentQuestionId(currentUser, nextQuestion.getId());
        // Update the currentQuestionId for the user

        boolean isLastQuestion = currentUser.getUnansweredQuestions().indexOf(nextQuestion) == currentUser.getUnansweredQuestions().size() - 1;
        setModelAttributes(model, currentUser, nextQuestion);
        model.addAttribute("isLastQuestion", isLastQuestion);
        return "step4";
    }

    private void initializeModelWithQuestion(User user, Question question, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("question", question);
        model.addAttribute("imagePath", question != null ? question.getImagePath() : null);
        updateCurrentQuestionId(user, question != null ? question.getId() : null);
    }

    @PostMapping("/endTest")
    public String endTest(@RequestParam Long userId, Model model) {
        // This method could also perform any necessary actions before ending the test
        // such as logging the end of the test, updating the user's status, etc.
        // Perform any required clean-up or saving operations before ending the test
        // Redirect to the result page
        return "redirect:/exam/result/" + userId;
    }


    private void updateCurrentQuestionId(User currentUser, Long nextQuestion) {
        currentUser.setCurrentQuestionId(nextQuestion);  // Setting the user's current question ID
        examService.updateUser(currentUser); // Updating the user
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


