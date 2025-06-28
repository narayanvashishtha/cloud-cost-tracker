package com.example.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cloud_costs")
public class CloudCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serviceName")
    private String serviceName;
    private double cost;

    private String usageType;
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //Constructors
    public CloudCost(){
    }

    public CloudCost(String serviceName, double cost, String usageType, String region, LocalDate startDate, LocalDate endDate, User user){
        this.serviceName = serviceName;
        this.cost = cost;
        this.usageType = usageType;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }

    //Getters
    public Long getId(){
        return id;
    }
    public String getServiceName(){
        return serviceName;
    }
    public double getCost(){
        return cost;
    }
    public String getUsageType() {
        return usageType;
    }
    public String getRegion() {
        return region;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public User getUser() {
        return user;
    }

    //Setters
    public void setId(Long id){
        this.id = id;
    }
    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
    public void setCost(double cost){
        this.cost = cost;
    }
    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
