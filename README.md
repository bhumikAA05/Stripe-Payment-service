# Stripe Payment Processing System
> Built during the Aug25 – Jul26 Java Microservices Course

A production-style **microservices payment platform** built with Spring Boot 3, Stripe API, AWS, and ActiveMQ.

---

## Architecture Overview

```
Client → stripe-provider-service (8081) ←→ Stripe API
              ↓ RestClient (BasicAuth)
         processing-service (8082) ←→ MySQL/RDS (PaymentDB)
              ↓ ActiveMQ
         notification-service (8083) → Email/SMS

All services register with → Eureka Server (8761)
All protected by           → Circuit Breaker (Resilience4j)
All deployed on            → AWS EC2 + RDS + Secrets Manager
Logs shipped to            → ELK Stack / S3
```

---

## Microservices

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service discovery (Netflix Eureka) |
| `stripe-provider-service` | 8081 | Stripe API: CreateSession, GetSession, ExpireSession, Webhooks |
| `processing-service` | 8082 | Payment domain: Transactions, PaymentStatusSystem, DB |
| `notification-service` | 8083 | ActiveMQ consumer: Email/SMS notifications |

---

## Course Session Mapping

| Week | Session | What's Built Here |
|---|---|---|
| W1 | D3–D5 | Spring Boot app, Maven, layered architecture |
| W2 | D1–D2 | Stripe dashboard, CreateSession API call |
| W2 | D3–D4 | Payment Domain model, Microservices intro |
| W3 | D1 | Spring & Maven Profiles (dev/prod in application.yml) |
| W3 | D2 | RestAPI standards (`@RestController`, `@RequestMapping`) |
| W3 | D4 | Git setup, stripe-provider-service scaffold |
| W3 | D5 | create-payment RestAPI, Packaging, Swagger (`springdoc`) |
| W4 | D1 | RestClient, BasicAuth, Lambda, `HttpServiceEngine` |
| W4 | D2 | `StripeConstants`, `JsonUtil` with Jackson |
| W4 | D3 | `CustomException`, `ControllerAdvice`, `GlobalExceptionHandler` |
| W5 | D1 | Stripe GetSession + ExpireSession APIs |
| W5 | D2 | PaymentDB setup, Spring JDBC integration |
| W5 | D3 | Processing 2 APIs, API Versioning `/api/v1/`, Factory pattern |
| W5 | D4 | `PaymentStatusSystem`, `TransactionDTO`, ModelMapper |
| W5 | D5 | `TransactionDAO` (Spring JDBC), Enums, schema.sql |
| W6 | D1 | Update Status + Pending Status DAO logic |
| W6 | D2 | stripe-provider → processing-service API integration |
| W6 | D4 | Webhook processing, HmacSHA256, Async Thread |
| W6 | D5 | Full webhook processing logic |
| W8 | D1 | Notification usecases |
| W8 | D2 | AWS EC2 + Firewall + stripe-provider-service deploy |
| W8 | D3 | AWS RDS + Secrets Manager (prod profile) |
| W8 | D4 | Unit testing + Mocking + Code coverage (`ProcessingServiceTest`) |
| W8 | D5 | Recon service usecase, ActiveMQ, release process |
| W9 | D3 | Circuit Breaker (Resilience4j config in application.yml) |
| W9 | D4 | Netflix Eureka MS communication |
| W9 | D5 | ELK + S3 distributed logging |

---

## Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8 (or H2 for local dev)
- Apache ActiveMQ 5.x
- Stripe Account (test keys)
- Docker (optional, for ActiveMQ + MySQL)

---

## Quick Start (Local Dev)

### 1. Start ActiveMQ
```bash
docker run -p 61616:61616 -p 8161:8161 rmohr/activemq
```

### 2. Set environment variables
```bash
export STRIPE_SECRET_KEY=sk_test_your_key_here
export STRIPE_WEBHOOK_SECRET=whsec_your_secret
```

### 3. Start services in order
```bash
# Terminal 1 - Eureka
cd eureka-server && mvn spring-boot:run

# Terminal 2 - Stripe Provider (uses H2 in dev)
cd stripe-provider-service && mvn spring-boot:run

# Terminal 3 - Processing Service
cd processing-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 4 - Notification Service
cd notification-service && mvn spring-boot:run
```

### 4. Access
- Eureka Dashboard: http://localhost:8761
- Stripe API Swagger: http://localhost:8081/swagger-ui.html
- Processing API Swagger: http://localhost:8082/swagger-ui.html
- H2 Console: http://localhost:8082/h2-console

---

## Key API Endpoints

### stripe-provider-service (port 8081)
```
POST /api/v1/stripe/create-session    - Create Stripe checkout session
GET  /api/v1/stripe/get-session/{id}  - Get session details
PUT  /api/v1/stripe/expire/{id}       - Expire session
POST /api/v1/stripe/webhook           - Stripe webhook (HmacSHA256)
```

### processing-service (port 8082)
```
POST /api/v1/transactions             - Create transaction
GET  /api/v1/transactions             - List all transactions
GET  /api/v1/transactions/{txnId}     - Get by ID
GET  /api/v1/transactions/pending     - Get pending transactions
PUT  /api/v1/transactions/{txnId}/status - Update status
```

---

## Create a Payment — Example

```bash
# Step 1: Create a transaction in processing-service
curl -X POST http://localhost:8082/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Rahul Sharma",
    "customerEmail": "rahul@example.com",
    "amount": 4999.00,
    "currency": "INR",
    "paymentMethod": "card"
  }'

# Step 2: Create Stripe checkout session
curl -X POST http://localhost:8081/api/v1/stripe/create-session \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Rahul Sharma",
    "customerEmail": "rahul@example.com",
    "amount": 499900,
    "currency": "INR",
    "txnId": "TXN-ABC12345"
  }'

# Step 3: Customer pays via sessionUrl → Stripe sends webhook
# Step 4: processing-service updates status to COMPLETED
# Step 5: notification-service sends confirmation via ActiveMQ
```

---

## AWS Production Setup (W8D2–D3)

1. Launch EC2 instance (t3.medium) with security group allowing ports 8081–8083
2. Set up RDS MySQL with `paymentdb` schema (schema.sql)
3. Store secrets in AWS Secrets Manager:
   - `stripe/api-key`
   - `stripe/webhook-secret`
   - `db/username` and `db/password`
4. Run with prod profile: `java -jar app.jar --spring.profiles.active=prod`

---

## Running Tests

```bash
cd processing-service
mvn test

# With coverage report
mvn test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

## Technology Stack

| Technology | Usage |
|---|---|
| Spring Boot 3.2 | Core framework |
| Spring Web MVC | REST controllers |
| Spring JDBC | Database access (TransactionDAO) |
| Spring Cloud Eureka | Service discovery |
| Resilience4j | Circuit breaker |
| Stripe Java SDK 24.x | Stripe API integration |
| ActiveMQ | Async messaging (notifications) |
| MySQL / H2 | Database (prod / dev) |
| SpringDoc OpenAPI | Swagger UI |
| Lombok | Boilerplate reduction |
| Mockito / JUnit 5 | Unit testing |
| AWS EC2 + RDS + Secrets Manager | Production deployment |
| ELK Stack | Distributed logging |
