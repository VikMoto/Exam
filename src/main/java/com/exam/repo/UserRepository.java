package com.exam.repo;

import com.exam.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findTopByOrderByIdDesc();
    @Query("SELECT u FROM User u JOIN FETCH u.answers WHERE u.id = (SELECT max(usr.id) FROM User usr)")
    Optional<User> findLatestUserWithAnswers();
}
