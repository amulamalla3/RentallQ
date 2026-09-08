import os
from dataclasses import dataclass

import numpy as np
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

app = FastAPI(title="RentalIQ ML Service", version="1.0.0")


class PredictionRequest(BaseModel):
    city: str = Field(min_length=1, max_length=80)
    bedrooms: int = Field(ge=0, le=20)
    bathrooms: float = Field(ge=0, le=20)
    squareFeet: int = Field(ge=100, le=50000)
    occupancyRate: float = Field(ge=0, le=1)
    currentRent: float = Field(ge=0)


class PredictionResponse(BaseModel):
    predictedRent: float
    predictedRevenue: float
    confidence: float
    modelVersion: str
    mae: float
    r2: float


class ExplainRequest(BaseModel):
    propertyName: str
    city: str
    currentRent: float
    predictedRent: float
    occupancyRate: float
    monthlyExpenses: float
    estimatedCurrentRevenue: float
    predictedRevenue: float
    potentialMonthlyUpside: float


class ExplainResponse(BaseModel):
    explanation: str
    source: str


@dataclass
class ModelBundle:
    pipeline: Pipeline
    mae: float
    r2: float
    version: str


def build_training_data(seed: int = 42, rows: int = 3500) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    cities = np.array(["Atlanta", "Austin", "Miami", "Chicago", "Charlotte", "Nashville"])
    city = rng.choice(cities, size=rows, p=[0.25, 0.18, 0.16, 0.15, 0.13, 0.13])
    bedrooms = rng.integers(1, 5, size=rows)
    bathrooms = np.round(rng.uniform(1, np.minimum(4.5, bedrooms + 1.0)), 1)
    square_feet = np.clip(
        420 + bedrooms * 390 + bathrooms * 85 + rng.normal(0, 180, size=rows),
        450,
        3600,
    ).round().astype(int)
    occupancy = np.clip(rng.normal(0.91, 0.07, size=rows), 0.60, 1.0)

    city_base = {
        "Atlanta": 1125,
        "Austin": 1280,
        "Miami": 1540,
        "Chicago": 1210,
        "Charlotte": 1070,
        "Nashville": 1190,
    }

    base = np.array([city_base[c] for c in city])
    seasonal_market_noise = rng.normal(0, 95, size=rows)
    market_rent = (
        base
        + bedrooms * 310
        + bathrooms * 145
        + square_feet * 0.34
        + (occupancy - 0.85) * 520
        + seasonal_market_noise
    )
    market_rent = np.maximum(market_rent, 650)

    return pd.DataFrame(
        {
            "city": city,
            "bedrooms": bedrooms,
            "bathrooms": bathrooms,
            "squareFeet": square_feet,
            "occupancyRate": occupancy,
            "marketRent": market_rent,
        }
    )


def train_model() -> ModelBundle:
    df = build_training_data()
    x = df[["city", "bedrooms", "bathrooms", "squareFeet", "occupancyRate"]]
    y = df["marketRent"]
    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=0.2, random_state=42
    )

    preprocessor = ColumnTransformer(
        [("city", OneHotEncoder(handle_unknown="ignore"), ["city"])],
        remainder="passthrough",
    )
    regressor = RandomForestRegressor(
        n_estimators=140,
        random_state=42,
        min_samples_leaf=3,
        n_jobs=-1,
    )
    pipeline = Pipeline(
        [("preprocessor", preprocessor), ("regressor", regressor)]
    )
    pipeline.fit(x_train, y_train)
    predictions = pipeline.predict(x_test)
    return ModelBundle(
        pipeline=pipeline,
        mae=float(mean_absolute_error(y_test, predictions)),
        r2=float(r2_score(y_test, predictions)),
        version="synthetic-rf-v1",
    )


MODEL = train_model()


def confidence_for(frame: pd.DataFrame, predicted_rent: float) -> float:
    transformed = MODEL.pipeline.named_steps["preprocessor"].transform(frame)
    forest = MODEL.pipeline.named_steps["regressor"]
    tree_predictions = np.array([tree.predict(transformed)[0] for tree in forest.estimators_])
    spread = float(np.std(tree_predictions))
    if predicted_rent <= 0:
        return 0.5
    confidence = 1.0 - min(spread / predicted_rent, 0.45)
    return float(np.clip(confidence, 0.55, 0.95))


