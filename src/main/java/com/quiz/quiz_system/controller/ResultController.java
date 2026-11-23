package com.quiz.quiz_system.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.quiz.quiz_system.repository.AttemptRepository;
import com.quiz.quiz_system.service.QuizAccess;

@Controller
public class ResultController {

    private final AttemptRepository attemptRepository;
    private final QuizAccess quizAccess;

    public ResultController(AttemptRepository attemptRepository, QuizAccess quizAccess) {
        this.attemptRepository = attemptRepository;
        this.quizAccess = quizAccess;
    }

    @GetMapping("/quizzes/{id}/results")
    public String results(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("quiz", quizAccess.getOwned(id, principal.getName()));
        model.addAttribute("attempts", attemptRepository.findByQuizIdOrderByTakenAtDesc(id));
        return "teacher-results";
    }
}