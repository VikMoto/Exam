package com.exam.services;

import com.exam.models.Card;
import com.exam.models.Question;
import com.exam.repo.CardRepository;
import com.exam.repo.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CardService {
    private final CardRepository cardRepository;
    private final QuestionRepository questionRepository;


    public CardService(CardRepository cardRepository, QuestionRepository questionRepository) {
        this.cardRepository = cardRepository;
        this.questionRepository = questionRepository;
    }

    // Save a new card
    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }

    // Get a card by its ID
    public Optional<Card> getCardById(Long cardId) {
        return cardRepository.findById(cardId);
    }

    // Get all cards
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    // Delete a card by its ID
    public void deleteCardById(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow();

        // Check if the card has associated questions with image paths and delete those images
        for (Question q : card.getQuestions()) {
            if (q.getImagePath() != null) {
                try {
                    Path imagePath = Paths.get("/app" + q.getImagePath()); // Assuming your image paths are like "/uploads/filename.jpg"
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Optionally log or notify about the failure to delete the image
                }
            }
        }
        questionRepository.deleteAll(card.getQuestions());
        cardRepository.deleteById(cardId);
    }

    public Card getNextCard(Long currentCardId) {
        Pageable limit = PageRequest.of(0, 1); // Limit to 1 result
        List<Card> cards = cardRepository.findCardsAfterCurrent(currentCardId, limit);

        if (cards.isEmpty()) {
            return null; // No next card found
        }

        return cards.get(0);
    }

    public Card getPreviousCard(Long currentCardId) {
        Pageable limit = PageRequest.of(0, 1); // Limit to 1 result
        List<Card> cards = cardRepository.findCardsBeforeCurrent(currentCardId, limit);

        if (cards.isEmpty()) {
            return null; // No previous card found
        }

        return cards.get(0);
    }


    public boolean existsById(Long cardId) {
        return cardRepository.existsById(cardId);
    }

    public Optional<Card> getCurrentCard(Long currentCardId) {
        return cardRepository.findById(currentCardId);
    }
}
