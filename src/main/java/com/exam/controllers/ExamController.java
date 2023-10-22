package com.exam.controllers;

import com.exam.models.Question;
import com.exam.models.User;
import com.exam.services.ExamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String startExam(@ModelAttribute User user, Model model) {
        List<Question> questions = examService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("currentUser", user);
        return "step4";  // Assuming step4.html displays the questions.
    }

    @PostMapping("/submit")
    public String submitAnswers(@ModelAttribute User user, @RequestParam List<Long> answerIds, Model model) {
        for (Long answerId : answerIds) {
            examService.saveSelectedAnswerForUser(user.getId(), answerId);
        }

        int score = examService.calculateScoreForUser(user.getId());
        model.addAttribute("score", score);
        return "result";  // Assuming result.html displays the user's score.
    }

    @GetMapping("/result")
    public String displayResult(@ModelAttribute User user, Model model) {
        int score = examService.calculateScoreForUser(user.getId());
        String rating = determineRating(score);
        model.addAttribute("score", score);
        model.addAttribute("rating", rating);
        return "step17";  // Assuming step17.html displays detailed results.
    }

    private String determineRating(int score) {
        if (score >= 56) return "12";
        if (score >= 51) return "11";
        if (score >= 46) return "10";
        // ... and so on for each range.
        return "0";  // Default if none of the above.
    }

    // You can add more methods as needed for the rest of the steps.
}


