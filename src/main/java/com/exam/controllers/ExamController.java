package com.exam.controllers;

import com.exam.models.Question;
import com.exam.models.User;
import com.exam.services.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
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

//    @PostMapping("/submit")
//    public String submitAnswers(@RequestParam Long userId, @RequestParam Map<String, String> answersMap, Model model)
//    {
//        for (String answerString : answersMap.values()) {
//            try {
//                Long answerId = Long.parseLong(answerString);
//                System.out.println("answerId = " + answerId);
//                examService.saveSelectedAnswerForUser(userId, answerId);
//            } catch (NumberFormatException ex) {
//                System.err.println("Invalid answer ID format: " + answerString);
//            }
//        }
//
//        int score = examService.calculateScoreForUser(userId);
//        model.addAttribute("score", score);
//        return "result";
//    }

    @PostMapping("/submit")
    public String submitAnswer(@RequestParam(name = "userId") Long userId,
                               @RequestParam Map<String, String> allRequestParams,
                               Model model) {

        // Extract and process submitted answer here (you can retrieve it from allRequestParams)

        // Set the current question ID for the service
        String answerKey = allRequestParams.keySet().stream()
                .filter(k -> k.startsWith("answerForQuestion_"))
                .findFirst()
                .orElse(null);

        if (answerKey != null) {
            String[] parts = answerKey.split("_");
            Long currentQuestionId = Long.parseLong(parts[1]);
            examService.setCurrentQuestionId(currentQuestionId);
        }

        Question nextQuestion = examService.getNextQuestion();

        if (nextQuestion == null) {
            return "redirect:/examFinished"; // Or wherever you want to redirect when the exam is finished
        }

        model.addAttribute("question", nextQuestion);
        model.addAttribute("user", examService.getUserById(userId)); // Assumes you have a method to get a user by ID

        return "step4";
    }


    @GetMapping("/result")
    public String displayResult(@ModelAttribute User user, Model model) {
        int score = examService.calculateScoreForUser(user.getId());
        String rating = determineRating(score);
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


