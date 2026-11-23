package com.quiz.quiz_system.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.QuizRepository;
import com.quiz.quiz_system.repository.UserRepository;
import com.quiz.quiz_system.service.QuizAccess;

@Controller
public class QuizController {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAccess quizAccess;

    public QuizController(QuizRepository quizRepository, UserRepository userRepository, QuizAccess quizAccess) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizAccess = quizAccess;
    }

    @GetMapping("/quizzes")
    public String list(Principal principal, Model model) {
        model.addAttribute("quizzes", quizRepository.findByTeacherUsername(principal.getName()));
        return "quizzes";
    }

    @PostMapping("/quizzes")
    public String create(@RequestParam String title,
            @RequestParam String description,
            @RequestParam String batches,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            Principal principal) {
        String cleanBatches = batches.replaceAll("\\s+", "");
        if (title.isBlank() || description.isBlank() || !cleanBatches.matches("\\d+(,\\d+)*")
                || deadline == null || deadline.isBefore(LocalDateTime.now())) {
            return "redirect:/quizzes?error";
        }
        Quiz quiz = new Quiz();
        quiz.setTitle(title.trim());
        quiz.setDescription(description.trim());
        quiz.setBatches(cleanBatches);
        quiz.setDeadline(deadline);
        quiz.setTeacher(userRepository.findByUsername(principal.getName()).orElseThrow());
        quizRepository.save(quiz);
        return "redirect:/quizzes";
    }

    @GetMapping("/quizzes/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("quiz", quizAccess.getOwned(id, principal.getName()));
        return "confirm-delete";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        quizRepository.delete(quizAccess.getOwned(id, principal.getName()));
        return "redirect:/quizzes";
    }
}