package com.quiz.quiz_system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiz.quiz_system.model.Question;
import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.QuestionRepository;
import com.quiz.quiz_system.repository.QuizRepository;

@Controller
public class StudentController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public StudentController(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/student")
    public String dashboard(Model model) {
        model.addAttribute("quizzes", quizRepository.findAll());
        return "student";
    }

    @GetMapping("/quizzes/{id}/take")
    public String take(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizRepository.findById(id).orElseThrow());
        model.addAttribute("questions", questionRepository.findByQuizId(id));
        return "take-quiz";
    }

    @PostMapping("/quizzes/{id}/submit")
    public String submit(@PathVariable Long id,
            @RequestParam Map<String, String> answers,
            Model model) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        List<Question> questions = questionRepository.findByQuizId(id);

        int score = 0;
        for (Question q : questions) {
            String chosen = answers.get("q_" + q.getId());
            if (q.getCorrectAnswer().equals(chosen)) {
                score++;
            }
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("score", score);
        model.addAttribute("total", questions.size());
        return "quiz-result";
    }
}