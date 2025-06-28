package com.example.tracker.service;

import com.example.tracker.dto.CloudCostRequestDTO;
import com.example.tracker.dto.CloudCostResponseDTO;
import com.example.tracker.model.CloudCost;
import com.example.tracker.model.User;
import com.example.tracker.repository.CloudCostRepository;
import com.example.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class CloudCostService {

    @Autowired
    private CloudCostRepository cloudCostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CostExplorerService costExplorerService;

    private User getCurrentAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
    }

    public CloudCostResponseDTO saveCloudCost(CloudCostRequestDTO requestDTO) {
        User currentUser = getCurrentAuthenticatedUser();

        if (requestDTO.getServiceName() == null || requestDTO.getServiceName().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        if (requestDTO.getCost() == null) {
            throw new IllegalArgumentException("Cost cannot be empty");
        }

        CloudCost cloudCost = new CloudCost();
        cloudCost.setServiceName(requestDTO.getServiceName());
        cloudCost.setCost(requestDTO.getCost());
        cloudCost.setUsageType(requestDTO.getUsageType());
        cloudCost.setRegion(requestDTO.getRegion());
        cloudCost.setStartDate(requestDTO.getStartDate());
        cloudCost.setEndDate(requestDTO.getEndDate());
        cloudCost.setUser(currentUser);

        CloudCost savedCloudCost = cloudCostRepository.save(cloudCost);

        return new CloudCostResponseDTO(savedCloudCost);
    }

    public List<CloudCostResponseDTO> getAllCloudCosts() {
        User currentUser = getCurrentAuthenticatedUser();
        List<CloudCost> cloudCosts = cloudCostRepository.findByUser(currentUser);
        return cloudCosts.stream()
                .map(CloudCostResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CloudCostResponseDTO updateCloudCost(Long id, CloudCostRequestDTO requestDTO) throws Exception {
        User currentUser = getCurrentAuthenticatedUser();
        Optional<CloudCost> existingCost = cloudCostRepository.findById(id);

        if (!existingCost.isPresent()) {
            throw new Exception("Cloud cost not found.");
        }

        CloudCost cloudCost = existingCost.get();

        if (!cloudCost.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("You are not authorized to update this cloud cost.");
        }

        cloudCost.setCost(requestDTO.getCost());
        cloudCost.setServiceName(requestDTO.getServiceName());
        cloudCost.setUsageType(requestDTO.getUsageType());
        cloudCost.setRegion(requestDTO.getRegion());
        cloudCost.setStartDate(requestDTO.getStartDate());
        cloudCost.setEndDate(requestDTO.getEndDate());

        CloudCost updatedCloudCost = cloudCostRepository.save(cloudCost);
        return new CloudCostResponseDTO(updatedCloudCost);
    }

    public void deleteCloudCost(Long id) throws Exception {
        User currentUser = getCurrentAuthenticatedUser();
        Optional<CloudCost> existingCost = cloudCostRepository.findById(id);

        if (!existingCost.isPresent()) {
            throw new Exception("Cloud cost not found.");
        }

        CloudCost cloudCost = existingCost.get();

        if (!cloudCost.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("You are not authorized to delete this cloud cost.");
        }

        cloudCostRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs every day at 1 AM
    public void fetchAndSaveAwsCosts() {
        List<User> users = userRepository.findAll();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(1); // Fetch costs for yesterday

        for (User user : users) {
            if (user.getAwsIamRoleArn() != null && !user.getAwsIamRoleArn().isEmpty()) {
                try {
                    List<CloudCost> awsCosts = costExplorerService.getCostAndUsage(user, startDate, endDate);
                    for (CloudCost cost : awsCosts) {
                        // Check if a similar record already exists to avoid duplicates
                        // For simplicity, we'll just save new ones. A more robust solution would check for existing records.
                        cloudCostRepository.save(cost);
                    }
                    System.out.println("Fetched and saved AWS costs for user: " + user.getUsername());
                } catch (Exception e) {
                    System.err.println("Error fetching AWS costs for user " + user.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    public Map<String, Double> getCloudCostSummary() {
        User currentUser = getCurrentAuthenticatedUser();
        List<CloudCost> cloudCosts = cloudCostRepository.findByUser(currentUser);

        return cloudCosts.stream()
                .collect(Collectors.groupingBy(CloudCost::getServiceName,
                        Collectors.summingDouble(CloudCost::getCost)));
    }
}
