package com.example.tracker.service;

import com.example.tracker.dto.CloudCostRequestDTO;
import com.example.tracker.dto.CloudCostResponseDTO;
import com.example.tracker.repository.CloudCostRepository;
import com.example.tracker.model.CloudCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CloudCostService {

    @Autowired
    private CloudCostRepository cloudCostRepository;

    /* OLD way
     Save a new cloud cost
    public List<CloudCost> getAllCloudCosts(){
        return cloudCostRepository.findAll();
    }
    // Save a new cloud cost
    public CloudCost saveCloudCost(CloudCost cloudCost){
        return cloudCostRepository.save(cloudCost);
    } */

    //DTO based

    //Method to save all CloudCosts
    public CloudCostResponseDTO saveCloudCost(CloudCostRequestDTO requestDTO) {
        //Validate Fields
        if (requestDTO.getServiceName() == null || requestDTO.getServiceName().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        if (requestDTO.getCost() == null) {
            throw new IllegalArgumentException("Cost cannot be empty");
        }

        // Create CloudCost entity from DTO
        CloudCost cloudCost = new CloudCost();
        cloudCost.setServiceName(requestDTO.getServiceName());
        cloudCost.setCost(requestDTO.getCost());

        // Save to database
        CloudCost savedCloudCost = cloudCostRepository.save(cloudCost);

        // Return saved data as ResponseDTO
        CloudCostResponseDTO responseDTO = new CloudCostResponseDTO();
        responseDTO.setId(savedCloudCost.getId());
        responseDTO.setServiceName(savedCloudCost.getServiceName());
        responseDTO.setCost(savedCloudCost.getCost());

        return responseDTO;
    }

    // Method to get all CloudCosts
    public List<CloudCostResponseDTO> getAllCloudCosts() {
        List<CloudCost> cloudCosts = cloudCostRepository.findAll(); // Fetch all records from the DB
        return cloudCosts.stream().map(cloudCost -> {
            CloudCostResponseDTO responseDTO = new CloudCostResponseDTO();
            responseDTO.setId(cloudCost.getId());
            responseDTO.setServiceName(cloudCost.getServiceName());
            responseDTO.setCost(cloudCost.getCost());
            return responseDTO;
        }).collect(Collectors.toList());
    }
}
