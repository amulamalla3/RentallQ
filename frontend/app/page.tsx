"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";

type Property = {
  id: number;
  name: string;
  address: string;
  city: string;
  state: string;
  bedrooms: number;
  bathrooms: number;
  squareFeet: number;
  currentMonthlyRent: number;
  occupancyRate: number;
  monthlyExpenses: number;
  estimatedMonthlyRevenue: number;
  estimatedMonthlyProfit: number;
};

type Analysis = {
  propertyId: number;
  propertyName: string;
  currentRent: number;
  predictedRent: number;
  rentDifferencePercent: number;
  occupancyRate: number;
  estimatedCurrentRevenue: number;
  predictedRevenue: number;
  estimatedCurrentProfit: number;
  potentialMonthlyUpside: number;
  confidence: number;
  recommendation: string;
  explanationSource: string;
  modelVersion: string;
  modelMae: number;
  modelR2: number;
  degradedMode: boolean;
};

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const money = (value: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(value);

const pct = (value: number) => `${Math.round(value * 100)}%`;

export default function Home() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [analysis, setAnalysis] = useState<Analysis | null>(null);
  const [analyzingId, setAnalyzingId] = useState<number | null>(null);
  const [showForm, setShowForm] = useState(false);

  async function loadProperties() {
    try {
      setError("");
      const response = await fetch(`${API}/api/properties`, { cache: "no-store" });
      if (!response.ok) throw new Error("Could not load properties");
      setProperties(await response.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load properties");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProperties();
  }, []);

  const summary = useMemo(() => {
    const revenue = properties.reduce((sum, p) => sum + Number(p.estimatedMonthlyRevenue), 0);
    const expenses = properties.reduce((sum, p) => sum + Number(p.monthlyExpenses), 0);
    const profit = properties.reduce((sum, p) => sum + Number(p.estimatedMonthlyProfit), 0);
    const occupancy = properties.length
      ? properties.reduce((sum, p) => sum + p.occupancyRate, 0) / properties.length
      : 0;
    return { revenue, expenses, profit, occupancy };
  }, [properties]);

  async function analyze(propertyId: number) {
    setAnalyzingId(propertyId);
    setError("");
    try {
      const response = await fetch(`${API}/api/properties/${propertyId}/analyze`, { method: "POST" });
      if (!response.ok) throw new Error("Analysis request failed");
      setAnalysis(await response.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Analysis request failed");
    } finally {
      setAnalyzingId(null);
    }
  }

  async function createProperty(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    const form = new FormData(event.currentTarget);
    const body = {
      name: form.get("name"),
      address: form.get("address"),
      city: form.get("city"),
      state: form.get("state"),
      bedrooms: Number(form.get("bedrooms")),
      bathrooms: Number(form.get("bathrooms")),
      squareFeet: Number(form.get("squareFeet")),
      currentMonthlyRent: Number(form.get("currentMonthlyRent")),
      occupancyRate: Number(form.get("occupancyRate")),
      monthlyExpenses: Number(form.get("monthlyExpenses")),
    };

    const response = await fetch(`${API}/api/properties`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const payload = await response.json().catch(() => null);
      setError(payload?.message ?? "Could not create property");
      return;
    }

    event.currentTarget.reset();
    setShowForm(false);
    await loadProperties();
  }

  return (
    <main className="shell">
      <header className="topbar">
        <div>
          <div className="eyebrow">Rental portfolio intelligence</div>
          <h1>RentalIQ</h1>
        </div>
        <button className="primary" onClick={() => setShowForm((value) => !value)}>
          {showForm ? "Close form" : "+ Add property"}
        </button>
      </header>

      <section className="hero">
        <div>
          <span className="pill">DEMO PORTFOLIO</span>
          <h2>Know what is underperforming before it costs you another month.</h2>
          <p>
            RentalIQ combines portfolio metrics, an ML pricing model, and an explanation layer to turn raw property data into a concrete pricing decision.
          </p>
        </div>
      </section>

      {error && <div className="error">{error}</div>}

      {showForm && (
        <section className="panel form-panel">
          <div className="section-heading">
            <div>
              <div className="eyebrow">POST /api/properties</div>
              <h3>Add a property</h3>
            </div>
            <p>This form creates and persists a new PostgreSQL record through the Spring Boot API.</p>
          </div>
          <form className="property-form" onSubmit={createProperty}>
            <input name="name" placeholder="Property name" required />
            <input name="address" placeholder="Address" required />
            <input name="city" placeholder="City" defaultValue="Atlanta" required />
            <input name="state" placeholder="State" defaultValue="GA" maxLength={2} required />
            <input name="bedrooms" type="number" min="0" placeholder="Bedrooms" required />
            <input name="bathrooms" type="number" step="0.5" min="0" placeholder="Bathrooms" required />
            <input name="squareFeet" type="number" min="100" placeholder="Square feet" required />
            <input name="currentMonthlyRent" type="number" min="0" placeholder="Monthly rent" required />
            <input name="occupancyRate" type="number" step="0.01" min="0" max="1" defaultValue="0.92" placeholder="Occupancy 0–1" required />
            <input name="monthlyExpenses" type="number" min="0" placeholder="Monthly expenses" required />
            <button className="primary" type="submit">Create property</button>
          </form>
        </section>
      )}

      <section className="metrics">
        <Metric label="Properties" value={String(properties.length)} />
        <Metric label="Monthly revenue" value={money(summary.revenue)} />
        <Metric label="Monthly profit" value={money(summary.profit)} />
        <Metric label="Avg. occupancy" value={pct(summary.occupancy)} />
      </section>

      <section className="panel">
        <div className="section-heading">
          <div>
            <div className="eyebrow">Portfolio</div>
            <h3>Property performance</h3>
          </div>
          <p>Analyze a property to call the Python model and compare current rent with the model estimate.</p>
        </div>

        {loading ? (
          <div className="empty">Loading properties…</div>
        ) : (
          <div className="property-grid">
            {properties.map((property) => (
              <article className="property-card" key={property.id}>
                <div className="property-card-top">
                  <div>
                    <div className="eyebrow">{property.city}, {property.state}</div>
                    <h4>{property.name}</h4>
                    <p>{property.address}</p>
                  </div>
                  <div className="score">{Math.round(property.occupancyRate * 100)}</div>
                </div>
                <div className="property-stats">
                  <div><span>Rent</span><strong>{money(property.currentMonthlyRent)}</strong></div>
                  <div><span>Revenue</span><strong>{money(property.estimatedMonthlyRevenue)}</strong></div>
                  <div><span>Profit</span><strong>{money(property.estimatedMonthlyProfit)}</strong></div>
                  <div><span>Size</span><strong>{property.squareFeet.toLocaleString()} ft²</strong></div>
                </div>
                <button className="secondary" onClick={() => analyze(property.id)} disabled={analyzingId === property.id}>
                  {analyzingId === property.id ? "Analyzing…" : "Analyze pricing →"}
                </button>
              </article>
            ))}
          </div>
        )}
      </section>

      {analysis && (
        <section className="analysis panel">
          <div className="section-heading">
            <div>
              <div className="eyebrow">ML + explanation</div>
              <h3>{analysis.propertyName}</h3>
            </div>
            <button className="text-button" onClick={() => setAnalysis(null)}>Close</button>
          </div>

          {analysis.degradedMode ? (
            <div className="warning">{analysis.recommendation}</div>
          ) : (
            <>
              <div className="comparison">
                <div><span>Current rent</span><strong>{money(analysis.currentRent)}</strong></div>
                <div className="arrow">→</div>
                <div><span>Model estimate</span><strong>{money(analysis.predictedRent)}</strong></div>
                <div><span>Difference</span><strong>{analysis.rentDifferencePercent > 0 ? "+" : ""}{analysis.rentDifferencePercent.toFixed(1)}%</strong></div>
              </div>
              <p className="recommendation">{analysis.recommendation}</p>
              <div className="model-meta">
                <span>Model: {analysis.modelVersion}</span>
                <span>Confidence: {Math.round(analysis.confidence * 100)}%</span>
                <span>MAE: {money(analysis.modelMae)}</span>
                <span>R²: {analysis.modelR2.toFixed(3)}</span>
                <span>Explanation: {analysis.explanationSource}</span>
              </div>
            </>
          )}
        </section>
      )}
    </main>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
