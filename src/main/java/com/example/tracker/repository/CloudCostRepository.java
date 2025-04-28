package com.example.tracker.repository;

import com.example.tracker.model.CloudCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudCostRepository extends JpaRepository<CloudCost, Long> {
}
