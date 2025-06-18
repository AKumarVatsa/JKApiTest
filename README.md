# BookStore API Automation Framework

This project is an **API test automation framework** built using **Java**, **RestAssured**, **TestNG**, and **ExtentReports** to validate the core functionalities of the [BookStore API](https://bookstore.toolsqa.com/swagger/). The framework supports **CI/CD using GitHub Actions** and **generates test reports**.

## Tech Stack

| Tool/Library      | Purpose                                |
|-------------------|----------------------------------------|
| Java (17)         | Programming language                   |
| RestAssured       | API automation                         |
| TestNG            | Test execution framework               |
| ExtentReports     | HTML reporting                         |
| Maven             | Build and dependency management        |
| GitHub Actions    | CI/CD pipeline                         |
| JaCoCo            | Code coverage                          |

## API Coverage

The framework covers all **CRUD operations** and includes:

- **User Management**  
  - Create User  
  - Generate Token  
  - Get User  

- **Book Collection**  
  - Add Book  
  - Get Book
  - Update Book
  - Delete Book 

- **Negative test**

## Testing Strategy

### 1. **Approach to Writing Test Flows**
- Created modular tests for user and book operations.
- Used `dependsOnMethods` in TestNG to preserve logical flow (e.g., token creation depends on user creation and so on).
- Ensured test data like usernames are **randomized using Math.Random class** to avoid conflicts for duplicacy.
- Used request chaining (e.g., userID from `/User` used for `/Books` and so on).

### 2. **Reliability & Maintainability**
- Centralized base URL and credentials in `config.properties`.
- Separated test logic, config, utilities, and reporting.
- Used assertions on status codes, response payloads.
- Included **both positive and negative** test scenarios.
- Implemented `BaseTest` to initialize ExtentReports and RestAssured.

### 3. **Challenges & Solutions**
| Challenge | Solution |
|----------|----------|
| Some API endpoints returned 400/401 unexpectedly | Added proper headers and verified request bodies using Swagger docs |
| Dynamic data | Used randomization to generate unique usernames |

## CI/CD Pipeline

### Trigger:  
Runs on **every push** and **pull request** to `main`.

### Steps in CICD process:
- Checkout code
- Setup Java (Temurin 17)
- Build and run tests via Maven
- Upload test reports:
  - Surefire Reports (TestNG)
  - ExtentReports (HTML)
  - JaCoCo (code coverage)

## Running Tests Locally

### Clone the repo in the required directory on your PC if you want to run locally:
git clone https://github.com/karanAtreya1986/BookStoreAPITesting.git

cd BookStoreAPITesting

Run test using Maven:
mvn clean test

View the report in your browser. Reports are present in the path:
test-output/ExtentReports.html

## How to View GitHub Actions Reports
Go to repo URL (https://github.com/karanAtreya1986/BookStoreAPITesting.git) → Actions
Click on the latest workflow run.
Scroll to Artifacts.

Download-

Extent Report (ExtentReports.html)

JaCoCo Report (index.html)

Surefire Report

## Contributing to repo:
Fork the repo

Create your feature branch (git checkout -b feature/add-new-test)

Commit and push

Submit a pull request




## Authors

- [@karanAtreya1986](https://www.github.com/karanAtreya1986)