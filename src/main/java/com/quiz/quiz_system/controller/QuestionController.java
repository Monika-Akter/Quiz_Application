package com.quiz.quiz_system.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.quiz.quiz_system.model.Question;
import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.QuestionRepository;
import com.quiz.quiz_system.service.QuizAccess;

@Controller
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final QuizAccess quizAccess;

    public QuestionController(QuestionRepository questionRepository, QuizAccess quizAccess) {
        this.questionRepository = questionRepository;
        this.quizAccess = quizAccess;
    }

    private Question getQuestion(Long quizId, Long qid) {
        Question q = questionRepository.findById(qid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.getQuiz().getId().equals(quizId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return q;
    }

    private boolean isValid(String text, String a, String b, String c, String d, String correct, Integer time) {
        return !text.isBlank() && !a.isBlank() && !b.isBlank() && !c.isBlank() && !d.isBlank()
                && correct.matches("[ABCD]") && time != null && time >= 5;
    }

    @GetMapping("/quizzes/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        Quiz quiz = quizAccess.getOwned(id, principal.getName());
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
            @RequestParam String correctAnswer,
            @RequestParam Integer timeLimit,
            Principal principal) {
        Quiz quiz = quizAccess.getOwned(id, principal.getName());
        if (!isValid(text, optionA, optionB, optionC, optionD, correctAnswer, timeLimit)) {
            return "redirect:/quizzes/" + id + "?error";
        }

        Question question = new Question();
        question.setText(text.trim());
        question.setOptionA(optionA.trim());
        question.setOptionB(optionB.trim());
        question.setOptionC(optionC.trim());
        question.setOptionD(optionD.trim());
        question.setCorrectAnswer(correctAnswer);
        question.setTimeLimit(timeLimit);
        question.setQuiz(quiz);
        questionRepository.save(question);
        return "redirect:/quizzes/" + id;
    }

    @PostMapping("/quizzes/{id}/questions/{qid}/delete")
    public String deleteQuestion(@PathVariable Long id, @PathVariable Long qid, Principal principal) {
        quizAccess.getOwned(id, principal.getName());
        questionRepository.delete(getQuestion(id, qid));
        return "redirect:/quizzes/" + id;
    }

    @GetMapping("/quizzes/{id}/questions/{qid}/edit")
    public String editForm(@PathVariable Long id, @PathVariable Long qid, Principal principal, Model model) {
        model.addAttribute("quiz", quizAccess.getOwned(id, principal.getName()));
        model.addAttribute("question", getQuestion(id, qid));
        return "edit-question";
    }

    @PostMapping("/quizzes/{id}/questions/{qid}/edit")
    public String updateQuestion(@PathVariable Long id,
            @PathVariable Long qid,
            @RequestParam String text,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam String correctAnswer,
            @RequestParam Integer timeLimit,
            Principal principal) {
        quizAccess.getOwned(id, principal.getName());
        Question question = getQuestion(id, qid);
        if (!isValid(text, optionA, optionB, optionC, optionD, correctAnswer, timeLimit)) {
            return "redirect:/quizzes/" + id + "?error";
        }

        question.setText(text.trim());
        question.setOptionA(optionA.trim());
        question.setOptionB(optionB.trim());
        question.setOptionC(optionC.trim());
        question.setOptionD(optionD.trim());
        question.setCorrectAnswer(correctAnswer);
        question.setTimeLimit(timeLimit);
        questionRepository.save(question);
        return "redirect:/quizzes/" + id;
    }
}