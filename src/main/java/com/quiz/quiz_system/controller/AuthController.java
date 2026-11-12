package com.quiz.quiz_system.controller;

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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
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
            @RequestParam String role,
            Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role.equals("TEACHER") ? "TEACHER" : "STUDENT");
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}