package com.exam.controllers;

import com.exam.dto.QuestionDTO;
import com.exam.models.Answer;
import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.services.AnswerService;
import com.exam.services.CardService;
import com.exam.services.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/teacher")
@SessionAttributes("currentCard") // Ensure the card is stored in the session
public class QuestionController {
    private final QuestionService questionService;
    private final CardService cardService;
    private final AnswerService answerService;
    private static int answerCount = 2;

    public QuestionController(QuestionService questionService, CardService cardService, AnswerService answerService) {
        this.questionService = questionService;
        this.cardService = cardService;
        this.answerService = answerService;
    }

    @GetMapping("/admin-panel")
    public String adminPanel() {
        return "redirect:/admin"; // This should be the name of your Thymeleaf template without the .html extension.
    }

    @GetMapping("/manage-cards")
    public String showManageCards(Model model) {
        Iterable<Card> cards = cardService.getAllCards();  // Assuming you have this method in your CardService
        model.addAttribute("cards", cards);
        return "manage-cards";  // Name of the Thymeleaf template you provided
    }

    @GetMapping("/add-question")
    public String showAddQuestionForm(@RequestParam(name = "cardId", required = false) Long cardId, Model model) {
        if (cardId != null) {
            // Fetch the card using the provided ID
            Card currentCard = cardService.getCardById(cardId).orElseThrow();
            model.addAttribute("currentCard", currentCard);
        }
        model.addAttribute("answerCount", answerCount);
        return "add-question";  // Name of the Thymeleaf template
    }


    @GetMapping("/add-question/{cardId}")
    public String showAddQuestionFormWithPath(@PathVariable Long cardId, Model model) {
        // Fetch the card using the provided ID from the path variable
        Card currentCard = cardService.getCardById(cardId).orElseThrow();
        model.addAttribute("currentCard", currentCard);
        model.addAttribute("answerCount", answerCount);
        return "add-question";  // Name of the Thymeleaf template
    }

    @PostMapping("/add-question/{cardId}")
    public String handleQuestionSubmissionForCard(@PathVariable Long cardId,
                                                  QuestionDTO questionDto,
                                                  @RequestParam Map<String, String> params,
                                                  @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        Card currentCard = cardService.getCardById(cardId).orElseThrow();
        return "redirect:/teacher/add-question/" + cardId;  // Redirect back to the add-question for the same card
    }



    @GetMapping("/addAnswerField")
    public String addAnswerField() {
        answerCount++;
        return "redirect:/teacher/add-question";
    }

    @PostMapping("/add-question")
    public String handleQuestionSubmission(QuestionDTO questionDto,
                                           @RequestParam Map<String, String> params,
                                           @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                                           @SessionAttribute(name = "currentCard", required = false) Card currentCard) {
        // Check if there's a current card in the session
        if (currentCard == null) {
            // If not, create a new card
            currentCard = new Card();
            cardService.saveCard(currentCard);
        }

        Question question = new Question();
        question.setContent(questionDto.getContent());

        try {
            handleImageFileUpload(imageFile, question);
        } catch (Exception e) {
            return String.valueOf(handleException(e));
        }

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
        answerCount = 2; // Reset to the initial count
        return "redirect:/teacher/add-question";
    }

