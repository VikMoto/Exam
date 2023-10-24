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
import java.util.List;


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

        // Check if the first question has an associated imagePath and add to model
        if (firstQuestion != null && firstQuestion.getImagePath() != null) {
            System.out.println("firstQuestion.getImagePath() = " + firstQuestion.getImagePath());
            model.addAttribute("imagePath", firstQuestion.getImagePath());
        }

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
        User userById = examService.getUserById(userId);


        if (!answerKeys.isEmpty()) {
            // Take the first key to get the current question ID
            String[] parts = answerKeys.get(0).split("_");
            Long currentQuestionId = Long.parseLong(parts[1]);
            examService.setCurrentQuestionId(currentQuestionId);

            int correctAnswersCount = 0; // To keep track of correct answers for a given question
            // Iterate over all submitted answers for the question
            for (String answerKey : answerKeys) {
                List<String> submittedAnswers = allRequestParams.get(answerKey);
//                System.out.println("submittedAnswers = " + submittedAnswers);

                if (submittedAnswers == null || submittedAnswers.isEmpty()) {
                    continue;  // Skip processing for null or empty values
                }

                for (String submittedAnswer : submittedAnswers) {
                    if (examService.isAnswerCorrect(currentQuestionId, submittedAnswer)) {
                        correctAnswersCount++;
                    }
                }
            }
            // Check if the user got all answers correct
            if (correctAnswersCount == examService.getNumberOfCorrectAnswers(currentQuestionId)) {
                examService.incrementUserScore(userId);
            }
        }

        Question nextQuestion = examService.getNextQuestion();


        if (nextQuestion != null && nextQuestion.getImagePath() != null) {
            model.addAttribute("imagePath", nextQuestion.getImagePath());
            System.out.println("nextQuestion.getImagePath() = " + nextQuestion.getImagePath());
        }


        if (nextQuestion == null) {
            return "redirect:/exam/result/" + userId;
            // Or wherever you want to redirect when the exam is finished
        }



        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", userById); // Assumes you have a method to get a user by ID

        return "step4";
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


