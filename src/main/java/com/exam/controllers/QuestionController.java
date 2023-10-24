package com.exam.controllers;

import com.exam.dto.QuestionDTO;
import com.exam.models.Answer;
import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.services.CardService;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import java.util.Map;

@Controller
@RequestMapping("/teacher")
@SessionAttributes("currentCard") // Ensure the card is stored in the session
public class QuestionController {

    private final QuestionService questionService;
    private final CardService cardService;

    private static int answerCount = 4;

    public QuestionController(QuestionService questionService, CardService cardService) {
        this.questionService = questionService;
        this.cardService = cardService;
    }



    @GetMapping("/addQuestion")
    public String showAddQuestionForm(@RequestParam(name = "cardId", required = false) Long cardId, Model model) {
        if (cardId != null) {
            // Fetch the card using the provided ID
            Card currentCard = cardService.getCardById(cardId).orElseThrow();
            model.addAttribute("currentCard", currentCard);
        }
        model.addAttribute("answerCount", answerCount);
        return "addQuestion";  // Name of the Thymeleaf template
    }

    @GetMapping("/addAnswerField")
    public String addAnswerField() {
        answerCount++;
        return "redirect:/teacher/addQuestion";
    }

    @PostMapping("/addQuestion")
    public String handleQuestionSubmission(QuestionDTO questionDto, @RequestParam Map<String, String> params,
                                           @SessionAttribute(name = "currentCard", required = false) Card currentCard) {
        // Check if there's a current card in the session
        System.out.println("currentCard = " + currentCard);
        if (currentCard == null) {
            // If not, create a new card
            currentCard = new Card();
            cardService.saveCard(currentCard);
        }

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

        // Set the current card for the question
        question.setCard(currentCard);
        questionService.saveQuestion(question);

        return "redirect:/teacher/addQuestion";
    }

    @GetMapping("/addCard")
    public String showAddCardForm(Model model) {
        model.addAttribute("questionCount", answerCount); // Add questionCount to the model
        return "addCard";  // Name of the Thymeleaf template for adding a card
    }


    @PostMapping("/addCard")
    public String handleCardSubmission(@RequestParam("cardName") String cardName,
                                       @RequestParam Map<String, String> params) {
        // Create a new card and save it to the database
        Card card = new Card();
        card.setName(cardName);
        Card saveCard = cardService.saveCard(card);

        // Extract the newly created card's ID
        System.out.println("saveCard = " + saveCard);
        Long cardId = saveCard.getId();


        // Redirect to the question adding page with the card's ID as a parameter
        return "redirect:/teacher/addQuestion?cardId=" + cardId;
    }

    @GetMapping("/resetQuestionForm")
    public String resetQuestionForm() {
        answerCount = 4; // Reset to the initial count
        return "redirect:/teacher/addCard";
    }

    @GetMapping("/endQuestionForm")
    public String endQuestionForm(SessionStatus sessionStatus) {
        // Complete the session for the current card
        sessionStatus.setComplete();
        return "redirect:/teacher/addCard";
    }
}
