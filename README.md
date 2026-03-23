# Nguyen Pacific Bank API

A secure REST API for a banking application built with Spring Boot.  
Supports user authentication, account management, deposits, withdrawals, transaction history, and account transfers.

## Full Stack Application

- 🔧 Backend API (this repo)
- 🌐 Frontend Web App: [Nguyen Pacific Bank Frontend](https://github.com/travistn/nguyen-pacific-bank-frontend)

## Features

- User registration and login with JWT authentication
- Secure password hashing using BCrypt
- Create checking and savings accounts
- Retrieve accounts for the authenticated user
- Deposit and withdraw funds
- View transaction history
- Transfer funds between accounts
- Ownership validation to prevent unauthorized access

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT Authentication
- Maven


## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/travistn/nguyen-pacific-bank-api.git
cd nguyen-pacific-bank-api
```

### 2. Configure environment variables

Create environment variables for your database and JWT secret:

```env
DB_URL=your_postgres_connection_string
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_secret_key
```


### 3. Run the application

#### Windows

```
mvnw.cmd spring-boot:run
```
#### macOS / Linux
```
./mvnw spring-boot:run
```

The API will run at:
```
http://localhost:8080
```


### API Overview

### Public Endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`

### Authentication
```
Authorization: Bearer YOUR_TOKEN
```

### Main Resources
- `/api/accounts` — manage bank accounts
- `/api/transactions` — deposits, withdrawals, transfers, and history
