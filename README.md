# Weather & Electricity Price Aggregator

A backend application built as part of a developer technical assignment.
The system aggregates historical electricity prices and weather data, allowing users to query and analyze correlations.

- **Backend:** Java 21 (Spring Boot 3)
- **Database:** PostgreSQL 16
- **Architecture:** Dockerized

---

### Features

- Receives uploaded CSV file of historical electricity prices (Nordpool, NPS Eesti)
- Stores and upserts price data into PostgreSQL
- Fetches historical weather data (temperature) from [Open-Meteo API](https://open-meteo.com/en/docs/historical-weather-api )
  - Fills missing weather gaps automatically
- Provides REST API endpoints for:
  - Uploading and importing electricity prices
  - Counting rows in electricity price table
  - Triggering weather data fetch
  - Querying aggregated data
- API documented with **Swagger UI**

---

## Data Handling Logic

**Price Import**
- CSV parsed using Apache Commons CSV
- Only Estonian spot price column (`NPS Eesti`) is imported
- Records are *upserted*
  - Existing dates are updated, missing dates inserted

**Weather Fetch**
- Periodic task (runs every minute) queries Open-Meteo API for daily temperature data
  - Only for days where some electricity price data point is present
- Data stored in database by date
- Weather import can be manually triggered via supplied endpoint

---

### Prerequisites

- Git
- [Docker](https://docs.docker.com/get-docker/) (tested with Docker Desktop on Windows)
- [Docker Compose](https://docs.docker.com/compose/install/) (typically bundled with Docker Desktop)
- JDK 21

---

## Quick Start Guide

### Clone the repository

```powershell
git clone https://github.com/kjlellep/energy-aggregator.git
cd energy-aggregator
```

### Build and start containers

From the project root, run:

```powershell
docker compose up --build
```

* This builds and starts:

  * Spring Boot backend (http://localhost:8080)
    * Home (`/`) forwards to Actuator links at /actuator
    * Swagger documentation `/swagger-ui/index.html`
  * PostgreSQL database (localhost:5432 inside Docker)

---

### Initialize database

The backend uses **Spring Boot + Flyway** for automatic schema migrations.
On first startup, migrations will run automatically meaning there is no manual setup needed.

---

## Security & Credentials (Local Development Only)

This repository is a self-contained technical assignment intended for **local** use.
For a zero-configuration reviewer experience, the Docker Compose file includes
development-only default credentials:

- DB: `POSTGRES_DB=energy`, `POSTGRES_USER=energy`, `POSTGRES_PASSWORD=energy`
- These values are **not** used in any non-local environment and must not be reused elsewhere.
- The database is bound to `127.0.0.1` to avoid external exposure.

You may override these defaults by providing a `.env` file, but it is **not required**.

---

### Run Tests

Integration tests use **Testcontainers** to start a temporary **PostgreSQL container** automatically.
These can be run in the host from the project root using the following commands:

Windows (PowerShell):
```powershell
.\mvnw -Dtest="*IT" test
```
Linux / macOS / WSL:
```bash
./mvnw -Dtest='*IT' test
```

> Note: The database runs in a Docker container via Testcontainers. A host JDK is required to run the tests,
> but the database itself is containerized

---

### API Documentation

Interactive API docs are available via Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

This includes all endpoints, input/output schemas, and example responses.

---

### Useful Docker Commands

Access PostgreSQL CLI inside database container:

```bash
docker compose exec db psql -U energy -d energy
```

Stop containers:

```bash
docker compose down
```

---

### Author

Built by Karl-Jonathan Lellep
