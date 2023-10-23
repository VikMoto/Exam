package com.exam.services;

import com.exam.models.Card;
import com.exam.repo.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {
    private final CardRepository cardRepository;


    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
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
}
