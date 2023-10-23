package com.exam.controllers;

import com.exam.dto.QuestionDTO;
import com.exam.models.Answer;
import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.services.CardService;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
public class QuestionController {


    private final QuestionService questionService;
    private final CardService cardService;

    private static int answerCount = 4;

    public QuestionController(QuestionService questionService, CardService cardService) {
        this.questionService = questionService;
        this.cardService = cardService;
    }


    @GetMapping("/addQuestion")
    public String showAddQuestionForm(Model model) {
        model.addAttribute("answerCount", answerCount);
        return "addQuestion";  // Name of the Thymeleaf template
    }

    @GetMapping("/addAnswerField/{cardId}")
    public String addAnswerField() {
        answerCount++;
        return "redirect:/teacher/addQuestion";
    }

    @PostMapping("/addQuestion/{cardId}")
    public String handleQuestionSubmission(QuestionDTO questionDto, @RequestParam Map<String, String> params) {
        System.out.println("Received DTO: " + questionDto);

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
                question.setQuestionOrder(order);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid order format provided");
            }
        }

        questionService.saveQuestion(question);

        return "redirect:/teacher/addQuestion";
    }

    @GetMapping("/addCard")
    public String showAddCardForm(Model model) {
        return "addCard";  // Name of the Thymeleaf template for adding a card
    }

    @PostMapping("/addCard")
    public String handleCardSubmission(@RequestParam("cardName") String cardName) {
        Card card = new Card();
        card.setName(cardName);

        List<Question> questions = new ArrayList<>();

        // Add 5 or more questions to the card
        for (int i = 1; i <= 5; i++) {
            Question question = new Question();
            question.setContent("Question " + i);
            question.setQuestionOrder(i);
            question.setCard(card);
            questions.add(question);
        }

        card.setQuestions(questions);

        // Save the card and questions to the database using your repository and service classes
        cardService.saveCard(card);
        return "redirect:/teacher/addCard";
    }

    @GetMapping("/resetQuestionForm")
    public String resetQuestionForm() {
        answerCount = 4; // Reset to the initial count
        return "redirect:/teacher/addQuestion";
    }


}
