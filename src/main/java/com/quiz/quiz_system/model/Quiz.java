package com.quiz.quiz_system.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String batches;
    private LocalDateTime deadline;

    @ManyToOne
    private AppUser teacher;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<Attempt> attempts = new ArrayList<>();

    public boolean isForBatch(String batch) {
        if (batches == null || batch == null)
            return false;
        for (String b : batches.split(",")) {
            if (b.trim().equals(batch.trim()))
                return true;
        }
        return false;
    }

    public boolean isOpen() {
        return deadline == null || LocalDateTime.now().isBefore(deadline);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBatches() {
        return batches;
    }

    public void setBatches(String batches) {
        this.batches = batches;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public AppUser getTeacher() {
        return teacher;
    }

    public void setTeacher(AppUser teacher) {
        this.teacher = teacher;
    }
}