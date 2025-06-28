package com.example.tracker.controller;

import com.example.tracker.dto.CloudCostRequestDTO;
import com.example.tracker.dto.CloudCostResponseDTO;
import com.example.tracker.service.CloudCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloud-costs")
public class CloudCostController {

    @Autowired
    private CloudCostService cloudCostService;

    @GetMapping
    public ResponseEntity<List<CloudCostResponseDTO>> getAllCloudCosts() {
        List<CloudCostResponseDTO> cloudCostList = cloudCostService.getAllCloudCosts();
        return ResponseEntity.status(HttpStatus.OK).body(cloudCostList);
    }

    @PostMapping
    public ResponseEntity<?> saveCloudCost(@RequestBody CloudCostRequestDTO requestDTO){
        try {
            CloudCostResponseDTO responseDTO = cloudCostService.saveCloudCost(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the cloud cost.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCloudCost(@PathVariable Long id, @RequestBody CloudCostRequestDTO requestDTO) {
        try {
            CloudCostResponseDTO updatedCloudCost = cloudCostService.updateCloudCost(id, requestDTO);
            return new ResponseEntity<>(updatedCloudCost, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Or a custom error response
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCloudCost(@PathVariable Long id) {
        try {
            cloudCostService.deleteCloudCost(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Double>> getCloudCostSummary() {
        Map<String, Double> summary = cloudCostService.getCloudCostSummary();
        return ResponseEntity.ok(summary);
    }
}
