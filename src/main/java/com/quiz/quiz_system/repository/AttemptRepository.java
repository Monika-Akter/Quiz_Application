package com.quiz.quiz_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quiz.quiz_system.model.Attempt;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByQuizIdOrderByTakenAtDesc(Long quizId);

    List<Attempt> findByStudentNo(String studentNo);

    boolean existsByStudentNoAndQuizId(String studentNo, Long quizId);
}