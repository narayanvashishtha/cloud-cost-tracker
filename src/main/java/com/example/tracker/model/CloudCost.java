package com.example.tracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cloud_costs")
public class CloudCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serviceName")
    private String serviceName;
    private double cost;

    //Constructors
    public CloudCost(){
    }

    public CloudCost(String serviceName, double cost){
        this.serviceName = serviceName;
        this.cost = cost;
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


}
