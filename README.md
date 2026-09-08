# RentalIQ

RentalIQ is a full-stack rental-property intelligence platform that helps property owners understand portfolio performance and make better pricing decisions.

## What is implemented

- **Next.js + TypeScript dashboard** for viewing and adding properties.
- **Java 21 + Spring Boot backend** with Controller → Service → Repository layering.
- **PostgreSQL persistence** for structured property data.
- **Python FastAPI ML service** with a trained rental-price model and holdout metrics.
- **Pricing analysis endpoint** that combines business metrics with ML predictions.
- **AI explanation layer** that can use OpenAI when `OPENAI_API_KEY` is configured; otherwise it falls back to a deterministic explanation so the demo always works.
- **Failure isolation**: if the ML service is unavailable, the Java backend returns a controlled degraded response instead of crashing.
- **Validation and duplicate protection** for common bad-input cases.
- **Seeded demo data** so the app is useful immediately after startup.

> Important: the current project has an AI explanation layer, not a fully autonomous tool-calling agent. Do not claim autonomous agent orchestration until you add it.

## Architecture

```text
Next.js frontend
      |
      | HTTP/JSON
      v
Spring Boot backend
      |         \
      |          \ HTTP/JSON
      v           v
PostgreSQL     Python FastAPI
                  |
                  v
              ML model
                  |
                  v
          AI explanation layer
```

The main backend remains the source of truth for application data and business logic. The Python service owns machine-learning inference because Python has the stronger ML ecosystem. The service boundary means the model can be replaced or retrained without rewriting the Java backend.

## Fastest demo: Docker Compose

Prerequisite: Docker Desktop.

```bash
cp .env.example .env
docker compose up --build
```

Open:

- Dashboard: http://localhost:3000
- Backend: http://localhost:8080/api/properties
- Backend health: http://localhost:8080/actuator/health
- ML health: http://localhost:8000/health
- ML docs: http://localhost:8000/docs

Three Atlanta demo properties are inserted automatically on first startup.

### Stop

```bash
docker compose down
```

### Reset all demo data

```bash
docker compose down -v
docker compose up --build
```

## Demo flow

1. Open the dashboard and show the seeded portfolio metrics.
2. Click **Analyze** on `Midtown Atlanta Condo`.
3. Explain that the frontend calls `POST /api/properties/{id}/analyze`.
4. The Spring service reads the property from PostgreSQL, computes current revenue/profit metrics, and calls the Python ML service.
5. The ML service predicts market rent and exposes its model MAE/R².
6. The explanation layer turns the prediction into a concise recommendation.
7. Add a new property from the form to demonstrate a real `POST /api/properties` request and persistence.

See `docs/demo-script.md` for a 2-minute interview demo.

## API examples

### Create a property

```bash
curl -X POST http://localhost:8080/api/properties \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Virginia Highland Duplex",
    "address": "500 Demo Ave",
    "city": "Atlanta",
    "state": "GA",
    "bedrooms": 3,
    "bathrooms": 2.5,
    "squareFeet": 1750,
    "currentMonthlyRent": 2600,
    "occupancyRate": 0.93,
    "monthlyExpenses": 780
  }'
```

### List properties

```bash
curl http://localhost:8080/api/properties
```

### Analyze property `1`

```bash
curl -X POST http://localhost:8080/api/properties/1/analyze
```

## Local development without Docker

### PostgreSQL

Create a database named `rentaliq`, or update the database environment variables.

### ML service

```bash
cd ml-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --reload --port 8000
```

### Backend

Java 21 and Maven 3.6.3+ are required.

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Interview explanation

The most important architecture point is **separation of concerns**:

- Next.js owns presentation.
- Spring Boot owns API validation, business logic, persistence, and orchestration.
- PostgreSQL owns durable structured data.
- Python owns ML training/inference.
- The LLM, when enabled, explains model output rather than inventing the numerical prediction.

This gives measurable predictions, explicit API contracts, independent deployment boundaries, and graceful degradation when the ML service is unavailable.

## Current limitations / honest next steps

- Demo data is synthetic; production pricing would require a licensed/public real-estate data source and a carefully evaluated training set.
- Authentication and multi-tenant authorization are not yet implemented.
- The AI layer currently explains analysis; full tool-calling agent workflows are a future milestone.
- Hibernate schema updates are used for demo simplicity; production should use versioned database migrations such as Flyway.

## Repository map

```text
backend/        Java Spring Boot API
frontend/       Next.js dashboard
ml-service/     FastAPI ML + explanation service
infrastructure/ deployment notes / future Terraform
docs/           architecture, demo, interview notes
scripts/        command-line demo helper
```
