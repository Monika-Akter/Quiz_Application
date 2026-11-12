package com.quiz.quiz_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.QuizRepository;

@Controller
public class QuizController {

    private final QuizRepository quizRepository;

    public QuizController(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }
    //quiz list dekhano
    @GetMapping("/quizzes")
    public String list(Model model) {
        model.addAttribute("quizzes", quizRepository.findAll());
        return "quizzes";
    }
    //notun quiz save kora
    @PostMapping("/quizzes")
    public String create(@RequestParam String title, @RequestParam String description) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quizRepository.save(quiz);
        return "redirect:/quizzes";
    }
}