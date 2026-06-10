from pydantic import BaseModel, Field


class PredictRequest(BaseModel):
    # ── Numeric features (must match training column names exactly) ──────────
    hour:                   int   = Field(..., ge=0, le=23)
    is_holiday:             int   = Field(..., ge=0, le=1)
    capacity:               int   = Field(..., gt=0)
    occupied_count:         int   = Field(..., ge=0)
    occupancy_rate_lot:     float = Field(..., ge=0.0, le=1.0)
    occupied_count_lag_1h:  int   = Field(..., ge=0)
    occupied_count_lag_2h:  int   = Field(..., ge=0)
    occupied_count_lag_3h:  int   = Field(..., ge=0)
    occupancy_rate_lag_1h:  float = Field(..., ge=0.0, le=1.0)
    occupancy_rate_lag_2h:  float = Field(..., ge=0.0, le=1.0)
    occupancy_rate_lag_3h:  float = Field(..., ge=0.0, le=1.0)
    
    day_of_week: str = Field(
		...,
		description="e.g. 'mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'"
	)

    # ── Categorical feature (OHE inside Pipeline) ────────────────────────────
    # VERIFY: open your training script and check whether X_train["day_of_week"]
    # contained strings ("Monday") or integers (0). Match that exactly here.
    # Default assumption: string day names from pd.Timestamp.day_name()


class PredictResponse(BaseModel):
    prediction: int
    probability: float
    model_used: str

    prediction_horizon_minutes: int = 60
    target_threshold: float = 0.85
    target_description: str = "Probability that LOT_A occupancy will be at least 85% in the next hour"
    high_occupancy_next_hour: bool | None = None
