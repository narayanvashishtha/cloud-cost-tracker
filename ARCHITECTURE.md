# Architecture Document: Cloud Cost Tracker

## 1. Introduction

This document provides a comprehensive technical explanation of the Cloud Cost Tracker application. It details the architectural design, the role of each component, the code implementation, and the flow of various operations within the system. This document serves as a deep dive into the technical aspects, complementing the Product Requirements Document (PRD.md) which outlines the project's scope, goals, and product description.

## 2. Overall Architecture

The Cloud Cost Tracker is built as a Spring Boot microservice, designed to be the backend for a potential frontend application (web or mobile). It follows a layered architecture, promoting separation of concerns and maintainability.

```
+-------------------+       +-------------------+       +-------------------+
|   User Frontend   | <---> | Cloud Cost Tracker| <---> |    AWS Services   |
| (Web/Mobile App)  |       |   (Spring Boot)   |       | (Cost Explorer,   |
+-------------------+       |   (Backend API)   |       | STS, IAM, etc.)   |
          ^                   +-------------------+       +-------------------+
          |                           |                   
          |                           |
          |                           v
          |                   +-------------------+
          +-----------------> |    MySQL Database |
                              | (CloudCost, User, |
                              |   Recommendation) |
                              +-------------------+
```

**Layers:**

*   **Presentation Layer (Controllers):** Exposes RESTful APIs for external clients (e.g., a frontend application) to interact with the system. It handles HTTP requests, validates input, and returns appropriate HTTP responses.
*   **Application/Service Layer (Services):** Contains the core business logic of the application. It orchestrates operations, interacts with repositories, and integrates with external services (like AWS). This layer is responsible for data validation, business rules, and transaction management.
*   **Data Access Layer (Repositories):** Provides an abstraction over the persistence mechanism. It defines methods for CRUD (Create, Read, Update, Delete) operations on the application's data models, interacting directly with the database.
*   **Domain Layer (Models):** Represents the core entities and their relationships within the application's business domain. These are typically plain old Java objects (POJOs) mapped to database tables.
*   **External Integrations:** Handles communication with third-party services, primarily AWS (Cost Explorer and STS) in this application.

## 3. Core Components and Their Roles

### 3.1. Spring Boot Application (`TrackerApplication.java`)

*   **`@SpringBootApplication`**: This is a convenience annotation that adds:
    *   `@Configuration`: Tags the class as a source of bean definitions for the application context.
    *   `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
    *   `@ComponentScan`: Tells Spring to look for other components, configurations, and services in the `com.example.tracker` package, allowing it to discover controllers, services, and repositories.
*   **`@EnableScheduling`**: This annotation, added to the main application class, enables Spring's scheduled task execution capability. It allows methods annotated with `@Scheduled` in service classes (like `CloudCostService` and `CostOptimizationService`) to run automatically at specified intervals.
*   **`main` method**: The entry point of the application, which uses `SpringApplication.run()` to bootstrap the Spring Boot application.

### 3.2. Models (`src/main/java/com/example/tracker/model` package)

These classes represent the entities stored in the database and define their structure and relationships.

*   **`User.java`**
    *   **Purpose**: Represents a user of the application. It stores authentication credentials and the AWS IAM Role ARN for secure AWS integration.
    *   **Annotations**:
        *   `@Entity`: Marks this class as a JPA entity, meaning it maps to a database table.
        *   `@Table(name = "users")`: Specifies the name of the database table for this entity.
        *   `@Id`: Marks the `id` field as the primary key.
        *   `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Configures the primary key to be auto-incremented by the database.
        *   `@Column(unique = true, nullable = false)`: Ensures `username` is unique and not null.
        *   `@Column(nullable = false)`: Ensures `password` is not null.
        *   `@Column(nullable = true)`: `awsIamRoleArn` can be null if a user hasn't configured it yet.
    *   **Fields**:
        *   `id`: Unique identifier for the user.
        *   `username`: User's login username.
        *   `password`: Hashed password for authentication.
        *   `awsIamRoleArn`: The Amazon Resource Name (ARN) of the IAM Role in the user's AWS account that this application will assume to fetch cost data. This is crucial for the secure STS integration.

