package com.quiz.quiz_system.controller;

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
public class QuestionController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuestionController(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/quizzes/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questionRepository.findByQuizId(id));
        return "quiz-detail";
    }

    @PostMapping("/quizzes/{id}/questions")
    public String addQuestion(@PathVariable Long id,
            @RequestParam String text,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam String correctAnswer) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();

        Question question = new Question();
        question.setText(text);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectAnswer(correctAnswer);
        question.setQuiz(quiz); // question ta kon quiz er seta bole dao

        questionRepository.save(question);
        return "redirect:/quizzes/" + id;
    }
}