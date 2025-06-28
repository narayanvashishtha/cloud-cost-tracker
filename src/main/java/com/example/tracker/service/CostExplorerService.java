package com.example.tracker.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageRequest;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageResult;
import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.GroupDefinition;
import com.amazonaws.services.costexplorer.model.GroupDefinitionType;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.example.tracker.model.CloudCost;
import com.example.tracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CostExplorerService {

    @Autowired
    private AWSCredentialService awsCredentialService;

    public List<CloudCost> getCostAndUsage(User user, LocalDate startDate, LocalDate endDate) {
        if (user.getAwsIamRoleArn() == null || user.getAwsIamRoleArn().isEmpty()) {
            throw new IllegalArgumentException("User does not have an AWS IAM Role ARN configured.");
        }

        AWSCredentialsProvider credentialsProvider = awsCredentialService.assumeRoleAndGetCredentials(
                user.getAwsIamRoleArn(), "CloudCostTrackerSession" + user.getId()
        );

        AWSCostExplorer ceClient = AWSCostExplorerClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();

        GetCostAndUsageRequest request = new GetCostAndUsageRequest()
                .withTimePeriod(new DateInterval()
                        .withStart(startDate.format(DateTimeFormatter.ISO_DATE))
                        .withEnd(endDate.format(DateTimeFormatter.ISO_DATE)))
                .withGranularity("DAILY")
                .withMetrics("UnblendedCost")
                .withGroupBy(new GroupDefinition().withType(GroupDefinitionType.DIMENSION).withKey("SERVICE"),
                        new GroupDefinition().withType(GroupDefinitionType.DIMENSION).withKey("REGION"),
                        new GroupDefinition().withType(GroupDefinitionType.DIMENSION).withKey("USAGE_TYPE"));

        GetCostAndUsageResult result = ceClient.getCostAndUsage(request);

        List<CloudCost> cloudCosts = new ArrayList<>();
        for (ResultByTime resultByTime : result.getResultsByTime()) {
            LocalDate costDate = LocalDate.parse(resultByTime.getTimePeriod().getStart());
            for (Group group : resultByTime.getGroups()) {
                String serviceName = group.getKeys().get(0);
                String region = group.getKeys().get(1);
                String usageType = group.getKeys().get(2);
                Double cost = Double.parseDouble(group.getMetrics().get("UnblendedCost").getAmount());

                CloudCost cloudCost = new CloudCost();
                cloudCost.setServiceName(serviceName);
                cloudCost.setRegion(region);
                cloudCost.setUsageType(usageType);
                cloudCost.setCost(cost);
                cloudCost.setStartDate(costDate);
                cloudCost.setEndDate(costDate.plusDays(1)); // Assuming daily granularity
                cloudCost.setUser(user);
                cloudCosts.add(cloudCost);
            }
        }
        return cloudCosts;
    }
}