@app.get("/health")
def health():
    return {
        "status": "ok",
        "modelVersion": MODEL.version,
        "mae": round(MODEL.mae, 2),
        "r2": round(MODEL.r2, 4),
    }


@app.post("/predict", response_model=PredictionResponse)
def predict(request: PredictionRequest):
    frame = pd.DataFrame(
        [
            {
                "city": request.city,
                "bedrooms": request.bedrooms,
                "bathrooms": request.bathrooms,
                "squareFeet": request.squareFeet,
                "occupancyRate": request.occupancyRate,
            }
        ]
    )
    predicted = float(MODEL.pipeline.predict(frame)[0])
    confidence = confidence_for(frame, predicted)
    return PredictionResponse(
        predictedRent=round(predicted, 2),
        predictedRevenue=round(predicted * request.occupancyRate, 2),
        confidence=round(confidence, 2),
        modelVersion=MODEL.version,
        mae=round(MODEL.mae, 2),
        r2=round(MODEL.r2, 4),
    )


def deterministic_explanation(request: ExplainRequest) -> str:
    if request.currentRent <= 0:
        pct = 0.0
    else:
        pct = (request.predictedRent - request.currentRent) / request.currentRent * 100

    occupancy_pct = request.occupancyRate * 100
    if pct >= 5:
        return (
            f"{request.propertyName} appears underpriced relative to the model. "
            f"Current rent is ${request.currentRent:,.0f} versus a predicted market rent of "
            f"${request.predictedRent:,.0f} ({pct:.1f}% higher). At {occupancy_pct:.0f}% occupancy, "
            f"the model estimates about ${max(request.potentialMonthlyUpside, 0):,.0f} in additional monthly "
            "revenue potential. Treat this as decision support and confirm against real local comparables before changing rent."
        )
    if pct <= -5:
        return (
            f"{request.propertyName} may be priced above the model estimate. Current rent is "
            f"${request.currentRent:,.0f} versus a predicted market rent of ${request.predictedRent:,.0f}. "
            f"Occupancy is {occupancy_pct:.0f}%, so I would review local comparables and vacancy risk before increasing price."
        )
    return (
        f"{request.propertyName} is priced close to the model estimate of ${request.predictedRent:,.0f}. "
        f"With {occupancy_pct:.0f}% occupancy, the bigger optimization opportunity may be controlling the "
        f"${request.monthlyExpenses:,.0f} in monthly expenses and monitoring local demand rather than making a large rent change."
    )


def llm_explanation(request: ExplainRequest) -> str | None:
    if not os.getenv("OPENAI_API_KEY"):
        return None
    try:
        from openai import OpenAI

        client = OpenAI()
        model = os.getenv("OPENAI_MODEL", "gpt-5.6-luna")
        prompt = f"""
You are the explanation layer for a rental-property analytics product.
Use only the numbers below. Do not invent market data. Give 2-4 concise sentences.
Explain whether the property appears underpriced, overpriced, or near the estimate, mention occupancy,
and give one cautious action. State that the model is decision support, not an appraisal.

Property: {request.propertyName}, {request.city}
Current monthly rent: ${request.currentRent:.2f}
Predicted monthly rent: ${request.predictedRent:.2f}
Occupancy rate: {request.occupancyRate:.3f}
Monthly expenses: ${request.monthlyExpenses:.2f}
Estimated current revenue: ${request.estimatedCurrentRevenue:.2f}
Predicted revenue: ${request.predictedRevenue:.2f}
Potential monthly upside: ${request.potentialMonthlyUpside:.2f}
""".strip()
        response = client.responses.create(model=model, input=prompt)
        text = getattr(response, "output_text", None)
        return text.strip() if text else None
    except Exception:
        return None


@app.post("/explain", response_model=ExplainResponse)
def explain(request: ExplainRequest):
    llm_text = llm_explanation(request)
    if llm_text:
        return ExplainResponse(explanation=llm_text, source="openai")
    return ExplainResponse(
        explanation=deterministic_explanation(request),
        source="deterministic-fallback",
    )
