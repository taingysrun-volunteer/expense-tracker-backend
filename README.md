# Expense Tracker – Backend

Backend service for the **Expense Tracker** application. This service provides RESTful APIs for authentication, expense management, categorization, reporting, and admin operations with role-based access control.

---

## Overview

The backend is built using **Spring Boot** and follows a layered architecture with clear separation of concerns. It supports JWT-based authentication, admin and user roles, and integrates with PostgreSQL for persistent storage.

---

## Features

### Authentication & Authorization

* User registration and login
* JWT-based authentication
* Role-based access control (RBAC)

    * ROLE_USER
    * ROLE_ADMIN

### User APIs

* Create, update, delete expenses
* Categorize expenses
* View expense history
* Generate basic reports

### Admin APIs

* Admin dashboard data
* Manage users
* Manage categories
* View system-level reports
* Reset user passwords

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Security + JWT
* Spring Data JPA (Hibernate)
* PostgreSQL (Neon)
* Gradle
* Docker
* Swagger / OpenAPI

---

## System Architecture

```
[ Client / Frontend ]
        |
        v
[ Spring Boot REST API ]
        |
        v
[ PostgreSQL Database ]
```

---

## Project Structure

```
backend/
 ├── src/main/java
 │   ├── controller    # REST controllers
 │   ├── service       # Business logic
 │   ├── repository    # JPA repositories
 │   ├── entity        # JPA entities
 │   ├── dto           # Request/response DTOs
 │   ├── security      # JWT, filters, security config
 │   └── config        # Application configuration
 ├── src/main/resources
 │   ├── application.yml
 │   └── application-dev.yml
 ├── Dockerfile
 └── build.gradle
```

---

## API Documentation

Swagger UI is available once the application is running:

```
http://localhost:8080/swagger-ui.html
```

---

## Running Locally

### Prerequisites

* Java 17 or higher
* PostgreSQL
* Gradle
* Docker (optional)

### Environment Variables

```
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
```

### Start Application

```
./gradlew bootRun
```

---

## Testing

* Unit and integration tests for:

    * Authentication flows (login, register)
    * Admin features (user, category, report management)
* API testing via Swagger UI

---

## Deployment

* Backend deployed using Render
* PostgreSQL hosted on Neon
* Docker used to build and deploy the application
* Environment variables configured in Render dashboard

---

## Post-Deployment Verification

* Database connectivity verified
* CORS configuration validated
* Authentication flows tested
* Admin endpoints verified

---

## Future Enhancements

* Advanced reporting and analytics
* Pagination and filtering improvements
* Caching with Redis
* Audit logging
* Rate limiting

---

## Author

Taingy Srun
Backend / Software Engineer

---

## License

This project is intended for educational and portfolio use.
