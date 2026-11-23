package com.quiz.quiz_system.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.QuizRepository;

@Component
public class QuizAccess {

    private final QuizRepository quizRepository;

    public QuizAccess(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public Quiz getOwned(Long quizId, String username) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (quiz.getTeacher() == null || !quiz.getTeacher().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return quiz;
    }
}