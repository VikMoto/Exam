package com.exam.repo;

import com.exam.models.Card;
import com.exam.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
}
