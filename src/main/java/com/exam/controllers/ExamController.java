package com.exam.controllers;

import com.exam.models.Question;
import com.exam.models.User;
import com.exam.services.ExamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
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
        Question firstQuestion = examService.getFirstQuestion();

        model.addAttribute("question", firstQuestion);
        model.addAttribute("user", user);

        return "step4";
    }


    @PostMapping("/submit")
    public String submitAnswer(@RequestParam(name = "userId") Long userId,
                               @RequestParam MultiValueMap<String, String> allRequestParams,
                               Model model) {

        // All keys that start with "answerForQuestion_" are related to answers
        List<String> answerKeys = allRequestParams.keySet().stream()
                .filter(k -> k.startsWith("answerForQuestion_"))
                .toList();

        if (!answerKeys.isEmpty()) {
            // Take the first key to get the current question ID
            String[] parts = answerKeys.get(0).split("_");
            Long currentQuestionId = Long.parseLong(parts[1]);
            examService.setCurrentQuestionId(currentQuestionId);

            // Iterate over all submitted answers for the question
            for (String answerKey : answerKeys) {
                List<String> submittedAnswers = allRequestParams.get(answerKey);
//                System.out.println("submittedAnswers = " + submittedAnswers);

                if (submittedAnswers == null || submittedAnswers.isEmpty()) {
                    continue;  // Skip processing for null or empty values
                }

                for (String submittedAnswer : submittedAnswers) {
                    if (examService.isAnswerCorrect(currentQuestionId, submittedAnswer)) {
                        examService.incrementUserScore(userId);
                    }
                }
            }
        }

        Question nextQuestion = examService.getNextQuestion();

        if (nextQuestion == null) {
            return "redirect:/exam/result/" + userId;
            // Or wherever you want to redirect when the exam is finished
        }

        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", examService.getUserById(userId)); // Assumes you have a method to get a user by ID

        return "step4";
    }




    @GetMapping("/result/{userId}")
    public String displayResult(@PathVariable Long userId, Model model) {
        User user = examService.getUserById(userId); // You'd need to have such a method in your service
        if (user == null) {
            // handle the case where the user is not found
            return "errorPage"; // replace with your error page/view
        }

        int score = examService.calculateScoreForUser(userId);
        String rating = determineRating(score);

        model.addAttribute("user", user);  // <-- This is the missing part
        model.addAttribute("score", score);
        model.addAttribute("rating", rating);

        return "step17";
    }

    private String determineRating(int score) {
        if (score >= 56) return "12";
        if (score >= 51) return "11";
        if (score >= 46) return "10";
        return "0";
    }

}


