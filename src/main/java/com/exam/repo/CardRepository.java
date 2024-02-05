package com.exam.repo;

import com.exam.models.Card;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.id > :currentCardId ORDER BY c.id ASC")
    List<Card> findCardsAfterCurrent(@Param("currentCardId") Long currentCardId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.id < :currentCardId ORDER BY c.id DESC")
    List<Card> findCardsBeforeCurrent(@Param("currentCardId") Long currentCardId, Pageable limit);

    @Query("SELECT c FROM Card c ORDER BY c.id ASC")
    
    List<Card> findAllOrderedById();

    @Query("SELECT c FROM Card c JOIN c.questions q WHERE q.id = :questionId")
    Card findCardByQuestionId(@Param("questionId") Long questionId);


    List<Card> findAllByOrderByIdAsc();
}
