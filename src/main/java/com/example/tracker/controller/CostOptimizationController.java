package com.example.tracker.controller;

import com.example.tracker.dto.RecommendationResponseDTO;
import com.example.tracker.model.User;
import com.example.tracker.repository.UserRepository;
import com.example.tracker.service.CostOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class CostOptimizationController {

    @Autowired
    private CostOptimizationService costOptimizationService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponseDTO>> getRecommendationsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        List<RecommendationResponseDTO> recommendations = costOptimizationService.getRecommendationsForUser(currentUser).stream()
                .map(RecommendationResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recommendations);
    }
}
