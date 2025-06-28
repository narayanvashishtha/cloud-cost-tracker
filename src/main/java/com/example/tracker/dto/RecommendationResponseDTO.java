package com.example.tracker.dto;

import com.example.tracker.model.Recommendation;

import java.time.LocalDate;

public class RecommendationResponseDTO {
    private Long id;
    private String type;
    private String description;
    private Double potentialSavings;
    private LocalDate dateGenerated;
    private Long userId;

    public RecommendationResponseDTO() {
    }

    public RecommendationResponseDTO(Recommendation recommendation) {
        this.id = recommendation.getId();
        this.type = recommendation.getType();
        this.description = recommendation.getDescription();
        this.potentialSavings = recommendation.getPotentialSavings();
        this.dateGenerated = recommendation.getDateGenerated();
        this.userId = recommendation.getUser() != null ? recommendation.getUser().getId() : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
