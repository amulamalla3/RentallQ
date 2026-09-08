# Interview guide

## 30-second explanation

"RentalIQ is a full-stack rental-property intelligence project. I built the main API in Java and Spring Boot with PostgreSQL, and a Next.js frontend calls that API for property and portfolio data. I separated pricing prediction into a Python FastAPI service using scikit-learn, so the numerical model stays measurable and can evolve independently. The backend then combines the prediction with business metrics and an AI explanation layer to tell the user why a property may be under- or over-priced."

## If the Python service is slow or unavailable

A strong answer:

"I treat the ML service as a separate failure domain. The Java client has connection and request timeouts, and the analysis service catches downstream failures. Instead of crashing the application, it returns a degraded response that says analysis is temporarily unavailable. In a higher-scale version I would add bounded retries with exponential backoff, a circuit breaker, bulkhead isolation, and monitoring around timeout/error rates. I would avoid unbounded retries because they can amplify an outage."

## Why the LLM does not predict rent

"The rental-price prediction should be measurable, repeatable, and evaluated against a holdout set, so I use an ML model for the numeric prediction. An LLM is better suited to interpreting the model output and explaining the tradeoff to a user."

## POST request

`POST /api/properties` means the client is asking the server to create a resource using the JSON request body. Spring converts the JSON body into a validated Java request object, the service applies business rules, and the repository persists an entity.

POST is not inherently idempotent: sending the same create request twice can create two records. RentalIQ reduces accidental duplicates with a database unique constraint and a service-level duplicate check for address/city/state.

## Do not claim yet

Do not claim any of these until you implement them:

- production users
- live Airbnb/MLS data
- authentication/multi-tenancy
- Kafka/Redis
- AWS deployment/Terraform
- autonomous tool-calling AI agent
- revenue lift from recommendations
