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


package com.kingsrook.qbits.jobs;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;


/*******************************************************************************
 ** STAND-IN(core=QRun-IO/qqq#1178, removeBy=RC.7): mirrors core DurableJobsTables and friends; switch to core's constants when RC.7 is released.
 *******************************************************************************/
public final class JobsCoreContract
{
   public static final String JOBS_JOB = "jobs_job";
   public static final String JOBS_CHUNK = "jobs_chunk";
   public static final String JOBS_ROW_ERROR = "jobs_row_error";
   public static final String JOBS_EVENT = "jobs_event";
   public static final String JOBS_WORKER = "jobs_worker";
   public static final String JOBS_UPLOAD = "jobs_upload";
   public static final String JOBS_USER = "jobs_user";
   public static final String JOBS_USER_ALL_ACCESS = "jobs_user_all_access";
   public static final String JOBS_ADMIN = "jobs_admin";
   public static final String ALERT_JOB_FAILED = "jobs.job-failed";
   public static final String ALERT_JOB_STALLED = "jobs.job-stalled";
   public static final String HEARTBEAT_REAPER = "jobs.reaper";
   public static final String DURABLE_JOBS_METADATA = "durableJobs";
   public static final String AUDIT = "audit";
   public static final String ID = "id";
   public static final String LABEL = "label";
   public static final String PROCESS_NAME = "processName";
   public static final String TABLE_NAME = "tableName";
   public static final String MODE = "mode";
   public static final String STATUS = "status";
   public static final String STATUS_REASON = "statusReason";
   public static final String PRIORITY = "priority";
   public static final String USER_ID = "userId";
   public static final String CLIENT_CHANNEL = "clientChannel";
   public static final String CREDENTIAL_SCOPE_JSON = "credentialScopeJson";
   public static final String SOURCE_TYPE = "sourceType";
   public static final String SOURCE_JSON = "sourceJson";
   public static final String PARAMS_JSON = "paramsJson";
   public static final String IDEMPOTENCY = "idempotency";
   public static final String SEMANTICS = "semantics";
   public static final String CHUNK_SIZE = "chunkSize";
   public static final String MAX_PARALLEL_CHUNKS = "maxParallelChunks";
   public static final String ROWS_TOTAL = "rowsTotal";
   public static final String ROWS_DONE = "rowsDone";
   public static final String ROWS_OK = "rowsOk";
   public static final String ROWS_FAILED = "rowsFailed";
   public static final String CHUNKS_TOTAL = "chunksTotal";
   public static final String CHUNKS_DONE = "chunksDone";
   public static final String CHUNKS_FAILED = "chunksFailed";
   public static final String SUMMARY_JSON = "summaryJson";
   public static final String OUTPUT_STORAGE_JSON = "outputStorageJson";
   public static final String PARENT_JOB_ID = "parentJobId";
   public static final String SAVED_BULK_LOAD_PROFILE_ID = "savedBulkLoadProfileId";
   public static final String NOTIFY_EMAIL = "notifyEmail";
   public static final String QUEUED_AT = "queuedAt";
   public static final String STARTED_AT = "startedAt";
   public static final String FINISHED_AT = "finishedAt";
   public static final String LAST_PROGRESS_AT = "lastProgressAt";
   public static final String ERROR_MESSAGE = "errorMessage";
   public static final String LEASE_VERSION = "leaseVersion";
   public static final String CREATE_DATE = "createDate";
   public static final String MODIFY_DATE = "modifyDate";
   public static final String JOB_ID = "jobId";
   public static final String SEQ = "seq";
   public static final String RANGE_JSON = "rangeJson";
   public static final String INPUT_STORAGE_REF = "inputStorageRef";
   public static final String ATTEMPT = "attempt";
   public static final String MAX_ATTEMPTS = "maxAttempts";
   public static final String LEASE_OWNER = "leaseOwner";
   public static final String LEASE_TOKEN = "leaseToken";
   public static final String LEASE_EXPIRES_AT = "leaseExpiresAt";
   public static final String HEARTBEAT_AT = "heartbeatAt";
   public static final String ROWS_IN = "rowsIn";
   public static final String CHECKPOINT_JSON = "checkpointJson";
   public static final String CHUNK_ID = "chunkId";
   public static final String ROW_NUMBER = "rowNumber";
   public static final String RECORD_PK = "recordPk";
   public static final String FIELD_NAME = "fieldName";
   public static final String ERROR_CODE = "errorCode";
   public static final String MESSAGE = "message";
   public static final String RETRY_JOB_ID = "retryJobId";
   public static final String RESOLVED = "resolved";
   public static final String TYPE = "type";
   public static final String ACTOR_USER_ID = "actorUserId";
   public static final String POD = "pod";
   public static final String DETAIL = "detail";
   public static final String POD_ID = "podId";
   public static final String THREADS = "threads";
   public static final String DRAINING = "draining";
   public static final String VERSION = "version";
   public static final String REF = "ref";
   public static final String FILE_NAME = "fileName";
   public static final String CONTENT_TYPE = "contentType";
   public static final String SIZE_BYTES = "sizeBytes";
   public static final String STORAGE_TABLE_NAME = "storageTableName";
   public static final String STORAGE_REFERENCE = "storageReference";
   public static final String EXPIRES_AT = "expiresAt";
   public static final String PLANNING = "PLANNING";
   public static final String AWAITING_CONFIRMATION = "AWAITING_CONFIRMATION";
   public static final String QUEUED = "QUEUED";
   public static final String RUNNING = "RUNNING";
   public static final String PAUSING = "PAUSING";
   public static final String PAUSED = "PAUSED";
   public static final String CANCELLING = "CANCELLING";
   public static final String CANCELLED = "CANCELLED";
   public static final String COMPLETED = "COMPLETED";
   public static final String COMPLETED_WITH_ERRORS = "COMPLETED_WITH_ERRORS";
   public static final String FAILED = "FAILED";
   public static final String NEEDS_REVIEW = "NEEDS_REVIEW";
   public static final String DONE = "DONE";

