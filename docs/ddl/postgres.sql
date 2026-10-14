-- Durable jobs schema, copied from core prime-durable-jobs.sql (QRun-IO/qqq#1178).
-- Snake-case columns; excludes test fixtures and destructive DROP statements.

CREATE TABLE jobs_job
(
   id                        SERIAL PRIMARY KEY,
   label                     VARCHAR(250),
   process_name              VARCHAR(250) NOT NULL,
   table_name                VARCHAR(250),
   mode                      VARCHAR(20),
   status                    VARCHAR(30) NOT NULL,
   status_reason             VARCHAR(500),
   priority                  INTEGER DEFAULT 5,
   user_id                   VARCHAR(250),
   client_channel            VARCHAR(250),
   credential_scope_json     TEXT,
   source_type               VARCHAR(30),
   source_json               TEXT,
   params_json               TEXT,
   idempotency               VARCHAR(30),
   semantics                 VARCHAR(20),
   chunk_size                INTEGER,
   max_parallel_chunks       INTEGER,
   rows_total                INTEGER,
   rows_done                 INTEGER,
   rows_ok                   INTEGER,
   rows_failed               INTEGER,
   chunks_total              INTEGER,
   chunks_done               INTEGER,
   chunks_failed             INTEGER,
   summary_json              TEXT,
   output_storage_json       TEXT,
   parent_job_id             INTEGER,
   saved_bulk_load_profile_id INTEGER,
   notify_email              BOOLEAN DEFAULT FALSE,
   queued_at                 TIMESTAMPTZ,
   started_at                TIMESTAMPTZ,
   finished_at               TIMESTAMPTZ,
   last_progress_at          TIMESTAMPTZ,
   error_message             TEXT,
   lease_version             INTEGER DEFAULT 0,
   create_date               TIMESTAMPTZ,
   modify_date               TIMESTAMPTZ
);

CREATE INDEX jobs_job_claim_idx ON jobs_job (status, priority, queued_at);

CREATE INDEX jobs_job_user_idx ON jobs_job (user_id, status);

CREATE TABLE jobs_chunk
(
   id                SERIAL PRIMARY KEY,
   job_id            INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   seq               INTEGER NOT NULL,
   status            VARCHAR(30) NOT NULL,
   range_json        TEXT,
   input_storage_ref VARCHAR(1000),
   attempt           INTEGER DEFAULT 0,
   max_attempts      INTEGER DEFAULT 3,
   lease_owner       VARCHAR(250),
   lease_token       VARCHAR(100),
   lease_expires_at  TIMESTAMPTZ,
   heartbeat_at      TIMESTAMPTZ,
   rows_in           INTEGER,
   rows_ok           INTEGER,
   rows_failed       INTEGER,
   summary_json      TEXT,
   checkpoint_json   TEXT,
   error_message     TEXT,
   started_at        TIMESTAMPTZ,
   finished_at       TIMESTAMPTZ,
   lease_version     INTEGER DEFAULT 0,
   create_date       TIMESTAMPTZ,
   modify_date       TIMESTAMPTZ,
   UNIQUE (job_id, seq)
);

CREATE INDEX jobs_chunk_status_idx ON jobs_chunk (job_id, status);

CREATE INDEX jobs_chunk_lease_idx ON jobs_chunk (status, lease_expires_at);

CREATE TABLE jobs_row_error
(
   id           SERIAL PRIMARY KEY,
   job_id       INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   chunk_id     INTEGER,
   row_number   INTEGER,
   record_pk    VARCHAR(250),
   field_name   VARCHAR(250),
   error_code   VARCHAR(100),
   message      TEXT,
   retry_job_id INTEGER,
   resolved     BOOLEAN DEFAULT FALSE,
   create_date  TIMESTAMPTZ
);

CREATE INDEX jobs_row_error_job_idx ON jobs_row_error (job_id);

-- STAND-IN(core=QRun-IO/qqq#1191, removeBy=RC.7): transactional outbox.
CREATE TABLE jobs_effect
(
   id                 SERIAL PRIMARY KEY,
   job_id             INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   effect_name        VARCHAR(100),
   status             VARCHAR(30),
   needs_review       BOOLEAN DEFAULT FALSE,
   row_key            VARCHAR(250),
   target_name        VARCHAR(100),
   effect_type        VARCHAR(30),
   chunk_id           INTEGER,
   chunk_seq          INTEGER,
   idempotency_key    VARCHAR(500),
   payload_json       TEXT,
   payload_ref        VARCHAR(1000),
   attempt            INTEGER DEFAULT 0,
   max_attempts       INTEGER,
   reconcile_attempts INTEGER DEFAULT 0,
   next_attempt_at    TIMESTAMPTZ,
   lease_owner        VARCHAR(250),
   lease_token        VARCHAR(100),
   lease_expires_at   TIMESTAMPTZ,
   lease_version      INTEGER DEFAULT 0,
   provider_ref       VARCHAR(500),
   last_error         TEXT,
   sent_at            TIMESTAMPTZ,
   create_date        TIMESTAMPTZ,
   modify_date        TIMESTAMPTZ
);

CREATE UNIQUE INDEX jobs_effect_idempotency_idx ON jobs_effect (idempotency_key);

CREATE INDEX jobs_effect_status_idx ON jobs_effect (status, next_attempt_at);

CREATE INDEX jobs_effect_job_idx ON jobs_effect (job_id);

CREATE TABLE jobs_event
(
   id             SERIAL PRIMARY KEY,
   job_id         INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   chunk_id       INTEGER,
   type           VARCHAR(30),
   actor_user_id  VARCHAR(250),
   client_channel VARCHAR(250),
   pod            VARCHAR(250),
   detail         TEXT,
   create_date    TIMESTAMPTZ
);

CREATE INDEX jobs_event_job_idx ON jobs_event (job_id);

CREATE TABLE jobs_worker
(
   id           SERIAL PRIMARY KEY,
   pod_id       VARCHAR(250) NOT NULL UNIQUE,
   started_at   TIMESTAMPTZ,
   heartbeat_at TIMESTAMPTZ,
   threads      INTEGER,
   draining     BOOLEAN,
   version      VARCHAR(100)
);

CREATE TABLE jobs_upload
(
   id                 SERIAL PRIMARY KEY,
   ref                VARCHAR(100) NOT NULL UNIQUE,
   user_id            VARCHAR(250),
   file_name          VARCHAR(250),
   content_type       VARCHAR(250),
   size_bytes         BIGINT,
   storage_table_name VARCHAR(250),
   storage_reference  VARCHAR(1000),
   expires_at         TIMESTAMPTZ,
   job_id             INTEGER,
   create_date        TIMESTAMPTZ
);
