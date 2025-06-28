package com.example.tracker.dto;

public class UserRegistrationDTO {
    private String username;
    private String password;
    private String awsIamRoleArn;

    // Getters and Setters
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