    private void handleImageFileUpload(MultipartFile imageFile, Question question) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // Change the directory path to the path mapped to the Docker volume
            Path uploadDir = Paths.get("/app/uploads/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path imagePath = uploadDir.resolve(Objects.requireNonNull(imageFile.getOriginalFilename()));
            Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            question.setImagePath("/uploads/" + imageFile.getOriginalFilename());
        }
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        e.printStackTrace();
        // Here, might also log the error or notify the user about the failure.
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorMessage", "An error occurred while processing your request.");
        return modelAndView;
    }


    @GetMapping("/add-card")
    public String showAddCardForm(Model model) {
        model.addAttribute("questionCount", answerCount); // Add questionCount to the model
        return "add-card";  // Name of the Thymeleaf template for adding a card
    }


    @PostMapping("/add-card")
    public String handleCardSubmission(@RequestParam("cardName") String cardName) {
        // Create a new card and save it to the database
        Card card = new Card();
        card.setName(cardName);
        Card saveCard = cardService.saveCard(card);

        // Extract the newly created card's ID
        System.out.println("saveCard = " + saveCard);
        Long cardId = saveCard.getId();

        // Redirect to the question adding page with the card's ID as a parameter
        return "redirect:/teacher/add-question?cardId=" + cardId;
    }


    @GetMapping("/update-card/{cardId}")
    public String showUpdateCardForm(@PathVariable Long cardId, Model model) {
        Card card = cardService.getCardById(cardId).orElseThrow();

        model.addAttribute("card", card);
        return "update-card";
    }

    @PostMapping("/update-card/{cardId}")
    public String handleCardUpdate(@PathVariable Long cardId,
                                   @ModelAttribute Card card,
                                   @RequestParam Map<String, String> params) {
        if (!cardService.existsById(cardId)) {
            throw new RuntimeException("Card not found!");
        }

        // Fetch the existing card and update its details
        Card existingCard = cardService.getCardById(cardId).orElseThrow();
        existingCard.setName(card.getName());

        // Iterate over each question in the card
        for (Question question : existingCard.getQuestions()) {
            String questionContent = params.get("question_" + question.getId());
            if (questionContent != null) {
                question.setContent(questionContent);
            }

            // Iterate over each answer in the question
            for (Answer answer : question.getAnswers()) {
                String answerContent = params.get("answer_" + answer.getId());
                if (answerContent != null) {
                    answer.setContent(answerContent);
                }
                String isCorrect = params.get("isCorrect_" + answer.getId());
                answer.setCorrect("on".equals(isCorrect));
                // Save the updated answer
                answerService.saveAnswer(answer);
            }
            // Save the updated question
            questionService.saveQuestion(question);
        }
        // Save the updated card
        cardService.saveCard(existingCard);
        return "redirect:/teacher/manage-cards";
    }


    @GetMapping("/delete-card/{cardId}")
    public String deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return "redirect:/teacher/manage-cards";
    }


    @GetMapping("/update-question/{questionId}")
    public String showUpdateQuestionForm(@PathVariable Long questionId, Model model) {
        Question question = (Question) questionService.getQuestionById(questionId);
        model.addAttribute("question", question);
        return "update-question";
    }

    @PostMapping("/updateQuestion")
    public String handleQuestionUpdate(@ModelAttribute Question question) {
        questionService.saveQuestion(question);
        return "redirect:/teacher/add-question";
    }

    @GetMapping("/deleteQuestion/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return "redirect:/teacher/manage-cards";
    }


    @GetMapping("/add-answer/{questionId}")
    public String showAddAnswerForm(@PathVariable Long questionId, Model model) {
        Question question = (Question) questionService.getQuestionById(questionId);
        model.addAttribute("question", question);
        return "add-answer";  // Name of the Thymeleaf template for adding an answer
    }

    @PostMapping("/add-answer")
    public String handleAnswerSubmission(@ModelAttribute Answer answer,
                                         @SessionAttribute(name = "question", required = false) Question question) {
        if (question == null) {
            return "redirect:/teacher/manage-cards";
        }
        answer.setQuestion(question);
        answerService.saveAnswer(answer);
        return "redirect:/teacher/manage-cards";
    }

    @GetMapping("/update-answer/{answerId}")
    public String showUpdateAnswerForm(@PathVariable Long answerId, Model model) {
        Answer answer = (Answer) answerService.getAnswerById(answerId).orElseThrow();
        model.addAttribute("answer", answer);
        return "update-answer";
    }


    @PostMapping("/update-answer")
    public String handleAnswerUpdate(@ModelAttribute Answer answer) {
        answerService.saveAnswer(answer);
        return "redirect:/teacher/add-question";  // Or redirect to a suitable view
    }

    @GetMapping("/delete-answer/{answerId}")
    public String deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        return "redirect:/teacher/manage-cards";
    }

    @GetMapping("/resetQuestionForm")
    public String resetQuestionForm() {
        answerCount = 2; // Reset to the initial count
        return "redirect:/teacher/manage-cards";
    }

    @GetMapping("/endQuestionForm")
    public String endQuestionForm(SessionStatus sessionStatus) {
        // Complete the session for the current card
        sessionStatus.setComplete();
        return "redirect:/teacher/manage-cards";
    }
}
