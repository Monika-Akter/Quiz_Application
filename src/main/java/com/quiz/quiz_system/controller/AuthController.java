package com.quiz.quiz_system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quiz.quiz_system.model.AppUser;
import com.quiz.quiz_system.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${quiz.teacher-code}")
    private String teacherCode;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/join";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String accessCode,
            Model model) {
        fullName = fullName.trim();
        username = username.trim();
        model.addAttribute("fullName", fullName);
        model.addAttribute("username", username);

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || accessCode.isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            return "register";
        }
        if (!accessCode.equals(teacherCode)) {
            model.addAttribute("error", "Invalid teacher access code.");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            return "register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "This username is already taken. Please choose a different one.");
            return "register";
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("TEACHER");
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}