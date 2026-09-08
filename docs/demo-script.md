# 2-minute RentalIQ demo script

## 0:00–0:20 — Problem

"RentalIQ helps rental-property owners understand which properties are underperforming and make better pricing decisions. I wanted the numerical prediction and the natural-language recommendation to be separate so the model stays measurable and reproducible."

## 0:20–0:45 — Dashboard

Open `http://localhost:3000`.

Show the seeded Atlanta properties and portfolio totals. Explain that the UI is Next.js and does not directly access either PostgreSQL or the ML model.

## 0:45–1:20 — Analyze a property

Click **Analyze** on `Midtown Atlanta Condo`.

Explain the call path:

1. `POST /api/properties/{id}/analyze`
2. Spring Boot loads the property from PostgreSQL.
3. The service calculates current revenue/profit.
4. Java calls the Python FastAPI service with a typed JSON contract.
5. The Python model predicts market rent and returns model metrics.
6. The explanation layer interprets the result.

Point out the model version, confidence, MAE, and R² shown in the UI.

## 1:20–1:45 — Demonstrate POST + persistence

Use the form to add a property.

"This sends a POST request to the Spring controller. Bean Validation rejects invalid values, the service checks for duplicate addresses, and the repository persists the entity in PostgreSQL."

Refresh the page and show that the property remains.

## 1:45–2:00 — Reliability

"The ML service is a separate failure domain. The Java client uses timeouts, and if Python is unavailable the API returns a controlled degraded response rather than taking down the main application."

## Questions to be ready for

- Why PostgreSQL instead of DynamoDB?
- Why split Java and Python?
- What does POST mean and why is it not necessarily idempotent?
- What happens when the ML service times out?
- What prevents duplicate properties?
- Why shouldn't an LLM predict rent directly?
- What would you change for production? (authentication, multi-tenancy, real data, migrations, caching, observability, stronger integration tests)
