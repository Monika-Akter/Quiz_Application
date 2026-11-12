package com.quiz.quiz_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quiz.quiz_system.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}