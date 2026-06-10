# Smart Parking System

Smart Parking System je data-intenzivna web aplikacija za upravljanje parkiralištem.
Aplikacija omogućuje evidenciju ulazaka i izlazaka vozila, praćenje dostupnosti
parkirnih mjesta u stvarnom vremenu te analitičku predikciju zauzetosti parkinga
pomoću integriranog XGBoost modela.

---

## Trenutni live data flow

```text
Manual ENTRY / EXIT event
→ TicketController
→ ParkingEventPublisher
→ Redis Pub/Sub channel
→ ParkingEventConsumer
→ Redis availability update
→ parking_occupancy_log snapshot
→ FeaturePreprocessorService
→ FastAPI ML Service
→ XGBoost next-hour forecast
→ Admin dashboard
```

Svaki uspješan `ENTRY` ili `EXIT` događaj ažurira trenutno stanje parkinga i zapisuje novi
occupancy snapshot u tablicu:

```text
parking_occupancy_log
```

---

## Tehnologije

### Backend
- Java 25
- Spring Boot
- Maven
- REST API

### Data storage i streaming
- PostgreSQL primary database
- PostgreSQL read replica
- Redis key-value store
- Redis Pub/Sub za obradu parking događaja
- Nginx load balancer

### Machine learning
- Python
- FastAPI
- scikit-learn pipeline
- XGBoost binary classifier
- joblib model serialization

### Deployment
- Docker
- Docker Compose

---

## Glavne funkcionalnosti

### 1. Osnovni sustav parkiranja
- Generiranje parkirne karte pri ulazu
- UUID identifikator karte
- Vrijeme ulaska
- Izlaz iz parkinga
- Automatski obračun cijene
- Pregled aktivnih i završenih karata

### 2. Praćenje dostupnosti parkinga
- Praćenje trenutnog broja slobodnih mjesta
- Praćenje trenutnog broja zauzetih mjesta
- Ukupni kapacitet parkinga
- Redis kao live state storage
- Event-driven update preko Redis Pub/Sub

### 3. Occupancy logging
Nakon svakog `ENTRY` i `EXIT` događaja sustav zapisuje novi snapshot u
`parking_occupancy_log`.

Snapshot sadrži:
- `lot_id`
- timestamp (`ts`)
- broj zauzetih mjesta (`occupied_count`)
- kapacitet (`capacity`)
- oznaku praznika (`is_holiday`)

Primjer:

```text
LOT_A | 2026-05-29 23:49:06 | occupied_count=101 | capacity=200 | is_holiday=0
```

### 4. Feature preprocessing
`FeaturePreprocessorService` čita najnovije occupancy snapshotove i priprema feature vector
koji mora odgovarati featureima korištenima pri treniranju modela.

Model koristi:
- `hour`
- `day_of_week`
- `is_holiday`
- `capacity`
- `occupied_count`
- `occupancy_rate_lot`
- `occupied_count_lag_1h`
- `occupied_count_lag_2h`
- `occupied_count_lag_3h`
- `occupancy_rate_lag_1h`
- `occupancy_rate_lag_2h`
- `occupancy_rate_lag_3h`

### 5. Machine learning predikcija
Integrirani model je XGBoost binary classifier.

Model ne predviđa trenutni broj zauzetih mjesta. Model predviđa vjerojatnost da će
`LOT_A` u sljedećem satu doseći visoku zauzetost.

Output modela:
- `prediction`
  - `0` = parking se ne predviđa kao visoko zauzet u sljedećem satu
  - `1` = parking se predviđa kao visoko zauzet u sljedećem satu
- `probability`
  - vjerojatnost klase `1`
- `model_used`
  - `xgboost`
- `prediction_horizon_minutes`
  - `60`
- `target_threshold`
  - `0.85`

Primjer odgovora:

```json
{
  "prediction": 1,
  "probability": 0.991,
  "model_used": "xgboost",
  "prediction_horizon_minutes": 60,
  "target_threshold": 0.85,
  "high_occupancy_next_hour": true
}
```

### 6. Admin dashboard
Dashboard prikazuje:
- trenutni kapacitet
- trenutno zauzeta mjesta
- trenutno slobodna mjesta
- XGBoost next-hour forecast za `LOT_A`
- vjerojatnost visoke zauzetosti u sljedećem satu
- manual `ENTRY` / `EXIT` kontrole za demonstraciju live event toka
- system status

Dashboard ne prikazuje arhitekturu sustava ni usporedbu modela, jer je namijenjen
administratoru parkinga, a ne tehničkoj dokumentaciji.

---

## Machine learning trening i evaluacija

Model je treniran kao binary classification problem.

Target varijabla u trening skripti definira se kao high occupancy u sljedećem satu:

```python
df["target_occupancy_rate_next_hour"] = df["occupancy_rate_lot"].shift(-1)

df["target_high_occupancy_next_hour"] = (
    df["target_occupancy_rate_next_hour"] >= 0.85
).astype(int)
```

To znači:

```text
Input: trenutno stanje + lag featurei iz prethodnih sati
Target: hoće li occupancy_rate_lot u sljedećem satu biti >= 0.85
```

Modeli su evaluirani offline pomoću skripte `compare.py`.
Uspoređeni su modeli za binary classification, a najbolji model integriran u aplikaciju je
XGBoost.

