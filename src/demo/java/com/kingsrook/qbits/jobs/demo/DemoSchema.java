/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.kingsrook.qbits.jobs.demo;


/*******************************************************************************
 ** The demo's H2 schema: core's jobs_* tables (snake_case columns, from the
 ** Postgres DDL in docs/ddl/postgres.sql) and the demo customer table.
 *******************************************************************************/
final class DemoSchema
{
   static final String H2_DDL = """
CREATE TABLE jobs_job
(
   id                        INT AUTO_INCREMENT PRIMARY KEY,
   label                     VARCHAR(250),
   process_name              VARCHAR(250) NOT NULL,
   table_name                VARCHAR(250),
   mode                      VARCHAR(20),
   status                    VARCHAR(30) NOT NULL,
   status_reason             VARCHAR(500),
   priority                  INTEGER DEFAULT 5,
   user_id                   VARCHAR(250),
   client_channel            VARCHAR(250),
   credential_scope_json     VARCHAR(1000000),
   source_type               VARCHAR(30),
   source_json               VARCHAR(1000000),
   params_json               VARCHAR(1000000),
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
   summary_json              VARCHAR(1000000),
   output_storage_json       VARCHAR(1000000),
   parent_job_id             INTEGER,
   saved_bulk_load_profile_id INTEGER,
   notify_email              BOOLEAN DEFAULT FALSE,
   queued_at                 TIMESTAMP WITH TIME ZONE,
   started_at                TIMESTAMP WITH TIME ZONE,
   finished_at               TIMESTAMP WITH TIME ZONE,
   last_progress_at          TIMESTAMP WITH TIME ZONE,
   error_message             VARCHAR(1000000),
   lease_version             INTEGER DEFAULT 0,
   create_date               TIMESTAMP WITH TIME ZONE,
   modify_date               TIMESTAMP WITH TIME ZONE
);

CREATE INDEX jobs_job_claim_idx ON jobs_job (status, priority, queued_at);

CREATE INDEX jobs_job_user_idx ON jobs_job (user_id, status);

CREATE TABLE jobs_chunk
(
   id                INT AUTO_INCREMENT PRIMARY KEY,
   job_id            INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   seq               INTEGER NOT NULL,
   status            VARCHAR(30) NOT NULL,
   range_json        VARCHAR(1000000),
   input_storage_ref VARCHAR(1000),
   attempt           INTEGER DEFAULT 0,
   max_attempts      INTEGER DEFAULT 3,
   lease_owner       VARCHAR(250),
   lease_token       VARCHAR(100),
   lease_expires_at  TIMESTAMP WITH TIME ZONE,
   heartbeat_at      TIMESTAMP WITH TIME ZONE,
   rows_in           INTEGER,
   rows_ok           INTEGER,
   rows_failed       INTEGER,
   summary_json      VARCHAR(1000000),
   checkpoint_json   VARCHAR(1000000),
   error_message     VARCHAR(1000000),
   started_at        TIMESTAMP WITH TIME ZONE,
   finished_at       TIMESTAMP WITH TIME ZONE,
   lease_version     INTEGER DEFAULT 0,
   create_date       TIMESTAMP WITH TIME ZONE,
   modify_date       TIMESTAMP WITH TIME ZONE,
   UNIQUE (job_id, seq)
);

CREATE INDEX jobs_chunk_status_idx ON jobs_chunk (job_id, status);

CREATE INDEX jobs_chunk_lease_idx ON jobs_chunk (status, lease_expires_at);

CREATE TABLE jobs_row_error
(
   id           INT AUTO_INCREMENT PRIMARY KEY,
   job_id       INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   chunk_id     INTEGER,
   row_number   INTEGER,
   record_pk    VARCHAR(250),
   field_name   VARCHAR(250),
   error_code   VARCHAR(100),
   message      VARCHAR(1000000),
   retry_job_id INTEGER,
   resolved     BOOLEAN DEFAULT FALSE,
   create_date  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX jobs_row_error_job_idx ON jobs_row_error (job_id);

CREATE TABLE jobs_event
(
   id             INT AUTO_INCREMENT PRIMARY KEY,
   job_id         INTEGER NOT NULL REFERENCES jobs_job (id) ON DELETE CASCADE,
   chunk_id       INTEGER,
   type           VARCHAR(30),
   actor_user_id  VARCHAR(250),
   client_channel VARCHAR(250),
   pod            VARCHAR(250),
   detail         VARCHAR(1000000),
   create_date    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX jobs_event_job_idx ON jobs_event (job_id);

CREATE TABLE jobs_worker
(
   id           INT AUTO_INCREMENT PRIMARY KEY,
   pod_id       VARCHAR(250) NOT NULL UNIQUE,
   started_at   TIMESTAMP WITH TIME ZONE,
   heartbeat_at TIMESTAMP WITH TIME ZONE,
   threads      INTEGER,
   draining     BOOLEAN,
   version      VARCHAR(100)
);

CREATE TABLE jobs_storage
(
   reference VARCHAR(1000) PRIMARY KEY,
   contents  BYTEA
);

CREATE TABLE jobs_upload
(
   id                 INT AUTO_INCREMENT PRIMARY KEY,
   ref                VARCHAR(100) NOT NULL UNIQUE,
   user_id            VARCHAR(250),
   file_name          VARCHAR(250),
   content_type       VARCHAR(250),
   size_bytes         BIGINT,
   storage_table_name VARCHAR(250),
   storage_reference  VARCHAR(1000),
   expires_at         TIMESTAMP WITH TIME ZONE,
   job_id             INTEGER,
   create_date        TIMESTAMP WITH TIME ZONE
);

CREATE TABLE demo_customer
(
   id             INT AUTO_INCREMENT PRIMARY KEY,
   name           VARCHAR(250),
   email          VARCHAR(250),
   tier           VARCHAR(20),
   lifetime_value INTEGER
);
      """;



   /*******************************************************************************
    ** not instantiable
    *******************************************************************************/
   private DemoSchema()
   {
   }

}
