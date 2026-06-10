# =============================================================================
# ml-service/Dockerfile
# =============================================================================
FROM python:3.11-slim

# Install curl for the Docker healthcheck only
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir --default-timeout=120 --retries 10 -r requirements.txt
# Copy application code (models are mounted via volume in docker-compose)
COPY main.py .
COPY preprocessing/ ./preprocessing/
COPY schemas/       ./schemas/

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
