package com.example.tracker.controller;

import com.example.tracker.dto.CloudCostRequestDTO;
import com.example.tracker.dto.CloudCostResponseDTO;
import com.example.tracker.model.CloudCost;
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

    @PutMapping("/{id}")
    public ResponseEntity<CloudCostResponseDTO> updateCloudCost(@PathVariable Long id, @RequestBody CloudCostRequestDTO requestDTO) {
        // Validate and check for null fields
        if (requestDTO.getServiceName() == null || requestDTO.getCost() == null) {
            return ResponseEntity.badRequest().body(null); // or some custom error message
        }
        try {
            CloudCost updatedCloudCost = cloudCostService.updateCloudCost(id, requestDTO);
            CloudCostResponseDTO responseDTO = new CloudCostResponseDTO(updatedCloudCost);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            // Handle the exception, maybe log it or send a custom response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Or a custom error response
        }
    }
    @DeleteMapping("/cloud-cost/{id}")
    public ResponseEntity<Void> deleteCloudCost(@PathVariable Long id) {
        cloudCostService.deleteCloudCost(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
