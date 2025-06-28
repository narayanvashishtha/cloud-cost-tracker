package com.example.tracker.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.springframework.stereotype.Service;

@Service
public class AWSCredentialService {

    public AWSCredentialsProvider assumeRoleAndGetCredentials(String roleArn, String roleSessionName) {
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().build();

        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withRoleSessionName(roleSessionName);

        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);

        return new AWSStaticCredentialsProvider(new BasicSessionCredentials(
                assumeRoleResult.getCredentials().getAccessKeyId(),
                assumeRoleResult.getCredentials().getSecretAccessKey(),
                assumeRoleResult.getCredentials().getSessionToken()
        ));
    }
}
