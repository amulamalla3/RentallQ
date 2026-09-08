# Architecture

## Request path

```text
Browser
  -> Next.js
  -> Spring Boot Controller
  -> Spring Boot Service
  -> Spring Data Repository -> PostgreSQL
  -> ML client -> FastAPI -> scikit-learn model
  -> explanation endpoint -> optional LLM/fallback
  -> response to browser
```

## Why Java + Spring Boot

The Java service is the application boundary. It validates incoming requests, owns business rules, controls persistence, and coordinates downstream services. Keeping this logic out of the UI and ML service makes the application easier to test and evolve.

## Why PostgreSQL

The core data is structured and relational. A property has a stable identity and measurable attributes, and future versions will add owners, portfolios, leases, reservations, maintenance events, and predictions. PostgreSQL provides transactions, constraints, indexes, and strong relational querying.

## Why Python for ML

Model training and inference use scikit-learn. Keeping ML behind an HTTP contract allows the Java application and model implementation to evolve independently.

## Failure handling

The Java ML client uses explicit timeouts. If prediction or explanation fails, the analysis endpoint returns a controlled degraded response rather than propagating a raw downstream exception to the user.

## API-contract risk

Java and Python use explicit request/response schemas. Integration tests should be added as the next step so a response-field rename in Python cannot silently break Java deserialization.
