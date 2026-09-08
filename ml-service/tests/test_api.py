from fastapi.testclient import TestClient

from app import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["mae"] > 0
    assert 0 <= body["r2"] <= 1


def test_prediction_is_valid():
    response = client.post(
        "/predict",
        json={
            "city": "Atlanta",
            "bedrooms": 2,
            "bathrooms": 2,
            "squareFeet": 1200,
            "occupancyRate": 0.95,
            "currentRent": 1800,
        },
    )
    assert response.status_code == 200
    body = response.json()
    assert body["predictedRent"] > 0
    assert body["predictedRevenue"] > 0
    assert 0.5 <= body["confidence"] <= 1.0


def test_invalid_occupancy_rejected():
    response = client.post(
        "/predict",
        json={
            "city": "Atlanta",
            "bedrooms": 2,
            "bathrooms": 2,
            "squareFeet": 1200,
            "occupancyRate": 1.4,
            "currentRent": 1800,
        },
    )
    assert response.status_code == 422
