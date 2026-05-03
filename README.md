# Simple Stock Market API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TomaszStr_SimpleStockMarket&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TomaszStr_SimpleStockMarket)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=TomaszStr_SimpleStockMarket&metric=coverage)](https://sonarcloud.io/summary/new_code?id=TomaszStr_SimpleStockMarket)

A high-availability, multi-instance Spring Boot application simulating a simplified stock exchange. This project is built using **Java 25**, leveraging modern language features and a robust containerized architecture to ensure scalability and fault tolerance.

## Overview

The application facilitates immediate buy/sell operations for stocks at a fixed price of 1. It is composed of three core domains:
- **Bank:** The sole liquidity provider controlling stock availability.
- **Wallets:** User-owned entities holding various stock inventories.
- **Audit Log:** A comprehensive record of all user-initiated wallet actions.

### Architecture & High Availability
The system is designed to be **Highly Available (HA)**. It utilizes:
- **3 Application Replicas:** Managed by Docker Compose to ensure zero downtime.
- **Nginx Load Balancer:** Distributes traffic across instances and handles failover.
- **PostgreSQL:** A shared persistent database for all instances.
- **Auto-Recreation:** If an instance fails (or is killed via `/chaos`), Docker automatically recreates it to maintain the 3-node quorum.

---

## Tech Stack

- **Runtime:** Java 25 (OpenJDK)
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Orchestration:** Docker & Docker Compose
- **Load Balancing:** Nginx
- **Build Tool:** Gradle

---

## Getting Started

The application is designed to run on **Windows, macOS (Intel/Apple Silicon), and Linux** using a single command.

### Prerequisites
- **Docker** and **Docker Compose** installed.
- Runtimes (Java/Go/TS/Kotlin) are assumed to be available as per requirements, but the build process is fully containerized.

### Running the Application (Production/HA Mode)

Navigate to the project root and execute the startup script with a port parameter of your choice.

**macOS / Linux:**
```bash
chmod +x start.sh
./start.sh 8080
```

**Windows:**
```bash
start.bat 8080
```

The API will be accessible at `http://localhost:8080`.

### Running Tests

To execute the comprehensive test suite (Unit and Integration tests):

```bash
./gradlew test
```

Note: Integration tests utilize Testcontainers (if configured) or a local Docker environment to verify database interactions and transactional integrity.

---

## API Endpoints

### Bank Operations
* **GET /bank**
    * Returns the current state of the bank's liquidity.
* **POST /bank**
    * Resets/Sets the bank's stock levels.
    * **Body:** `{"stocks": [{"name": "AAPL", "quantity": 100}]}`

### Wallet Operations
* **POST /wallets/{wallet_id}/stocks/{stock_name}**
    * Buy or Sell 1 unit of stock.
    * **Body:** `{"type": "buy"}` or `{"type": "sell"}`
    * *Note: Creates the wallet automatically if it does not exist.*
* **GET /wallets/{wallet_id}**
    * Returns the current state of a particular wallet.
    * **Response:** `{"id": "...", "stocks": [{"name": "...", "quantity": 0}]}`
* **GET /wallets/{wallet_id}/stocks/{stock_name}**
    * Returns a single number representing the quantity of the specified stock.

### Audit & System
* **GET /audit**
    * Returns a log of all wallet-related operations.
* **POST /chaos**
    * **Chaos Testing:** Kills the specific instance serving the request to test HA failover.

---

## Chaos & HA Verification

To verify the High Availability requirement:

1. **Start the application** using `./start.sh 8080`.
2. **Check the logs** or Docker Dashboard to see three instances running.
3. **Trigger the chaos endpoint:**

```bash
curl -X POST http://localhost:8080/chaos
```

Observation: The request returns 200 OK, but the container serving it terminates immediately. Nginx handles the failover to the remaining replicas, and Docker Compose automatically recreates the dead container.

--- 

### Engineering Practices

- **Global Error Handling:** Consistent 400 (Bad Request), 404 (Not Found), and 500 (Internal Error) responses.

- **Atomic Transactions:** Uses `@Transactional` to maintain database integrity across Bank and Wallet modules.

- **Clean Code:** Separation of concerns between Controller, Service, and Repository layers.