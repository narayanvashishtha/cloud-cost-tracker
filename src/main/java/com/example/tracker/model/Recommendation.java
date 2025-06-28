package com.example.tracker.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // e.g., "Idle Resource", "Right-Sizing", "Reserved Instance Opportunity"

    @Column(nullable = false, length = 1000)
    private String description;

    private Double potentialSavings; // Optional, estimated savings

    private LocalDate dateGenerated;

    // Constructors
    public Recommendation() {
    }

    public Recommendation(User user, String type, String description, Double potentialSavings, LocalDate dateGenerated) {
        this.user = user;
        this.type = type;
        this.description = description;
        this.potentialSavings = potentialSavings;
        this.dateGenerated = dateGenerated;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPotentialSavings() {
        return potentialSavings;
    }

    public void setPotentialSavings(Double potentialSavings) {
        this.potentialSavings = potentialSavings;
    }

    public LocalDate getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(LocalDate dateGenerated) {
        this.dateGenerated = dateGenerated;
    }
}
