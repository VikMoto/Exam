package com.exam.services;

import com.exam.models.Card;
import com.exam.repo.CardRepository;
import com.exam.repo.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        questionRepository.deleteAll(card.getQuestions());
        cardRepository.deleteById(cardId);
    }


    public boolean existsById(Long cardId) {
        return cardRepository.existsById(cardId);
    }
}
