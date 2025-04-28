package com.example.tracker.dto;

import java.time.LocalDate;

public class CloudCostResponseDTO {

    private Long id;
    private String serviceName;
    private Double cost;
    private LocalDate createdAt;

    //Constructors
    public CloudCostResponseDTO(){}

    public CloudCostResponseDTO(Long id, String serviceName, Double cost, LocalDate createdAt){
        this.id = id;
        this.serviceName = serviceName;
        this.cost = cost;
        this.createdAt = createdAt;
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
    public LocalDate getCreatedAt(){
        return createdAt;
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
    public void setCreatedAt(){
        this.createdAt = createdAt;
    }
}
