# 🏦 Enterprise Banking Microservices Platform
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)

A production-grade, cloud-ready enterprise banking backend built with **Spring Boot 3.3.5**, **Java 21**, and **Microservices Architecture**. The platform handles user authentication, account management, transactions, payments, and real-time email notifications — all running in Docker containers and communicating through an API Gateway.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                    Client Apps                       │
│                 (Web / Mobile / CLI)                 │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                   API Gateway                        │
│                   Port: 8080                         │
│         JWT Validation | Rate Limiting               │
│         Request Logging | CORS Handling              │
└──────┬───────────┬──────────┬───────────┬───────────┘
       │           │          │           │
┌──────▼──┐  ┌─────▼───┐ ┌───▼──────┐ ┌─▼────────┐
│  Auth   │  │ Account │ │Transaction│ │ Payment  │
│ Service │  │ Service │ │ Service  │ │ Service  │
│  :8081  │  │  :8082  │ │  :8083  │ │  :8084   │
└─────────┘  └────┬────┘ └────┬─────┘ └────┬─────┘
                  │           │             │
                  └───────────┴─────────────┘
                              │
                  ┌───────────▼─────────────┐
                  │   Notification Service   │
                  │         :8085            │
                  │    Email Notifications   │
                  └─────────────────────────┘

┌─────────────────────────────────────────────────────┐
│                Infrastructure Layer                  │
│  PostgreSQL (per-service DB) | Redis (Cache)        │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15 (per-service) |
| Cache | Redis 7 |
| Migrations | Flyway |
| Messaging | REST (HTTP) |
| Email | Spring Mail + Gmail SMTP |
| Documentation | SpringDoc OpenAPI / Swagger UI |
| Containerization | Docker + Docker Compose |
| Build Tool | Maven |
| Code Generation | Lombok |

---

## 🏗️ Services

### 1. Auth Service — Port 8081
Handles all identity and access management for the platform.
- User registration and login
- JWT access token generation (15 min expiry)
- JWT refresh token (7 days expiry)
- Role-based access control (USER, ADMIN, TELLER)
- BCrypt password hashing
- Flyway database migrations

### 2. Account Service — Port 8082
Manages bank accounts and balances.
- Create SAVINGS and CHECKING accounts
- Real-time balance lookups with Redis caching (Cache-Aside pattern)
- Credit and debit operations
- Account status management (ACTIVE, SUSPENDED, CLOSED)
- Daily transaction limits

### 3. Transaction Service — Port 8083
Records all financial movements as an audit trail.
- Deposit and withdrawal processing
- Calls Account Service to update balances
- Permanent transaction history with reference IDs
- Transaction status tracking (PENDING, COMPLETED, FAILED)

### 4. Payment Service — Port 8084
Handles fund transfers between accounts.
- Account-to-account transfers
- Scheduled recurring payments
- Saga pattern for distributed transaction consistency
- Payment status lifecycle management

### 5. Notification Service — Port 8085
Sends real-time email notifications for banking events.
- Deposit, withdrawal, and transfer confirmations
- Duplicate notification prevention via referenceId
- Automatic retry for failed notifications (every 5 min, max 3 retries)
- Notification history per user
- Gmail SMTP integration

### 6. API Gateway — Port 8080
Single entry point for all client requests.
- JWT validation before forwarding requests
- Rate limiting (60 requests/min per IP)
- Request and response logging with timing
- CORS handling
- Routes all traffic to downstream services

---

## 🚀 Getting Started

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running
- Git

### 1. Clone the Repository
```bash
git clone https://github.com/karthik-mogilipuram/Enterprise-Banking-Microservices.git
```

### 2. Configure Environment Variables
Create a `.env` file in the project root:
```env
DB_PASSWORD=your-postgres-password
JWT_SECRET=dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBqd3QgdG9rZW4gZ2VuZXJhdGlvbiBiYW5raW5n
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-16-char-gmail-app-password
```

> **Note:** For Gmail, generate an App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords). 2-Step Verification must be enabled.

### 3. Start Everything
```bash
docker-compose up -d
```

This single command will:
- Start PostgreSQL and automatically create all 5 databases
- Start Redis
- Build and start all 6 Spring Boot services
- Set up all networking between containers

### 4. Wait for Services to Start
Check all containers are running:
```bash
docker ps
```

Watch gateway logs to confirm everything is ready:
```bash
docker logs banking-gateway -f
```

Wait until you see: `Started ApiGatewayApplication`

### 5. Access the Platform
| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Auth Swagger UI | http://localhost:8081/swagger-ui.html |
| Account Swagger UI | http://localhost:8082/swagger-ui.html |
| Transaction Swagger UI | http://localhost:8083/swagger-ui.html |
| Payment Swagger UI | http://localhost:8084/swagger-ui.html |
| Notification Swagger UI | http://localhost:8085/swagger-ui.html |

---

## 📡 API Endpoints

All endpoints below go through the API Gateway at `http://localhost:8080`.

### Auth Service `/api/auth`

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login and get JWT tokens |
| POST | `/api/auth/refresh` | No | Refresh access token |
| GET | `/api/auth/me` | Yes | Get current user profile |