Za evaluaciju se koriste metrike:
- accuracy
- precision
- recall
- F1-score
- confusion matrix
- classification report

Napomena: točne vrijednosti metrika potrebno je navesti u završnoj dokumentaciji ili reportu
prema rezultatima iz `compare.py`.

---

## Arhitektura sustava

```text
Browser Dashboard
        |
        v
Nginx Load Balancer
        |
        v
Spring Boot backend instances
        |
        +--------------------+
        |                    |
        v                    v
 PostgreSQL primary       Redis
 analytical storage       live state + Pub/Sub
        |
        v
FeaturePreprocessorService
        |
        v
FastAPI ML Service
        |
        v
XGBoost model
```

---

## Baza podataka

Glavna relacijska baza je PostgreSQL.

Važne tablice:
- `tickets`
- `reports`
- `parking_occupancy_log`

`parking_occupancy_log` je ključna tablica za ML pipeline jer sadrži povijesne occupancy
snapshotove koji se pretvaraju u featuree za model.

---

## Redis

Redis se koristi za:
- trenutno stanje dostupnosti parkinga
- ukupan kapacitet
- Pub/Sub obradu `ENTRY` i `EXIT` događaja
- cacheiranje prediction rezultata

Važni Redis ključevi:
- `capacity:total`
- `availability:total:free`
- `prediction:LOT_A`

---

## API endpointi

### Tickets

```http
POST /tickets/entry
```

Kreira novu parking kartu i generira `ENTRY` event.

```http
PUT /tickets/exit/{ticketUuid}
```

Zatvara parking kartu i generira `EXIT` event.

```http
GET /tickets
```

Vraća listu karata.

### Availability

```http
GET /availability/total
```

Vraća trenutno stanje parkinga:

```json
{
  "capacity": 200,
  "free": 100,
  "occupied": 100
}
```

### ML prediction

```http
GET /api/parking/predict/LOT_A?refresh=true
```

Pokreće svježu XGBoost predikciju za `LOT_A`.

```http
POST /api/parking/predict/LOT_A/refresh
```

Eksplicitno pokreće full pipeline:
DB snapshotovi → featurei → ML service → prediction.

### ML service health

```http
GET http://localhost:8000/health
```

Primjer:

```json
{
  "status": "ok",
  "active_model": "xgboost",
  "models": {
    "xgboost": "ok"
  }
}
```

---

## Pokretanje projekta

### 1. Kreirati `.env` datoteku

U folderu `docker` kreirati `.env` datoteku:

```env
POSTGRES_DB=smart_parking
POSTGRES_USER=parking_user
POSTGRES_PASSWORD=your_password

REPLICA_PASSWORD=your_replica_password

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres_primary:5432/smart_parking
SPRING_DATASOURCE_USERNAME=parking_user
SPRING_DATASOURCE_PASSWORD=your_password

SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
```

### 2. Pokrenuti Docker Compose

Iz foldera `docker`:

```bash
docker compose up -d --build
```

### 3. Otvoriti aplikaciju

```text
http://localhost:8080
```

### 4. Provjeriti ML service

```bash
curl http://localhost:8000/health
```

Očekivano:

```json
{
  "status": "ok",
  "active_model": "xgboost",
  "models": {
    "xgboost": "ok"
  }
}
```

---

## Testiranje live data pipelinea

### 1. Napraviti ENTRY

```bash
curl -X POST http://localhost:8080/tickets/entry
```

### 2. Provjeriti availability

```bash
curl http://localhost:8080/availability/total
```

### 3. Provjeriti occupancy log u bazi

```bash
docker exec -it postgres_primary psql -U parking_user -d smart_parking -c "SELECT id, lot_id, ts, occupied_count, capacity, is_holiday FROM parking_occupancy_log ORDER BY ts DESC LIMIT 10;"
```

### 4. Pokrenuti prediction

```bash
curl "http://localhost:8080/api/parking/predict/LOT_A?refresh=true"
```

### 5. Napraviti EXIT

Prvo pronaći aktivni ticket:

```bash
curl http://localhost:8080/tickets
```

Zatim zatvoriti ticket:

```bash
curl -X PUT http://localhost:8080/tickets/exit/{ticketUuid}
```

Nakon toga se u `parking_occupancy_log` mora pojaviti novi snapshot s ažuriranim brojem
zauzetih mjesta.

---

## Demonstracija ML funkcionalnosti

Za demonstraciju se mogu ručno generirati `ENTRY` i `EXIT` događaji preko dashboarda.
U stvarnoj implementaciji isti endpointi mogli bi biti povezani s rampom, kamerom,
senzorom ili vanjskim ticketing sustavom.

Primjer demo scenarija:
1. Administrator klikne `ENTRY`.
2. Backend kreira ticket.
3. Redis event se obradi.
4. Redis availability se ažurira.
5. Novi snapshot se upisuje u `parking_occupancy_log`.
6. FeaturePreprocessorService priprema featuree.
7. XGBoost model vraća next-hour forecast.
8. Dashboard prikazuje probability i status.

Važno: probability koju model prikazuje nije trenutni postotak zauzetosti. To je vjerojatnost
da će parking u sljedećem satu biti u high-occupancy stanju.

---
