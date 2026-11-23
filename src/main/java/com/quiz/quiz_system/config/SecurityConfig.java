package com.quiz.quiz_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.quiz.quiz_system.repository.UserRepository;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository) {
                return username -> userRepository.findByUsername(username)
                                .map(u -> User.withUsername(u.getUsername())
                                                .password(u.getPassword())
                                                .roles(u.getRole())
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/join", "/student/**", "/login", "/register",
                                                                "/css/**", "/error")
                                                .permitAll()
                                                .requestMatchers("/quizzes", "/quizzes/**").hasRole("TEACHER")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/quizzes", true)
                                                .permitAll())
                                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

                return http.build();
        }
}