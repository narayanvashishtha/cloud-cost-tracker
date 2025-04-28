package com.example.tracker.dto;

public class CloudCostRequestDTO {

    private String serviceName;
    private Double cost;

    //Constructors
    public CloudCostRequestDTO(){}
    public CloudCostRequestDTO(String serviceName,Double cost){
        this.serviceName = serviceName;
        this.cost = cost;
    }

    //Getters and Setters
    public String getServiceName(){
        return getServiceName();
    }
    public Double getCost(){
        return getCost();
    }

    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
    public void setCost(Double cost){
        this.cost = cost;
    }
}
