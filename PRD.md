# Product Requirements Document: Cloud Cost Tracker

## 1. Introduction

The Cloud Cost Tracker is a Spring Boot application designed to help users monitor, manage, and optimize their cloud spending, primarily focusing on AWS. The application will provide a secure platform for users to track their cloud costs, integrate directly with AWS Cost Explorer to fetch detailed billing data, and offer actionable recommendations for cost optimization.

## 2. Goals

*   Provide a secure, multi-user platform for cloud cost tracking.
*   Enable users to view their AWS cloud costs broken down by service, region, and other dimensions.
*   Offer insights and recommendations for optimizing cloud spending.
*   Automate the fetching of cloud cost data from AWS.
*   Provide a user-friendly API for integration with potential front-end applications.

## 3. Authentication and Authorization

### 3.1 User Management
*   **User Registration:** Users will be able to create an account with a unique username and password.
*   **User Login:** Authenticated users will be able to log in to the application.
*   **Password Management:** Secure password storage (hashing and salting) and mechanisms for password reset.

### 3.2 Authentication Mechanism
*   **Spring Security:** The application will leverage Spring Security for robust authentication and authorization.
*   **JWT (JSON Web Tokens) or Session-based Authentication:** For API security, either JWTs (for statelessness) or traditional session management will be implemented to secure REST endpoints.

### 3.3 Secure AWS Credential Handling (Crucial)
Directly storing long-lived AWS Access Key IDs and Secret Access Keys is a significant security risk. The application will implement a more secure approach using AWS Security Token Service (STS) and IAM Roles.

**Flow:**
1.  **User Configuration in AWS:** Each user will be instructed to create a dedicated IAM Role in their AWS account. This role will have:
    *   A trust policy allowing your application's AWS account (or a specific IAM user/role within your account) to assume it.
    *   Permissions policies granting read-only access to AWS Cost Explorer (`ce:GetCostAndUsage`, `ce:GetDimensionValues`, etc.).
2.  **User Input in Application:** When a user registers or configures their AWS integration within the Cloud Cost Tracker, they will provide the ARN (Amazon Resource Name) of the IAM Role they created in their AWS account.
3.  **Temporary Credential Generation:**
    *   When a user logs into the Cloud Cost Tracker, or when a scheduled cost fetch is initiated for a user, the backend will use its *own* securely managed AWS credentials (e.g., an IAM Role attached to the EC2 instance running the application, or credentials from AWS Secrets Manager) to call AWS STS's `AssumeRole` API.
    *   The `AssumeRole` API call will use the ARN provided by the user.
    *   AWS STS will return temporary credentials (Access Key ID, Secret Access Key, and Session Token) with a limited lifespan (e.g., 15 minutes to 1 hour).
4.  **AWS API Calls:** The `CostExplorerService` will use these *temporary, user-specific* AWS credentials to make calls to the AWS Cost Explorer API.
5.  **Data Isolation:** All fetched cost data will be explicitly linked to the user who owns it, ensuring that users can only view their own cloud costs.

## 4. Core Cloud Cost Tracking Functionality

### 4.1 Data Model (`CloudCost`)
The `CloudCost` entity will store aggregated or detailed cost information.
*   `id` (Primary Key)
*   `serviceName` (e.g., "Amazon EC2", "Amazon S3")
*   `cost` (Numeric value)
*   `usageType` (e.g., "DataTransfer-Out", "BoxUsage:t2.micro") - *New field for more granularity*
*   `region` (e.g., "us-east-1") - *New field for more granularity*
*   `startDate` (Date of cost period start) - *New field*
*   `endDate` (Date of cost period end) - *New field*
*   `userId` (Foreign Key to User entity, for data isolation) - *New field*

### 4.2 API Endpoints (`CloudCostController`)
The existing CRUD endpoints will be enhanced and secured:
*   `GET /api/cloud-costs`: Retrieve all cloud costs for the authenticated user.
*   `POST /api/cloud-costs`: Manually add a cloud cost record (primarily for testing or edge cases, automated fetching is preferred).
*   `PUT /api/cloud-costs/{id}`: Update an existing cloud cost record (only if owned by the authenticated user).
*   `DELETE /api/cloud-costs/{id}`: Delete a cloud cost record (only if owned by the authenticated user).
*   `GET /api/cloud-costs/summary`: (New) Retrieve aggregated cost summaries (e.g., total cost per service, per region).

