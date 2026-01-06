-- Ekstenzije
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Replication user (idempotent)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'replica_user') THEN
    CREATE ROLE replica_user WITH REPLICATION LOGIN PASSWORD 'replica_pass';
  END IF;
END$$;

-- Tablice
CREATE TABLE IF NOT EXISTS tickets (
    id SERIAL PRIMARY KEY,
    ticket_uuid UUID DEFAULT gen_random_uuid(),
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    price NUMERIC(10,2),
    paid BOOLEAN DEFAULT FALSE
);
