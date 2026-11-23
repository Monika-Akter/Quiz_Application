package com.quiz.quiz_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quiz.quiz_system.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByTeacherUsername(String username);
}