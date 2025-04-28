package com.example.tracker.controller;

import com.example.tracker.dto.CloudCostRequestDTO;
import com.example.tracker.dto.CloudCostResponseDTO;
import com.example.tracker.service.CloudCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cloud-costs")
public class CloudCostController {

    @Autowired
    private CloudCostService cloudCostService;

    // Get endpoint - get all CloudCosts
    @GetMapping
    public ResponseEntity<List<CloudCostResponseDTO>> getAllCloudCosts() {
        List<CloudCostResponseDTO> cloudCostList = cloudCostService.getAllCloudCosts();
        return ResponseEntity.status(HttpStatus.OK).body(cloudCostList);
    }

    /* POST endpoint - add a new cloud cost
    OLD WAY, taking Entity Directly
    @PostMapping
    public CloudCost createCloudCost(@RequestBody CloudCost cloudCost){
        return cloudCostService.saveCloudCost(cloudCost);
    } */

    //DTO-based way
    @PostMapping("/cloudcost")
    public ResponseEntity<?> saveCloudCost(@RequestBody CloudCostRequestDTO requestDTO){
        try {
            // Call service to save the cloud cost
            CloudCostResponseDTO responseDTO = cloudCostService.saveCloudCost(requestDTO);

            // Return success with ResponseDTO
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (IllegalArgumentException e) {
            // Return error if validation fails
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
