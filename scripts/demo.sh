#!/usr/bin/env bash
set -euo pipefail

API="${API_BASE_URL:-http://localhost:8080}"

echo "== Health =="
curl -s "$API/actuator/health"; echo

echo "== Seeded properties =="
curl -s "$API/api/properties"; echo

echo "== Analyze property 1 =="
curl -s -X POST "$API/api/properties/1/analyze"; echo

echo "== Create demo property =="
curl -s -X POST "$API/api/properties" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Duplex","address":"900 Interview Way","city":"Atlanta","state":"GA","bedrooms":3,"bathrooms":2.5,"squareFeet":1800,"currentMonthlyRent":2500,"occupancyRate":0.92,"monthlyExpenses":800}'; echo
