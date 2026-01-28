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
 │   ├── controller    # REST co
```
