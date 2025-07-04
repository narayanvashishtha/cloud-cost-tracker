package com.example.tracker.repository;

import com.example.tracker.model.CloudCost;
import com.example.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudCostRepository extends JpaRepository<CloudCost, Long> {
    List<CloudCost> findByUser(User user);
}
