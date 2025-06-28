package com.example.tracker.dto;

import java.time.LocalDate;

public class CloudCostRequestDTO {

    private String serviceName;
    private Double cost;
    private String usageType;
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;

    //Constructors
    public CloudCostRequestDTO(){}
    public CloudCostRequestDTO(String serviceName, Double cost, String usageType, String region, LocalDate startDate, LocalDate endDate){
        this.serviceName = serviceName;
        this.cost = cost;
        this.usageType = usageType;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    //Getters and Setters
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
}
