

import pandas as pd

# ── Column name contract ──────────────────────────────────────────────────────
# Order does NOT matter for a named DataFrame — ColumnTransformer selects
# by name — but keep this list accurate for the health-check probe below.
NUMERIC_COLS: list[str] = [
    "hour",
    "is_holiday",
    "capacity",
    "occupied_count",
    "occupancy_rate_lot",
    "occupied_count_lag_1h",
    "occupied_count_lag_2h",
    "occupied_count_lag_3h",
    "occupancy_rate_lag_1h",
    "occupancy_rate_lag_2h",
    "occupancy_rate_lag_3h",
]
CATEGORICAL_COLS: list[str] = ["day_of_week"]
ALL_COLS: list[str] = NUMERIC_COLS + CATEGORICAL_COLS


def build_feature_dataframe(req) -> pd.DataFrame:
    """
    Accepts a PredictRequest (Pydantic model) and returns a single-row
    DataFrame ready for Pipeline.predict() / Pipeline.predict_proba().

    No manual encoding is done here — the Pipeline's ColumnTransformer
    handles OneHotEncoding of day_of_week internally.
    """
    row = {
        "hour":                 int(req.hour),
        "is_holiday":            int(req.is_holiday),
        "capacity":              int(req.capacity),
        "occupied_count":        int(req.occupied_count),
        "occupancy_rate_lot":    float(req.occupancy_rate_lot),
        "occupied_count_lag_1h": int(req.occupied_count_lag_1h),
        "occupied_count_lag_2h": int(req.occupied_count_lag_2h),
        "occupied_count_lag_3h": int(req.occupied_count_lag_3h),
        "occupancy_rate_lag_1h": float(req.occupancy_rate_lag_1h),
        "occupancy_rate_lag_2h": float(req.occupancy_rate_lag_2h),
        "occupancy_rate_lag_3h": float(req.occupancy_rate_lag_3h),
        "day_of_week": str(req.day_of_week)
    }
    # Explicit column list preserves order for readability; Pipeline ignores order.
    return pd.DataFrame([row], columns=ALL_COLS)


def build_health_probe() -> pd.DataFrame:
    """Minimal valid row used by /health to smoke-test all three Pipelines."""
    return pd.DataFrame([{
        "hour":                  9,
        "is_holiday":            0,
        "capacity":              100,
        "occupied_count":        60,
        "occupancy_rate_lot":    0.60,
        "occupied_count_lag_1h": 55,
        "occupied_count_lag_2h": 50,
        "occupied_count_lag_3h": 45,
        "occupancy_rate_lag_1h": 0.55,
        "occupancy_rate_lag_2h": 0.50,
        "occupancy_rate_lag_3h": 0.45,
        "day_of_week":           "mon",
    }], columns=ALL_COLS)