## 5. AWS Cost Explorer Integration

### 5.1 `CostExplorerService` Implementation
This service will be responsible for interacting with the AWS Cost Explorer API.

**Key AWS Cost Explorer APIs to use:**
*   `GetCostAndUsage`: This is the primary API for fetching detailed cost and usage data.
    *   **Parameters:**
        *   `TimePeriod`: Define the start and end dates for the cost data.
        *   `Granularity`: `DAILY`, `MONTHLY`, or `HOURLY`.
        *   `Metrics`: `UnblendedCost`, `BlendedCost`, `UsageQuantity`.
        *   `GroupBy`: Dimensions to group the costs by (e.g., `SERVICE`, `REGION`, `USAGE_TYPE`, `LINKED_ACCOUNT`, `TAG`).
        *   `Filter`: To narrow down results (e.g., by service, tag, or linked account).
*   `GetDimensionValues`: To retrieve available values for specific dimensions (e.g., all service names, all regions). This can be used to populate dropdowns in a UI.

### 5.2 Data Fetching and Mapping
1.  **Scheduled Fetching:** A scheduled task (e.g., using Spring's `@Scheduled` annotation) will periodically (e.g., daily or weekly) fetch cost data for all registered users.
2.  **User-Specific Credentials:** For each user, the `CostExplorerService` will obtain temporary AWS credentials via STS (as described in Section 3.3).
3.  **API Calls:** It will make `GetCostAndUsage` calls to AWS Cost Explorer, grouping data by `SERVICE`, `REGION`, and `USAGE_TYPE`.
4.  **Data Transformation:** The raw data from AWS Cost Explorer will be transformed and mapped to the `CloudCost` entity.
    *   Each entry from `GetCostAndUsage` will correspond to one or more `CloudCost` records.
    *   Existing records for the same period and dimensions will be updated, and new records will be inserted.

## 6. Cloud Cost Optimization & Recommendations

This feature will analyze the fetched cost data and provide actionable recommendations to users to reduce their cloud spending.

### 6.1 Recommendation Categories (Examples)
*   **Idle Resource Identification:**
    *   **Logic:** Analyze usage data (if available from other AWS APIs like CloudWatch, or infer from zero usage costs for certain services). Identify resources that are provisioned but have minimal or no usage.
    *   **Recommendation:** "You have X idle EC2 instances/EBS volumes/RDS instances in region Y. Consider terminating them to save Z dollars/month."
*   **Right-Sizing Recommendations:**
    *   **Logic:** (Requires more advanced integration with CloudWatch metrics for CPU/memory utilization). Identify instances or services that are over-provisioned for their actual usage.
    *   **Recommendation:** "Your EC2 instance `i-xxxxxxxx` is consistently underutilized. Consider downgrading from `m5.large` to `t3.medium` to save X dollars/month."
*   **Reserved Instances (RIs) / Savings Plans (SPs) Opportunities:**
    *   **Logic:** Analyze consistent on-demand usage patterns for EC2, Fargate, Lambda, etc.
    *   **Recommendation:** "Based on your consistent EC2 usage, purchasing a 1-year Reserved Instance for `m5.large` in `us-east-1` could save you X%."
*   **Data Transfer Cost Analysis:**
    *   **Logic:** Identify high data transfer out costs.
    *   **Recommendation:** "Your data transfer out costs are significant. Review your architecture for opportunities to use CDN, private links, or optimize data egress."
*   **Unused/Old Snapshots/AMIs:**
    *   **Logic:** (Requires integration with EC2/EBS APIs). Identify old, unused snapshots or AMIs.
    *   **Recommendation:** "You have several old EBS snapshots/AMIs that are no longer associated with active resources. Consider deleting them to reduce storage costs."

### 6.2 Implementation Approach
1.  **New Service:** A `CostOptimizationService` will be created.
2.  **Analysis Logic:** This service will contain methods to analyze the `CloudCost` data stored in the database. For more advanced recommendations (e.g., right-sizing), it might need to integrate with other AWS APIs (e.g., CloudWatch, EC2) using the same STS temporary credential mechanism.
3.  **Recommendation Storage:** Recommendations could be stored in a new database table (e.g., `Recommendation` entity with fields like `userId`, `type`, `description`, `potentialSavings`, `status`).
4.  **API Endpoint:** A new API endpoint (e.g., `GET /api/cloud-costs/recommendations`) will expose these recommendations to the front-end.

## 7. Application Architecture & Code Flow

### 7.1 High-Level Architecture
```
+-------------------+       +-------------------+       +-------------------+
|   User Frontend   | <---> | Cloud Cost Tracker| <---> |    AWS Services   |
| (Web/Mobile App)  |       |   (Spring Boot)   |       | (Cost Explorer,   |
+-------------------+       +-------------------+       | STS, IAM, etc.)   |
          ^                           |                   +-------------------+
          |                           |
          |                           v
          |                   +-------------------+
          +-----------------> |    MySQL Database |
                              | (CloudCost, User, |
                              |   Recommendation) |
                              +-------------------+
```

### 7.2 Detailed Code Flow

1.  **User Login:**
    *   User sends credentials to `AuthController` (new controller).
    *   `AuthController` uses `Spring Security` to authenticate.
    *   Upon successful authentication, a JWT/session is returned.

2.  **Fetching User-Specific Cloud Costs (API Request):**
    *   Authenticated user makes a `GET /api/cloud-costs` request.
    *   `CloudCostController` receives the request.
    *   `CloudCostService` is called to retrieve costs for the current authenticated `userId`.
    *   `CloudCostRepository` fetches data from the MySQL database.
    *   Data is returned through DTOs to the `CloudCostController` and then to the frontend.

3.  **Scheduled AWS Cost Data Ingestion:**
    *   A `@Scheduled` method in `CloudCostService` (or a dedicated `CostIngestionService`) triggers the process.
    *   For each active user:
        *   The service retrieves the user's configured AWS IAM Role ARN.
        *   It calls `AWSCredentialService` (new service) to assume the role via STS and get temporary credentials.
        *   `CostExplorerService` is invoked with these temporary credentials.
        *   `CostExplorerService` makes `GetCostAndUsage` calls to AWS.
        *   Raw AWS data is processed and mapped to `CloudCost` entities.
        *   `CloudCostRepository` saves/updates these entities in the MySQL database.

4.  **Generating Recommendations:**
    *   A separate `@Scheduled` method or an on-demand API call triggers `CostOptimizationService`.
    *   `CostOptimizationService` queries `CloudCostRepository` for relevant cost data.
    *   It applies its analysis logic (e.g., identifying idle resources, usage patterns).
    *   New `Recommendation` entities are created/updated in the database via `RecommendationRepository` (new repository).

5.  **Displaying Recommendations:**
    *   Authenticated user makes a `GET /api/cloud-costs/recommendations` request.
    *   `CostOptimizationController` (new controller) receives the request.
    *   `CostOptimizationService` retrieves recommendations for the current `userId`.
    *   Data is returned to the frontend.

## 8. Technology Stack

*   **Backend:** Java 21, Spring Boot
*   **Web Framework:** Spring Web (RESTful API)
*   **Data Persistence:** Spring Data JPA, Hibernate
*   **Database:** MySQL
*   **Build Tool:** Maven
*   **Cloud Integration:** AWS SDK for Java (specifically `aws-java-sdk-costexplorer`, `aws-java-sdk-sts`)
*   **Security:** Spring Security
*   **Testing:** JUnit, Mockito, Spring Boot Test

## 9. Future Considerations

*   **Multi-Cloud Support:** Extend to other cloud providers (Azure, GCP).
*   **Alerting:** Set up notifications for cost anomalies or budget overruns.
*   **Detailed Reporting:** More advanced dashboards and reporting features.
*   **Tagging Integration:** Deeper analysis based on AWS resource tags.
*   **UI Development:** A dedicated frontend application (e.g., React, Angular, Vue.js) to consume the API.

## 10. Implementation Roadmap / Next Steps

This section outlines the remaining tasks and potential future enhancements, broken down into actionable steps.

### 10.1 Core Backend Development (Completed/In Progress)

*   **Authentication & Authorization (Spring Security)**
    *   User Entity, Repository, DTOs, Services (UserRegistration, UserDetailsServiceImpl)
    *   Security Configuration (BCryptPasswordEncoder, basic security chain)
    *   Auth Controller (User Registration endpoint)
*   **Cloud Cost Tracking (CRUD & User Scoping)**
    *   CloudCost Model (with new fields: usageType, region, startDate, endDate, userId)
    *   CloudCostRequestDTO & CloudCostResponseDTO (updated)
    *   CloudCostService (user-scoped CRUD, getCurrentAuthenticatedUser helper)
    *   CloudCostRepository (findByUser method)
    *   CloudCostController (updated endpoints)
*   **AWS Cost Explorer Integration (STS & Scheduled Fetching)**
    *   Add `aws-java-sdk-sts` dependency
    *   AWSCredentialService (AssumeRole logic)
    *   CostExplorerService (GetCostAndUsage, temporary credentials)
    *   CloudCostService (scheduled `fetchAndSaveAwsCosts`)
    *   EnableScheduling in `TrackerApplication`
*   **Cloud Cost Optimization & Recommendations (Basic)**
    *   Recommendation Model, Repository
    *   CostOptimizationService (basic `generateRecommendations`)
    *   RecommendationResponseDTO
    *   CostOptimizationController (recommendations endpoint)
*   **Cloud Cost Summary**
    *   CloudCostService (`getCloudCostSummary` method)
    *   CloudCostController (summary endpoint)

### 10.2 Remaining Backend Tasks

*   **Refine AWS Cost Data Ingestion**
    *   Implement logic in `fetchAndSaveAwsCosts` to handle existing records (update instead of always saving new ones) to prevent duplicates and ensure data integrity.
    *   Consider more robust error handling and logging for AWS API calls.
*   **Enhance Cost Optimization Logic**
    *   Implement more sophisticated recommendation categories as outlined in Section 6.1 (Idle Resource Identification, Right-Sizing, RI/SP Opportunities, Data Transfer Analysis, Unused Snapshots/AMIs).
    *   This will likely require integrating with additional AWS APIs (e.g., CloudWatch for metrics, EC2 for resource details) using the STS temporary credentials.
    *   Develop algorithms for analyzing usage patterns and identifying optimization opportunities.
*   **Implement User Login Endpoint**
    *   While Spring Security handles the authentication, a dedicated `/api/auth/login` endpoint might be needed for explicit login requests, potentially returning a JWT for stateless authentication.
*   **Error Handling & Validation**
    *   Implement global exception handling for consistent error responses.
    *   Add more comprehensive input validation for all DTOs using Spring's `@Valid` and validation annotations.
*   **Pagination & Filtering for API Endpoints**
    *   Enhance `getAllCloudCosts` and `getRecommendationsForUser` to support pagination and filtering parameters for large datasets.
*   **Testing**
    *   Write comprehensive unit tests for all service and utility classes.
    *   Write integration tests for controllers and repository interactions.
    *   Write end-to-end tests for critical flows (e.g., user registration, cost fetching).

### 10.3 Future Enhancements (Beyond Initial Scope)

*   **Multi-Cloud Support**: Extend the application to integrate with other cloud providers like Azure and Google Cloud Platform, requiring new service integrations and data models.
*   **Alerting and Notifications**: Implement features to send alerts for budget overruns, cost anomalies, or new recommendations via email, SMS, or other channels.
*   **Advanced Reporting & Dashboards**: Develop more complex data aggregation and visualization capabilities, potentially requiring a dedicated reporting module or integration with a BI tool.
*   **AWS Tagging Integration**: Allow users to analyze costs based on their AWS resource tags, providing more granular insights.
*   **Frontend Development**: Build a dedicated user interface (web or mobile) to consume the backend APIs, providing a rich user experience for managing and visualizing cloud costs and recommendations.
*   **Containerization & Deployment**: Prepare the application for deployment using Docker and Kubernetes for scalability and ease of management.
*   **Monitoring & Logging**: Implement robust monitoring and logging solutions (e.g., Prometheus, Grafana, ELK stack) for production environments.