*   **`CloudCost.java`**
    *   **Purpose**: Stores individual cloud cost records, now enhanced with more granular details and linked to a specific user.
    *   **Annotations**:
        *   `@Entity`, `@Table(name = "cloud_costs")`, `@Id`, `@GeneratedValue`: Similar to `User.java`, for JPA mapping.
        *   `@Column(name = "serviceName")`: Maps the field to a specific column name.
        *   `@ManyToOne`: Defines a many-to-one relationship with the `User` entity (many `CloudCost` records can belong to one `User`).
        *   `@JoinColumn(name = "user_id", nullable = false)`: Specifies the foreign key column (`user_id`) in the `cloud_costs` table that links to the `users` table. `nullable = false` ensures every cost record is associated with a user.
    *   **Fields**:
        *   `id`: Unique identifier for the cost record.
        *   `serviceName`: The name of the cloud service (e.g., "Amazon EC2", "Amazon S3").
        *   `cost`: The monetary cost incurred.
        *   `usageType`: (New) More specific detail about the usage (e.g., "DataTransfer-Out", "BoxUsage:t2.micro").
        *   `region`: (New) The AWS region where the cost was incurred (e.g., "us-east-1").
        *   `startDate`: (New) The start date of the cost period.
        *   `endDate`: (New) The end date of the cost period.
        *   `user`: A reference to the `User` entity who owns this cost record.

*   **`Recommendation.java`**
    *   **Purpose**: Stores cost optimization recommendations generated by the application, linked to a specific user.
    *   **Annotations**:
        *   `@Entity`, `@Table(name = "recommendations")`, `@Id`, `@GeneratedValue`: Standard JPA entity mapping.
        *   `@ManyToOne`, `@JoinColumn(name = "user_id", nullable = false)`: Links a recommendation to a specific user.
        *   `@Column(nullable = false)`: Ensures `type` and `description` are not null.
        *   `@Column(nullable = false, length = 1000)`: Sets a maximum length for the description.
    *   **Fields**:
        *   `id`: Unique identifier for the recommendation.
        *   `user`: A reference to the `User` entity for whom the recommendation is generated.
        *   `type`: The category of the recommendation (e.g., "Idle Resource", "High Cost Alert").
        *   `description`: A detailed explanation of the recommendation.
        *   `potentialSavings`: An optional estimated monetary saving.
        *   `dateGenerated`: The date when the recommendation was generated.

### 3.3. Repositories (`src/main/java/com/example/tracker/repository` package)

These interfaces extend Spring Data JPA's `JpaRepository`, providing powerful CRUD operations and query methods automatically, reducing boilerplate code.

*   **`UserRepository.java`**
    *   Extends `JpaRepository<User, Long>`: Provides standard CRUD operations for the `User` entity, with `Long` as the type of its primary key.
    *   `Optional<User> findByUsername(String username)`: A custom query method. Spring Data JPA automatically generates the implementation for this method based on its name, allowing retrieval of a `User` by their username.

*   **`CloudCostRepository.java`**
    *   Extends `JpaRepository<CloudCost, Long>`: Provides standard CRUD operations for `CloudCost` entities.
    *   `List<CloudCost> findByUser(User user)`: A custom query method to retrieve all `CloudCost` records associated with a specific `User`.

*   **`RecommendationRepository.java`**
    *   Extends `JpaRepository<Recommendation, Long>`: Provides standard CRUD operations for `Recommendation` entities.
    *   `List<Recommendation> findByUser(User user)`: A custom query method to retrieve all `Recommendation` records for a given `User`.

### 3.4. DTOs (`src/main/java/com/example/tracker/dto` package)

Data Transfer Objects (DTOs) are used to define the structure of data exchanged between the client (frontend) and the server (backend API). They help decouple the internal domain models from the external API contract, providing flexibility and preventing direct exposure of database entities.

*   **`UserRegistrationDTO.java`**
    *   **Purpose**: Used as the request body for user registration. It contains fields necessary for creating a new user account.
    *   **Fields**: `username`, `password`, `awsIamRoleArn`.