**Register Request:**
```json
{
  "fullName": "Karthik Mogilipuram",
  "email": "karthik@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

**Login Response:**
```json
{
  "userId": "uuid",
  "email": "karthik@example.com",
  "fullName": "Karthik Mogilipuram",
  "role": "USER",
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci..."
}
```

---

### Account Service `/api/accounts`

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/accounts` | Yes | Create a new bank account |
| GET | `/api/accounts/{id}` | Yes | Get account details |
| GET | `/api/accounts/user/{userId}` | Yes | Get all accounts for a user |
| GET | `/api/accounts/{id}/balance` | Yes | Get current balance |
| PATCH | `/api/accounts/{id}/status` | Yes | Update account status |

**Create Account Request:**
```json
{
  "userId": "uuid",
  "accountType": "SAVINGS",
  "initialDeposit": 5000
}
```

**Balance Response:**
```json
{
  "accountId": "uuid",
  "accountNumber": "1199579302",
  "balance": 6000.00,
  "currency": "USD"
}
```

---

### Transaction Service `/api/transactions`

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/transactions/deposit` | Yes | Deposit money |
| POST | `/api/transactions/withdraw` | Yes | Withdraw money |
| GET | `/api/transactions/{id}` | Yes | Get transaction details |
| GET | `/api/transactions/account/{accountId}` | Yes | Get transaction history |

**Deposit Request:**
```json
{
  "accountId": "uuid",
  "amount": 1000,
  "description": "Salary deposit"
}
```

**Transaction Response:**
```json
{
  "id": "uuid",
  "accountId": "uuid",
  "type": "DEPOSIT",
  "amount": 1000,
  "balanceAfter": 6000.0,
  "status": "COMPLETED",
  "referenceId": "B2C19367",
  "description": "Salary deposit"
}
```

---

### Payment Service `/api/payments`

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/payments/transfer` | Yes | Transfer between accounts |
| POST | `/api/payments/schedule` | Yes | Schedule a payment |
| GET | `/api/payments/{id}` | Yes | Get payment details |
| GET | `/api/payments/account/{accountId}` | Yes | Get payment history |

**Transfer Request:**
```json
{
  "fromAccountId": "uuid",
  "toAccountId": "uuid",
  "amount": 500,
  "description": "Rent payment"
}
```

---

### Notification Service `/api/notifications`

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/notifications/send` | No | Send a notification |
| GET | `/api/notifications/user/{userId}` | No | Get user notifications |
| GET | `/api/notifications/status/{status}` | No | Get by status |
| POST | `/api/notifications/retry` | No | Manually trigger retry |

---

## 🧪 Testing the Platform

### Step 1 — Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Karthik Mogilipuram\",\"email\":\"karthik@test.com\",\"password\":\"password123\",\"phone\":\"1234567890\"}"
```

### Step 2 — Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"karthik@test.com\",\"password\":\"password123\"}"
```
Copy the `accessToken` and `userId` from the response.

### Step 3 — Create Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "{\"userId\":\"YOUR_USER_ID\",\"accountType\":\"SAVINGS\",\"initialDeposit\":5000}"
```

### Step 4 — Deposit Money
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "{\"accountId\":\"YOUR_ACCOUNT_ID\",\"amount\":1000,\"description\":\"Test deposit\"}"
```

### Step 5 — Check Balance
```bash
curl -X GET http://localhost:8080/api/accounts/YOUR_ACCOUNT_ID/balance \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Step 6 — Send Email Notification
```bash
curl -X POST http://localhost:8085/api/notifications/send \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"YOUR_USER_ID\",\"recipient\":\"your-email@gmail.com\",\"type\":\"EMAIL\",\"eventType\":\"DEPOSIT\",\"amount\":1000,\"balanceAfter\":6000,\"accountNumber\":\"YOUR_ACCOUNT_NUMBER\",\"referenceId\":\"TEST-001\",\"description\":\"Test\"}"
```

---

## 🐳 Docker Commands Reference

```bash
# Start all services
docker-compose up -d

# Start and rebuild all services
docker-compose up --build

# Rebuild only one service
docker-compose up --build api-gateway

# Stop all services
docker-compose down

# View running containers
docker ps

# View logs for a service
docker logs banking-gateway -f

# View last 50 lines of logs
docker logs banking-auth --tail 50
```

---

## 📁 Project Structure

```
Enterprise-Banking-Microservices/
├── docker-compose.yml
├── init-db.sql
├── .env
├── Auth Service/
│   └── auth-service/
├── Account Service/
│   └── account-service/
├── Transaction Service/
│   └── transaction-service/
├── Payment Service/
│   └── payment-services/
├── Notification Service/
│   └── notification-service/
└── API Gateway/
    └── api-gateway/
```

---

## 🔐 Security

- All endpoints except `/api/auth/register`, `/api/auth/login`, and `/api/auth/refresh` require a valid JWT token
- Tokens are validated at the API Gateway before forwarding to services
- Passwords are hashed using BCrypt
- JWT tokens are signed with HMAC-SHA384
- Rate limiting enforced at gateway level (60 requests/min per IP)
- Sensitive config loaded from environment variables — never hardcoded

---

## 👨‍💻 Author

**Karthik Mogilipuram**
Senior Full Stack Developer
- LinkedIn: [linkedin.com/in/karthik-mogilipuram](https://linkedin.com/in/karthik-mogilipuram)
- Email: moglipuram.karthik@gmail.com

---

## 📄 License

This project is licensed under the MIT License.
