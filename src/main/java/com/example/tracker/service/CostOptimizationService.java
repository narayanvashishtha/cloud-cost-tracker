package com.example.tracker.service;

import com.example.tracker.model.CloudCost;
import com.example.tracker.model.Recommendation;
import com.example.tracker.model.User;
import com.example.tracker.repository.CloudCostRepository;
import com.example.tracker.repository.RecommendationRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CostOptimizationService {

    @Autowired
    private CloudCostRepository cloudCostRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2 AM
    public void generateRecommendations() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Example: Identify high-cost services
            List<CloudCost> userCosts = cloudCostRepository.findByUser(user);
            double totalCost = userCosts.stream().mapToDouble(CloudCost::getCost).sum();

            if (totalCost > 1000) { // Arbitrary threshold for high cost
                Recommendation recommendation = new Recommendation(
                        user,
                        "High Cost Alert",
                        "Your total cloud spending is high. Review your services for potential optimizations.",
                        null, // No specific savings estimate for this general alert
                        LocalDate.now()
                );
                recommendationRepository.save(recommendation);
            }

            // More sophisticated recommendation logic would go here
            // e.g., analyzing usage patterns, identifying idle resources (requires more data than just cost)
        }
    }

    public List<Recommendation> getRecommendationsForUser(User user) {
        return recommendationRepository.findByUser(user);
    }
}
