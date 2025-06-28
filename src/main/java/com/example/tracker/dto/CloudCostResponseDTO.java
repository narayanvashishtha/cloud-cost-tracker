package com.example.tracker.dto;

import com.example.tracker.model.CloudCost;

import java.time.LocalDate;

public class CloudCostResponseDTO {

    private Long id;
    private String serviceName;
    private Double cost;
    private String usageType;
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;

    //Constructors
    public CloudCostResponseDTO(){}

    public CloudCostResponseDTO(Long id, String serviceName, Double cost, String usageType, String region, LocalDate startDate, LocalDate endDate, Long userId){
        this.id = id;
        this.serviceName = serviceName;
        this.cost = cost;
        this.usageType = usageType;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public CloudCostResponseDTO(CloudCost cloudCost){
        this.id = cloudCost.getId();
        this.cost = cloudCost.getCost();
        this.serviceName = cloudCost.getServiceName();
        this.usageType = cloudCost.getUsageType();
        this.region = cloudCost.getRegion();
        this.startDate = cloudCost.getStartDate();
        this.endDate = cloudCost.getEndDate();
        this.userId = cloudCost.getUser() != null ? cloudCost.getUser().getId() : null;
    }

    //Getters and Setters
    public Long getId(){
        return id;
    }
    public String getServiceName(){
        return serviceName;
    }
    public Double getCost(){
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
    public Long getUserId() {
        return userId;
    }

    public void setId(Long id){
        this.id = id;
    }
    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
    public void setCost(Double cost){
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
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
