package com.exam.controllers;

import com.exam.dto.QuestionDTO;
import com.exam.models.Answer;
import com.exam.models.Question;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class QuestionController {


    private final QuestionService questionService;

    private static int answerCount = 4;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/addQuestion")
    public String showAddQuestionForm(Model model) {
        model.addAttribute("answerCount", answerCount);
        return "addQuestion";  // Name of the Thymeleaf template
    }

    @GetMapping("/addAnswerField")
    public String addAnswerField() {
        answerCount++;
        return "redirect:/teacher/addQuestion";
    }

    @PostMapping("/addQuestion")
    public String handleQuestionSubmission(QuestionDTO questionDto, @RequestParam Map<String, String> params) {
        System.out.println("Received DTO: " + questionDto);
        // Temporary debug statements
        // request.getParameterMap().forEach((key, value) -> {
        //     System.out.println(key + ": " + Arrays.toString(value));
        // });

        Question question = new Question();
        question.setContent(questionDto.getContent());

        // Extract and add answers based on prefixes
        for (int i = 1; params.containsKey("answer_" + i); i++) {
            Answer answer = new Answer();
            answer.setContent(params.get("answer_" + i));
            answer.setCorrect("on".equals(params.get("isCorrect_" + i)));
            question.addAnswer(answer);
        }

        // Extract order from the request parameters and set it to the question entity
        if (params.containsKey("order")) {
            try {
                int order = Integer.parseInt(params.get("order"));
                question.setOrder(order);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid order format provided");
            }
        }

        questionService.saveQuestion(question);

        return "redirect:/teacher/addQuestion";
    }

    @GetMapping("/resetQuestionForm")
    public String resetQuestionForm() {
        answerCount = 4; // Reset to the initial count
        return "redirect:/teacher/addQuestion";
    }


}
