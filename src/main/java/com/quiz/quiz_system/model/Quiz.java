package com.quiz.quiz_system.model;

import jakarta.persistence.*;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
