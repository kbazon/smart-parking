# Smart Parking System

Smart Parking System je data-intenzivna web aplikacija za upravljanje parkiralištem.
Sustav omogućuje evidenciju ulazaka i izlazaka vozila, praćenje dostupnosti parkirnih
mjesta u stvarnom vremenu te generiranje analitičkih izvještaja.

Projekt je izrađen u sklopu kolegija **Infrastruktura za podatke velikog obujma (IPVO)**.

---

## Tehnologije
- Java 25
- Spring Boot
- PostgreSQL (Primary + Read Replica)
- Redis (Cache / Event processing)
- Nginx (Load balancer)
- Docker
- Maven

---

## Funkcionalnosti

### Faza 1 – Osnovni sustav parkiranja
- Generiranje parkirne karte pri ulazu (UUID, vrijeme ulaska)
- Izlaz iz parkirališta s automatskim obračunom cijene
- REST API
- Relacijska baza podataka (PostgreSQL)
- Load balancer i replikacija baze

---

### Faza 2 – Praćenje dostupnosti parkirnih mjesta
- Praćenje broja slobodnih parkirnih mjesta u stvarnom vremenu
- Redis kao in-memory predmemorija
- Obrada događaja (ulaz / izlaz) i ažuriranje stanja dostupnosti

---

### Faza 3 – Analitika i izvještaji
- Batch obrada podataka
- Dnevni i mjesečni izvještaji
- Izračun:
  - najopterećenijih sati
  - postotka zauzetosti
  - prosječnog trajanja parkiranja
  - ukupnog prihoda

---

## Pokretanje projekta

1. Pri prvom pokretanju potrebno je stvoriti .env datoteku unutar "docker" foldera koja ima sljedeći sadržaj:

"
POSTGRES_DB=smart_parking
POSTGRES_USER=parking_user
POSTGRES_PASSWORD=your_password

REPLICA_PASSWORD=your_replica_password

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres_primary:5432/smart_parking
SPRING_DATASOURCE_USERNAME=parking_user
SPRING_DATASOURCE_PASSWORD=your_password

SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
"

2. Unutar docker foldera u projektu izvršiti sljedeću naredbu : "docker compose up -d --build"
3. Aplikacija je dostupna na: http://localhost:8080