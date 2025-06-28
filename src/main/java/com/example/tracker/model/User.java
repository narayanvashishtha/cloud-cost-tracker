package com.example.tracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true) // Will store the ARN of the IAM role for STS
    private String awsIamRoleArn;

    // Constructors
    public User() {
    }

    public User(String username, String password, String awsIamRoleArn) {
        this.username = username;
        this.password = password;
        this.awsIamRoleArn = awsIamRoleArn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAwsIamRoleArn() {
        return awsIamRoleArn;
    }

    public void setAwsIamRoleArn(String awsIamRoleArn) {
        this.awsIamRoleArn = awsIamRoleArn;
    }
}
