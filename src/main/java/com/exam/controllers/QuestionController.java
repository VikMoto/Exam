package com.exam.controllers;

import com.exam.models.Question;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QuestionController {


    private final QuestionService questionService;

    private static int answerCount = 4;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/teacher/addQuestion")
    public String showAddQuestionForm(Model model) {
        model.addAttribute("answerCount", answerCount);
        return "addQuestion";  // Name of the Thymeleaf template
    }

    @GetMapping("/teacher/addAnswerField")
    public String addAnswerField() {
        answerCount++;
        return "redirect:/teacher/addQuestion";
    }

    @PostMapping("/teacher/addQuestion")
    public String handleQuestionSubmission(@RequestParam String content,
                                           @RequestParam String[] answer,
                                           @RequestParam(required = false) boolean[] isCorrect) {

        // Construct the Question object and save it
        Question question = new Question();
        question.setContent(content);
        // Construct the Answer objects and add them to the question here...

        questionService.saveQuestion(question);

        // Reset answerCount for the next session or user
        answerCount = 4;

        // Redirect to a success page or back to the form with a success message
        return "redirect:/teacher/addQuestion";
    }
}
