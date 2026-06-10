"""
main.py — Smart Parking ML microservice
FastAPI + sklearn XGBoost Pipeline
Runs on port 8000 inside Docker network as 'ml-service'
"""

import os
import logging

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException

from schemas.predict_schema import PredictRequest, PredictResponse
from preprocessing.feature_builder import build_feature_dataframe, build_health_probe

# ── Logging ───────────────────────────────────────────────────────────────────
logging.basicConfig(level=logging.INFO)
log = logging.getLogger("ml-service")

# ── Config ────────────────────────────────────────────────────────────────────
MODEL_DIR    = os.getenv("MODEL_DIR", "/app/models")
ACTIVE_MODEL = "xgboost"
# ── Load XGBoost Pipeline once at startup ─────────────────────────────────────# Each .joblib file is a full sklearn Pipeline:
#   Pipeline([
#       ("preprocessor", ColumnTransformer([
#           ("ohe",         OneHotEncoder(...), ["day_of_week"]),
#           ("passthrough", "passthrough",      [...numeric cols...]),
#       ])),
#       ("classifier", XGBClassifier(...)),   # or LogisticRegression / RF
#   ])
MODEL_FILES = {
    "xgboost": "parking_xgboost_model_no_event.joblib",
}

models: dict = {}
for name, filename in MODEL_FILES.items():
    path = os.path.join(MODEL_DIR, filename)
    try:
        models[name] = joblib.load(path)
        log.info("Loaded model '%s' from %s", name, path)
    except FileNotFoundError:
        log.error("Model file not found: %s — service will start but '%s' unavailable", path, name)

if ACTIVE_MODEL not in models:
    raise RuntimeError(
        f"ACTIVE_MODEL='{ACTIVE_MODEL}' was not loaded. "
        f"Available: {list(models.keys())}"
    )

app = FastAPI(title="Smart Parking ML Service", version="1.0.0")


# ── Helper ────────────────────────────────────────────────────────────────────
def _predict_with(model_name: str, df: pd.DataFrame) -> dict:
    """Run one Pipeline and return prediction + probability."""
    if model_name not in models:
        raise HTTPException(status_code=400, detail=f"Model '{model_name}' not available")
    pipeline = models[model_name]
    prediction  = int(pipeline.predict(df)[0])
    probability = float(pipeline.predict_proba(df)[0][1])   # P(class=1)
    return {"prediction": prediction, "probability": probability}


# ── Endpoints ─────────────────────────────────────────────────────────────────
@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    """
    Predicts whether LOT_A will reach high occupancy in the next hour.
    Class 1 means occupancy_rate_lot >= 0.85 in the next hour.
    """
    df = build_feature_dataframe(req)
    result = _predict_with(ACTIVE_MODEL, df)

    log.info(
        "next-hour forecast | dow=%s hour=%d | pred=%d prob=%.3f",
        req.day_of_week,
        req.hour,
        result["prediction"],
        result["probability"]
    )

    return PredictResponse(
        **result,
        model_used=ACTIVE_MODEL,
        high_occupancy_next_hour=result["prediction"] == 1
    )



@app.get("/health")
def health():
    """
    Smoke-tests every loaded Pipeline with a known-good probe row.
    Spring Boot depends_on condition: service_healthy — this endpoint
    must return 200 before app1/app2 containers start.
    """
    probe = build_health_probe()
    statuses = {}
    for name, pipeline in models.items():
        try:
            pipeline.predict(probe)
            pipeline.predict_proba(probe)
            statuses[name] = "ok"
        except Exception as exc:
            statuses[name] = f"ERROR: {exc}"

    all_ok = all(v == "ok" for v in statuses.values())
    return {
        "status":       "ok" if all_ok else "degraded",
        "active_model": ACTIVE_MODEL,
        "models":       statuses,
    }