   // STAND-IN(core=QRun-IO/qqq#1191, removeBy=RC.7): transactional outbox contract.
   public static final String JOBS_EFFECT = "jobs_effect";
   public static final String JOBS_EFFECT_STATUS = "jobs_effect_status";
   public static final String JOBS_EFFECT_TYPE = "jobs_effect_type";
   public static final String ALERT_EFFECT_UNRESOLVED = "jobs.effect-unresolved";
   public static final String EFFECT_NAME = "effectName";
   public static final String NEEDS_REVIEW_FIELD = "needsReview";
   public static final String ROW_KEY = "rowKey";
   public static final String TARGET_NAME = "targetName";
   public static final String EFFECT_TYPE = "effectType";
   public static final String CHUNK_SEQ = "chunkSeq";
   public static final String IDEMPOTENCY_KEY = "idempotencyKey";
   public static final String PAYLOAD_JSON = "payloadJson";
   public static final String PAYLOAD_REF = "payloadRef";
   public static final String RECONCILE_ATTEMPTS = "reconcileAttempts";
   public static final String NEXT_ATTEMPT_AT = "nextAttemptAt";
   public static final String PROVIDER_REF = "providerRef";
   public static final String LAST_ERROR = "lastError";
   public static final String SENT_AT = "sentAt";
   public static final String PENDING = "PENDING";
   public static final String SENDING = "SENDING";
   public static final String SENT = "SENT";
   public static final String UNKNOWN = "UNKNOWN";
   public static final String LOCAL_TX = "LOCAL_TX";
   public static final String IDEMPOTENT_EXTERNAL = "IDEMPOTENT_EXTERNAL";
   public static final String NON_IDEMPOTENT_EXTERNAL = "NON_IDEMPOTENT_EXTERNAL";

   public static final List<String> DATA_TABLES = List.of(JOBS_JOB, JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EFFECT, JOBS_EVENT, JOBS_WORKER);
   public static final List<String> ALL_TABLES = List.of(JOBS_JOB, JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EVENT, JOBS_WORKER, JOBS_UPLOAD);
   public static final List<String> RUNNING_STATUSES = List.of(RUNNING, PAUSING, CANCELLING, PLANNING);
   public static final List<String> QUEUED_STATUSES = List.of(QUEUED, AWAITING_CONFIRMATION);
   public static final List<String> ATTENTION_STATUSES = List.of(FAILED, NEEDS_REVIEW, COMPLETED_WITH_ERRORS);
   public static final List<String> TERMINAL_STATUSES = List.of(COMPLETED, COMPLETED_WITH_ERRORS, FAILED, CANCELLED);

   /*******************************************************************************
    ** Constants only.
    *******************************************************************************/
   private JobsCoreContract()
   {
   }

   /*******************************************************************************
    ** The core's inferred child join name.
    *******************************************************************************/
   public static String childJoin(String child)
   {
      return (QJoinMetaData.makeInferredJoinName(JOBS_JOB, child));
   }

   /*******************************************************************************
    ** The display label of a job status, as core's JobStatus labels it (the
    ** record screens show the same words). Unknown values pass through.
    *******************************************************************************/
   public static String statusLabel(String status)
   {
      if(status == null)
      {
         return ("Unknown");
      }
      return (switch(status)
      {
         case AWAITING_CONFIRMATION -> "Awaiting Confirmation";
         case QUEUED -> "Queued";
         case PLANNING -> "Planning";
         case RUNNING -> "Running";
         case PAUSING -> "Pausing";
         case PAUSED -> "Paused";
         case CANCELLING -> "Cancelling";
         case CANCELLED -> "Cancelled";
         case COMPLETED -> "Completed";
         case COMPLETED_WITH_ERRORS -> "Completed With Errors";
         case FAILED -> "Failed";
         case NEEDS_REVIEW -> "Needs Review";
         default -> status;
      });
   }
}
