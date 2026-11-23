package com.quiz.quiz_system.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiz.quiz_system.model.Attempt;
import com.quiz.quiz_system.model.Question;
import com.quiz.quiz_system.model.Quiz;
import com.quiz.quiz_system.repository.AttemptRepository;
import com.quiz.quiz_system.repository.QuestionRepository;
import com.quiz.quiz_system.repository.QuizRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    private static final String INFO = "studentInfo";
    private static final String STATE = "quizState";
    private static final long GRACE_MS = 1000;

    static class StudentInfo {
        String name;
        String university;
        String department;
        String batch;
        String studentNo;
    }

    static class QuizState {
        Long quizId;
        Long attemptId;
        List<Long> questionIds = new ArrayList<>();
        int index = 0;
        int score = 0;
        long deadline;
    }

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    public StudentController(QuizRepository quizRepository,
            QuestionRepository questionRepository,
            AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    private StudentInfo getInfo(HttpSession session) {
        Object o = session.getAttribute(INFO);
        return o instanceof StudentInfo i ? i : null;
    }

    private QuizState getState(HttpSession session, Long quizId) {
        Object o = session.getAttribute(STATE);
        if (o instanceof QuizState s && s.quizId.equals(quizId)) {
            return s;
        }
        return null;
    }

    private void startQuestion(QuizState s) {
        Question q = questionRepository.findById(s.questionIds.get(s.index)).orElseThrow();
        int seconds = q.getTimeLimit() != null ? q.getTimeLimit() : 30;
        s.deadline = System.currentTimeMillis() + seconds * 1000L;
    }

    private String next(QuizState s, Long quizId) {
        s.index++;
        if (s.index >= s.questionIds.size()) {
            return "redirect:/student/quizzes/" + quizId + "/result";
        }
        startQuestion(s);
        return "redirect:/student/quizzes/" + quizId + "/question";
    }

    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }

    @PostMapping("/join")
    public String join(@RequestParam String fullName,
            @RequestParam String university,
            @RequestParam String department,
            @RequestParam String batch,
            @RequestParam String studentNo,
            HttpSession session,
            Model model) {
        fullName = fullName.trim();
        university = university.trim();
        department = department.trim();
        batch = batch.trim();
        studentNo = studentNo.trim();

        model.addAttribute("fullName", fullName);
        model.addAttribute("university", university);
        model.addAttribute("department", department);
        model.addAttribute("batch", batch);
        model.addAttribute("studentNo", studentNo);

        if (fullName.isEmpty() || university.isEmpty() || department.isEmpty()
                || batch.isEmpty() || studentNo.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            return "join";
        }
        if (!batch.matches("\\d+")) {
            model.addAttribute("error", "Batch must be a number.");
            return "join";
        }

        StudentInfo info = new StudentInfo();
        info.name = fullName;
        info.university = university;
        info.department = department;
        info.batch = batch;
        info.studentNo = studentNo;
        session.setAttribute(INFO, info);
        session.removeAttribute(STATE);
        return "redirect:/student/quizzes";
    }

    @GetMapping("/student/exit")
    public String exit(HttpSession session) {
        session.invalidate();
        return "redirect:/join";
    }

    @GetMapping("/student/quizzes")
    public String available(HttpSession session, Model model) {
        StudentInfo info = getInfo(session);
        if (info == null) {
            return "redirect:/join";
        }

        Set<Long> takenIds = new HashSet<>();
        for (Attempt a : attemptRepository.findByStudentNo(info.studentNo)) {
            takenIds.add(a.getQuiz().getId());
        }

        List<Quiz> available = new ArrayList<>();
        for (Quiz q : quizRepository.findAll()) {
            if (q.isForBatch(info.batch) && q.isOpen() && !takenIds.contains(q.getId())
                    && !questionRepository.findByQuizId(q.getId()).isEmpty()) {
                available.add(q);
            }
        }

        model.addAttribute("studentName", info.name);
        model.addAttribute("studentNo", info.studentNo);
        model.addAttribute("batch", info.batch);
        model.addAttribute("quizzes", available);
        return "student";
    }

    @GetMapping("/student/quizzes/{id}/start")
    public String start(@PathVariable Long id, HttpSession session) {
        StudentInfo info = getInfo(session);
        if (info == null) {
            return "redirect:/join";
        }
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null || !quiz.isForBatch(info.batch)) {
            return "redirect:/student/quizzes";
        }
        if (!quiz.isOpen()) {
            return "redirect:/student/quizzes?closed";
        }
        if (attemptRepository.existsByStudentNoAndQuizId(info.studentNo, id)) {
            return "redirect:/student/quizzes?already";
        }
        List<Question> questions = questionRepository.findByQuizId(id);
        if (questions.isEmpty()) {
            return "redirect:/student/quizzes";
        }

        Attempt attempt = new Attempt();
        attempt.setStudentName(info.name);
        attempt.setUniversity(info.university);
        attempt.setDepartment(info.department);
        attempt.setBatch(info.batch);
        attempt.setStudentNo(info.studentNo);
        attempt.setQuiz(quiz);
        attempt.setScore(0);
        attempt.setTotal(questions.size());
        attempt.setTakenAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        QuizState s = new QuizState();
        s.quizId = id;
        s.attemptId = attempt.getId();
        for (Question q : questions) {
            s.questionIds.add(q.getId());
        }
        startQuestion(s);
        session.setAttribute(STATE, s);
        return "redirect:/student/quizzes/" + id + "/question";
    }

    @GetMapping("/student/quizzes/{id}/question")
    public String question(@PathVariable Long id, HttpSession session, Model model) {
        QuizState s = getState(session, id);
        if (s == null) {
            return "redirect:/student/quizzes";
        }
        if (s.index >= s.questionIds.size()) {
            return "redirect:/student/quizzes/" + id + "/result";
        }

        long remaining = s.deadline - System.currentTimeMillis();
        if (remaining <= 0) {
            return next(s, id);
        }

        Question q = questionRepository.findById(s.questionIds.get(s.index)).orElseThrow();
        model.addAttribute("quiz", quizRepository.findById(id).orElseThrow());
        model.addAttribute("question", q);
        model.addAttribute("number", s.index + 1);
        model.addAttribute("total", s.questionIds.size());
        model.addAttribute("secondsLeft", (remaining + 999) / 1000);
        return "take-quiz";
    }

    @PostMapping("/student/quizzes/{id}/answer")
    public String answer(@PathVariable Long id,
            @RequestParam(required = false) String answer,
            HttpSession session) {
        QuizState s = getState(session, id);
        if (s == null) {
            return "redirect:/student/quizzes";
        }
        if (s.index >= s.questionIds.size()) {
            return "redirect:/student/quizzes/" + id + "/result";
        }

        Question q = questionRepository.findById(s.questionIds.get(s.index)).orElseThrow();
        boolean inTime = System.currentTimeMillis() <= s.deadline + GRACE_MS;
        if (inTime && q.getCorrectAnswer().equals(answer)) {
            s.score++;
            Attempt attempt = attemptRepository.findById(s.attemptId).orElseThrow();
            attempt.setScore(s.score);
            attemptRepository.save(attempt);
        }
        return next(s, id);
    }

    @GetMapping("/student/quizzes/{id}/result")
    public String result(@PathVariable Long id, HttpSession session, Model model) {
        QuizState s = getState(session, id);
        if (s == null || s.index < s.questionIds.size()) {
            return "redirect:/student/quizzes";
        }
        session.removeAttribute(STATE);

        model.addAttribute("quiz", quizRepository.findById(id).orElseThrow());
        model.addAttribute("score", s.score);
        model.addAttribute("total", s.questionIds.size());
        return "quiz-result";
    }
}