*   **`CloudCostRequestDTO.java`**
    *   **Purpose**: Used as the request body for creating or updating `CloudCost` records. It includes all the relevant fields for a cost entry.
    *   **Fields**: `serviceName`, `cost`, `usageType`, `region`, `startDate`, `endDate`.

*   **`CloudCostResponseDTO.java`**
    *   **Purpose**: Used as the response body when returning `CloudCost` records to the client. It mirrors the `CloudCost` model but can be tailored (e.g., omitting sensitive internal fields or adding derived fields).
    *   **Fields**: `id`, `serviceName`, `cost`, `usageType`, `region`, `startDate`, `endDate`, `userId`.
    *   **Constructor**: Includes a constructor that takes a `CloudCost` entity and maps its fields to the DTO, including extracting the `userId` from the associated `User` object.

*   **`RecommendationResponseDTO.java`**
    *   **Purpose**: Used as the response body when returning `Recommendation` records to the client.
    *   **Fields**: `id`, `type`, `description`, `potentialSavings`, `dateGenerated`, `userId`.
    *   **Constructor**: Maps fields from a `Recommendation` entity to the DTO.

### 3.5. Services (`src/main/java/com/example/tracker/service` package)

Services encapsulate the business logic and orchestrate operations across different components. They are typically annotated with `@Service` to be managed by Spring's dependency injection container.

*   **`UserService.java`**
    *   **Purpose**: Handles user-related business logic, specifically user registration.
    *   **Dependencies**: `UserRepository`, `PasswordEncoder` (for hashing passwords).
    *   **Methods**:
        *   `registerNewUser(UserRegistrationDTO registrationDTO)`: Takes a DTO, checks if the username already exists, encodes the password using `BCryptPasswordEncoder`, creates a new `User` entity, and saves it via `userRepository`.

*   **`UserDetailsServiceImpl.java`**
    *   **Purpose**: Integrates with Spring Security to load user-specific data during the authentication process.
    *   **Dependencies**: `UserRepository`.
    *   **Methods**:
        *   `loadUserByUsername(String username)`: Implements Spring Security's `UserDetailsService` interface. It fetches a `User` from the database using `userRepository.findByUsername()`. If found, it constructs and returns a `org.springframework.security.core.userdetails.User` object (Spring Security's internal user representation) containing the username, password, and authorities (currently empty `ArrayList` as roles are not yet implemented).

*   **`AWSCredentialService.java`**
    *   **Purpose**: Provides a secure mechanism to obtain temporary AWS credentials by assuming an IAM Role in the user's AWS account. This is critical for security, as it avoids storing long-lived user AWS keys.
    *   **Dependencies**: AWS SDK for STS (`AWSSecurityTokenService`).
    *   **Methods**:
        *   `assumeRoleAndGetCredentials(String roleArn, String roleSessionName)`: This method performs the core STS operation.
            1.  It builds an `AWSSecurityTokenService` client.
            2.  Creates an `AssumeRoleRequest` with the provided `roleArn` (from the user's `User` entity) and a `roleSessionName` (a unique identifier for the session).
            3.  Calls `stsClient.assumeRole(request)` to get temporary credentials from AWS.
            4.  Extracts the `AccessKeyId`, `SecretAccessKey`, and `SessionToken` from the `AssumeRoleResult`.
            5.  Returns an `AWSStaticCredentialsProvider` initialized with these temporary credentials. This provider can then be used by other AWS service clients (like Cost Explorer) to make authenticated calls.

*   **`CostExplorerService.java`**
    *   **Purpose**: Interacts with the AWS Cost Explorer API to fetch detailed cost and usage data for a given user.
    *   **Dependencies**: `AWSCredentialService` (to get temporary credentials), AWS SDK for Cost Explorer (`AWSCostExplorer`).
    *   **Methods**:
        *   `getCostAndUsage(User user, LocalDate startDate, LocalDate endDate)`: This method fetches cost data.
            1.  It first checks if the `user` has an `awsIamRoleArn` configured.
            2.  It calls `awsCredentialService.assumeRoleAndGetCredentials()` to obtain temporary AWS credentials for the specific user.
            3.  It builds an `AWSCostExplorer` client using these temporary credentials.
            4.  Constructs a `GetCostAndUsageRequest`:
                *   Sets `TimePeriod` using the provided `startDate` and `endDate`.
                *   Sets `Granularity` to `DAILY`.
                *   Requests `UnblendedCost` as the metric.
                *   Groups the results by `SERVICE`, `REGION`, and `USAGE_TYPE` dimensions to get granular data.
            5.  Executes the `ceClient.getCostAndUsage(request)` call.
            6.  Parses the `ResultByTime` and `Group` objects from the AWS response.
            7.  Maps the extracted data (service name, region, usage type, cost, date) into `CloudCost` entities.
            8.  Sets the `User` object on each `CloudCost` entity to maintain ownership.
            9.  Returns a `List<CloudCost>`.

*   **`CloudCostService.java`**
    *   **Purpose**: Manages the business logic for `CloudCost` entities, including CRUD operations and the scheduled fetching of AWS cost data. It ensures that all operations are performed in the context of the currently authenticated user.
    *   **Dependencies**: `CloudCostRepository`, `UserRepository`, `CostExplorerService`.
    *   **Methods**:
        *   `getCurrentAuthenticatedUser()`: A helper method that retrieves the username of the currently authenticated user from Spring Security's `SecurityContextHolder` and then fetches the corresponding `User` entity from the `userRepository`. This ensures all operations are user-scoped.
        *   `saveCloudCost(CloudCostRequestDTO requestDTO)`: Saves a new `CloudCost` record. It validates input, maps the DTO to a `CloudCost` entity, sets the `currentUser` as the owner, and saves it via `cloudCostRepository`. Returns a `CloudCostResponseDTO`.
        *   `getAllCloudCosts()`: Retrieves all `CloudCost` records for the `currentUser` using `cloudCostRepository.findByUser()`. Maps the results to `CloudCostResponseDTO`s.
        *   `updateCloudCost(Long id, CloudCostRequestDTO requestDTO)`: Updates an existing `CloudCost` record. It first verifies that the record exists and that the `currentUser` is the owner. Then, it updates the fields and saves the entity. Returns a `CloudCostResponseDTO`.
        *   `deleteCloudCost(Long id)`: Deletes a `CloudCost` record. It verifies ownership before deleting.
        *   `@Scheduled(cron = "0 0 1 * * ?") public void fetchAndSaveAwsCosts()`: This is a crucial method for automated data ingestion.
            1.  Annotated with `@Scheduled`, it runs daily at 1 AM.
            2.  It fetches all registered `User` entities from `userRepository`.
            3.  For each user, it checks if an `awsIamRoleArn` is configured.
            4.  If so, it calls `costExplorerService.getCostAndUsage()` to fetch AWS cost data for the previous day using the user's temporary credentials.
            5.  It then iterates through the fetched `CloudCost` entities and saves them to the `cloudCostRepository`. (Note: The current implementation simply saves; a more robust solution would check for existing records to avoid duplicates or update them).
        *   `getCloudCostSummary()`: Aggregates cloud costs for the current user. It fetches all costs for the user and then uses Java Streams (`Collectors.groupingBy` and `Collectors.summingDouble`) to sum costs by `serviceName`, returning a `Map<String, Double>`.

*   **`CostOptimizationService.java`**
    *   **Purpose**: Generates and manages cost optimization recommendations for users.
    *   **Dependencies**: `CloudCostRepository`, `RecommendationRepository`, `UserRepository`.
    *   **Methods**:
        *   `@Scheduled(cron = "0 0 2 * * ?") public void generateRecommendations()`: Runs daily at 2 AM.
            1.  Fetches all users.
            2.  For each user, it retrieves their `CloudCost` records.
            3.  **Current Logic (Basic Example)**: It calculates the total cost for the user. If the total cost exceeds an arbitrary threshold (e.g., $1000), it generates a generic "High Cost Alert" recommendation and saves it.
            4.  **Future Expansion**: This method is designed to be expanded with more sophisticated logic for identifying idle resources, right-sizing opportunities, reserved instance recommendations, etc. (as outlined in `PRD.md`). This would involve more complex analysis of `CloudCost` data and potentially integration with other AWS APIs.
        *   `getRecommendationsForUser(User user)`: Retrieves all `Recommendation` records for a given `User` from `recommendationRepository`.

### 3.6. Controllers (`src/main/java/com/example/tracker/controller` package)

Controllers are the entry points for the RESTful API. They handle incoming HTTP requests, delegate to service methods, and return HTTP responses. They are annotated with `@RestController` and `@RequestMapping`.

*   **`AuthController.java`**
    *   **Purpose**: Handles user authentication-related endpoints, specifically user registration.
    *   **Dependencies**: `UserService`.
    *   **Endpoints**:
        *   `POST /api/auth/register`: Takes a `UserRegistrationDTO` as `@RequestBody`. Calls `userService.registerNewUser()`. Returns `201 Created` on success or `400 Bad Request` for validation errors (e.g., username already exists) or `500 Internal Server Error` for unexpected issues.

*   **`CloudCostController.java`**
    *   **Purpose**: Exposes REST endpoints for managing `CloudCost` records.
    *   **Dependencies**: `CloudCostService`.
    *   **Endpoints**:
        *   `GET /api/cloud-costs`: Retrieves all cloud costs for the authenticated user. Calls `cloudCostService.getAllCloudCosts()`. Returns `200 OK` with a list of `CloudCostResponseDTO`s.
        *   `POST /api/cloud-costs`: Saves a new cloud cost. Takes `CloudCostRequestDTO`. Calls `cloudCostService.saveCloudCost()`. Returns `201 Created` or error responses.
        *   `PUT /api/cloud-costs/{id}`: Updates an existing cloud cost. Takes `id` from `@PathVariable` and `CloudCostRequestDTO` from `@RequestBody`. Calls `cloudCostService.updateCloudCost()`. Returns `200 OK` or error responses.
        *   `DELETE /api/cloud-costs/{id}`: Deletes a cloud cost. Takes `id` from `@PathVariable`. Calls `cloudCostService.deleteCloudCost()`. Returns `204 No Content` on success or `500 Internal Server Error`.
        *   `GET /api/cloud-costs/summary`: (New) Retrieves a summary of cloud costs, grouped by service. Calls `cloudCostService.getCloudCostSummary()`. Returns `200 OK` with a `Map<String, Double>`.

*   **`CostOptimizationController.java`**
    *   **Purpose**: Exposes REST endpoints for retrieving cost optimization recommendations.
    *   **Dependencies**: `CostOptimizationService`, `UserRepository` (to get the current user).
    *   **Endpoints**:
        *   `GET /api/recommendations`: Retrieves all recommendations for the authenticated user. Calls `costOptimizationService.getRecommendationsForUser()`. Returns `200 OK` with a list of `RecommendationResponseDTO`s.

### 3.7. Configuration (`src/main/java/com/example/tracker/config` package)

Configuration classes define beans and settings for the Spring application context.

*   **`SecurityConfig.java`**
    *   **Purpose**: Configures Spring Security for web application security, including authentication and authorization rules.
    *   **Annotations**:
        *   `@Configuration`: Indicates that this class contains Spring bean definitions.
        *   `@EnableWebSecurity`: Enables Spring Security's web security features.
    *   **Beans**:
        *   `securityFilterChain(HttpSecurity http)`: This bean defines the security filter chain. It configures:
            *   `csrf(csrf -> csrf.disable())`: Disables CSRF protection. **Note**: For production web applications, CSRF should generally be enabled and handled appropriately (e.g., with tokens in frontend requests). It's disabled here for simplicity in a backend-only API context.
            *   `authorizeHttpRequests(authorize -> authorize ...)`: Defines authorization rules:
                *   `.requestMatchers("/api/auth/**").permitAll()`: Allows unauthenticated access to all endpoints under `/api/auth` (e.g., `/api/auth/register`).
                *   `.anyRequest().authenticated()`: Requires all other requests to be authenticated.
        *   `passwordEncoder()`: Defines a `PasswordEncoder` bean.
            *   `return new BCryptPasswordEncoder()`: Uses BCrypt for strong password hashing. This is crucial for securely storing user passwords.

## 4. Detailed Flow of Operations

This section describes the step-by-step execution flow for key operations within the application.

### 4.1. User Registration

1.  **Client Request**: A frontend application sends an HTTP `POST` request to `/api/auth/register` with a JSON body containing `username`, `password`, and optionally `awsIamRoleArn`.
2.  **`AuthController`**: Receives the request, maps the JSON to a `UserRegistrationDTO` object.
3.  **`AuthController` to `UserService`**: Calls `userService.registerNewUser(registrationDTO)`.
4.  **`UserService`**: 
    *   Checks `userRepository.findByUsername()` to ensure the username is not already taken.
    *   If unique, it encodes the `password` from the DTO using `passwordEncoder.encode()`.
    *   Creates a new `User` entity, setting its `username`, encoded `password`, and `awsIamRoleArn`.
5.  **`UserService` to `UserRepository`**: Calls `userRepository.save(user)` to persist the new user entity to the `users` table in the MySQL database.
6.  **`UserService` Response**: Returns the saved `User` entity.
7.  **`AuthController` Response**: Returns a `ResponseEntity` with `HttpStatus.CREATED` (201) and a success message, or `HttpStatus.BAD_REQUEST` (400) if the username exists or other validation fails.

### 4.2. User Login & Authentication

1.  **Client Request**: A frontend application sends authentication credentials (username/password) to Spring Security's default login endpoint (or a custom one if configured, though not explicitly defined in `AuthController` for login yet).
2.  **Spring Security Filter Chain**: Intercepts the request.
3.  **`UserDetailsServiceImpl`**: Spring Security's `DaoAuthenticationProvider` (default) calls `userDetailsService.loadUserByUsername(username)`.
4.  **`UserDetailsServiceImpl` to `UserRepository`**: Calls `userRepository.findByUsername(username)` to retrieve the user's details from the database.
5.  **`UserDetailsServiceImpl` Response**: If the user is found, it returns a `org.springframework.security.core.userdetails.User` object containing the username, password (from DB), and authorities. If not found, it throws `UsernameNotFoundException`.
6.  **Spring Security Password Matching**: Compares the provided password with the stored (hashed) password using the `BCryptPasswordEncoder` configured in `SecurityConfig`.
7.  **Authentication Success/Failure**: If credentials match, Spring Security creates an `Authentication` object and stores it in the `SecurityContextHolder`. If not, authentication fails.
8.  **Client Response**: Spring Security handles the response (e.g., redirect on success, error on failure). For a REST API, this typically involves returning a success status and potentially a JWT (if JWT authentication is implemented, which is a common next step for stateless APIs).

### 4.3. Fetching User-Specific Cloud Costs

1.  **Client Request**: An authenticated frontend application sends an HTTP `GET` request to `/api/cloud-costs`.
2.  **Spring Security Authorization**: The request passes through Spring Security's filter chain. Since `anyRequest().authenticated()` is configured, Spring Security verifies that the request is from an authenticated user (i.e., an `Authentication` object exists in `SecurityContextHolder`).
3.  **`CloudCostController`**: Receives the request.
4.  **`CloudCostController` to `CloudCostService`**: Calls `cloudCostService.getAllCloudCosts()`.
5.  **`CloudCostService` (`getCurrentAuthenticatedUser()` call)**:
    *   Retrieves the username of the currently authenticated user from `SecurityContextHolder.getContext().getAuthentication().getName()`.
    *   Calls `userRepository.findByUsername(username)` to get the full `User` entity from the database.
6.  **`CloudCostService` to `CloudCostRepository`**: Calls `cloudCostRepository.findByUser(currentUser)` to fetch only the `CloudCost` records associated with the authenticated user.
7.  **`CloudCostService` Response**: Maps the retrieved `CloudCost` entities to `CloudCostResponseDTO`s and returns a `List<CloudCostResponseDTO>`.
8.  **`CloudCostController` Response**: Returns a `ResponseEntity` with `HttpStatus.OK` (200) and the list of DTOs to the client.

### 4.4. Scheduled AWS Cost Data Ingestion

1.  **Scheduled Trigger**: At 1 AM daily, the `@Scheduled` method `fetchAndSaveAwsCosts()` in `CloudCostService` is automatically invoked by Spring's scheduler.
2.  **`CloudCostService` to `UserRepository`**: Calls `userRepository.findAll()` to retrieve all registered users in the system.
3.  **Iterate Users**: The method loops through each `User`.
4.  **Check AWS ARN**: For each user, it checks if `user.getAwsIamRoleArn()` is configured (not null or empty).
5.  **`CloudCostService` to `CostExplorerService`**: If an ARN exists, it calls `costExplorerService.getCostAndUsage(user, startDate, endDate)`.
6.  **`CostExplorerService` to `AWSCredentialService`**: `CostExplorerService` first calls `awsCredentialService.assumeRoleAndGetCredentials(user.getAwsIamRoleArn(), ...)`.
7.  **`AWSCredentialService` to AWS STS**: `AWSCredentialService` makes an `AssumeRole` API call to AWS Security Token Service (STS) using the application's own AWS credentials (which must be configured securely, e.g., via environment variables or IAM role for the EC2 instance running the app). STS returns temporary credentials (Access Key ID, Secret Access Key, Session Token) for the user's assumed role.
8.  **`CostExplorerService` to AWS Cost Explorer**: `CostExplorerService` uses these temporary credentials to build an `AWSCostExplorer` client and makes a `GetCostAndUsage` API call to AWS Cost Explorer, requesting daily cost data grouped by service, region, and usage type.
9.  **AWS Cost Explorer Response**: AWS Cost Explorer returns the raw cost data.
10. **`CostExplorerService` Data Mapping**: Parses the AWS response, extracts relevant cost details, and maps them into `CloudCost` entities, setting the `User` object on each `CloudCost` to link it to the owner.
11. **`CostExplorerService` Response**: Returns a `List<CloudCost>` to `CloudCostService`.
12. **`CloudCostService` to `CloudCostRepository`**: Iterates through the received `CloudCost` list and calls `cloudCostRepository.save(cost)` for each, persisting the data to the `cloud_costs` table in the MySQL database.
13. **Logging**: Prints messages to the console indicating success or errors for each user's cost fetching process.

### 4.5. Generating Cost Optimization Recommendations

1.  **Scheduled Trigger**: At 2 AM daily, the `@Scheduled` method `generateRecommendations()` in `CostOptimizationService` is automatically invoked.
2.  **`CostOptimizationService` to `UserRepository`**: Calls `userRepository.findAll()` to get all users.
3.  **Iterate Users**: The method loops through each `User`.
4.  **`CostOptimizationService` to `CloudCostRepository`**: For each user, it calls `cloudCostRepository.findByUser(user)` to retrieve their cloud cost data.
5.  **Analysis Logic (Current Basic Example)**: It calculates the `totalCost` for the user. If `totalCost` exceeds a predefined threshold (e.g., $1000), it proceeds to generate a recommendation.
6.  **Create `Recommendation`**: A new `Recommendation` entity is created with the user, a type ("High Cost Alert"), a description, and the current date.
7.  **`CostOptimizationService` to `RecommendationRepository`**: Calls `recommendationRepository.save(recommendation)` to store the generated recommendation in the `recommendations` table.
8.  **Future Expansion**: This is the point where more advanced analysis logic would be integrated to provide specific, actionable recommendations (e.g., identifying idle resources by checking for zero usage costs over a period, or suggesting right-sizing based on historical usage patterns if more detailed metrics are integrated).

## 5. Key Technologies and Dependencies

The project leverages the following key technologies and their corresponding Maven dependencies (as seen in `pom.xml`):

*   **Java 21**: The programming language.
*   **Spring Boot (3.4.5)**: The framework for building stand-alone, production-grade Spring applications.
    *   `spring-boot-starter-parent`: Manages dependency versions.
    *   `spring-boot-starter-web`: For building RESTful APIs.
    *   `spring-boot-starter-data-jpa`: For data persistence with JPA and Hibernate.
    *   `spring-boot-starter-security`: For authentication and authorization.
    *   `spring-boot-starter-test`: For writing unit and integration tests.
*   **MySQL Connector/J (8.0.31)**: JDBC driver for connecting to MySQL databases.
    *   `mysql-connector-j`
*   **AWS SDK for Java (1.12.250)**: For interacting with AWS services.
    *   `aws-java-sdk-costexplorer`: Specifically for fetching cost and usage data.
    *   `aws-java-sdk-sts`: Specifically for assuming IAM roles and obtaining temporary security credentials.
*   **Maven**: The build automation tool (`mvnw` wrapper is included).
*   **Hibernate**: JPA implementation used by Spring Data JPA for ORM (Object-Relational Mapping).
*   **BCrypt**: Used for secure password hashing (via `BCryptPasswordEncoder`).

## 6. Database Schema (Conceptual)

The application interacts with a MySQL database. Based on the JPA entities, the following tables are conceptually created and managed:

*   **`users` table**:
    *   `id` (BIGINT, Primary Key, Auto-increment)
    *   `username` (VARCHAR, Unique, Not Null)
    *   `password` (VARCHAR, Not Null)
    *   `aws_iam_role_arn` (VARCHAR, Nullable)

*   **`cloud_costs` table**:
    *   `id` (BIGINT, Primary Key, Auto-increment)
    *   `service_name` (VARCHAR)
    *   `cost` (DOUBLE)
    *   `usage_type` (VARCHAR)
    *   `region` (VARCHAR)
    *   `start_date` (DATE)
    *   `end_date` (DATE)
    *   `user_id` (BIGINT, Foreign Key to `users.id`, Not Null)

*   **`recommendations` table**:
    *   `id` (BIGINT, Primary Key, Auto-increment)
    *   `user_id` (BIGINT, Foreign Key to `users.id`, Not Null)
    *   `type` (VARCHAR, Not Null)
    *   `description` (VARCHAR, Not Null, max length 1000)
    *   `potential_savings` (DOUBLE, Nullable)
    *   `date_generated` (DATE)

## 7. Security Considerations

Security has been a primary consideration in the design, particularly concerning AWS credential handling and user authentication.

*   **Secure AWS Credential Handling (AWS STS AssumeRole)**:
    *   **Problem**: Directly storing long-lived AWS Access Key IDs and Secret Access Keys for each user is a severe security vulnerability. If the database is compromised, all user AWS accounts could be exposed.
    *   **Solution**: The application implements the AWS Security Token Service (STS) `AssumeRole` pattern. Users configure an IAM Role in their own AWS account with specific, limited permissions (e.g., read-only access to Cost Explorer) and a trust policy allowing the application's AWS account to assume it. The application then stores only the ARN of this role. When fetching data, the application's backend uses its own securely managed AWS credentials to *temporarily* assume the user's role via STS. This yields short-lived credentials that are used for API calls. This significantly reduces the risk of compromise.
*   **Password Hashing**: User passwords are never stored in plain text. `BCryptPasswordEncoder` is used to hash passwords before storing them in the database, making them resistant to brute-force attacks even if the database is breached.
*   **Spring Security**: Provides robust authentication and authorization mechanisms, ensuring that only authenticated users can access protected resources and that users can only access data they own.
*   **User-Scoped Data Access**: All data access operations (fetching, updating, deleting costs and recommendations) are explicitly scoped to the currently authenticated user, preventing users from accessing or modifying data belonging to others.
*   **CSRF Protection**: Currently disabled for simplicity in a backend API. For a production application with a browser-based frontend, CSRF protection should be enabled and properly handled (e.g., using CSRF tokens in requests).

This document provides a detailed overview of the Cloud Cost Tracker's architecture and implementation. It should serve as a comprehensive guide for understanding the codebase from a technical perspective.