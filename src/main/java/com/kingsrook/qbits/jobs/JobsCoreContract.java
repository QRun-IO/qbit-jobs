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
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaData;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaDataProvider;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsTables;
import com.kingsrook.qqq.backend.core.jobs.effects.EffectStatus;
import com.kingsrook.qqq.backend.core.jobs.effects.JobEffectType;
import com.kingsrook.qqq.backend.core.jobs.model.ChunkStatus;
import com.kingsrook.qqq.backend.core.jobs.model.JobStatus;
import com.kingsrook.qqq.backend.core.jobs.runtime.JobSignals;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;


/*******************************************************************************
 ** Core identifiers and field names used by the Jobs app.
 *******************************************************************************/
public final class JobsCoreContract
{
   public static final String JOBS_JOB = DurableJobsTables.TABLE_JOB;
   public static final String JOBS_CHUNK = DurableJobsTables.TABLE_CHUNK;
   public static final String JOBS_ROW_ERROR = DurableJobsTables.TABLE_ROW_ERROR;
   public static final String JOBS_EVENT = DurableJobsTables.TABLE_EVENT;
   public static final String JOBS_WORKER = DurableJobsTables.TABLE_WORKER;
   public static final String JOBS_UPLOAD = DurableJobsTables.TABLE_UPLOAD;
   public static final String JOBS_USER = DurableJobsTables.SECURITY_KEY_USER;
   public static final String JOBS_USER_ALL_ACCESS = DurableJobsTables.SECURITY_KEY_USER_ALL_ACCESS;
   public static final String JOBS_ADMIN = DurableJobsTables.PERMISSION_ADMIN;
   public static final String ALERT_JOB_FAILED = JobSignals.ALERT_JOB_FAILED;
   public static final String ALERT_JOB_STALLED = JobSignals.ALERT_JOB_STALLED;
   public static final String HEARTBEAT_REAPER = JobSignals.HEARTBEAT_REAPER;
   public static final String DURABLE_JOBS_METADATA = DurableJobsMetaData.NAME;
   public static final String AUDIT = AuditsMetaDataProvider.TABLE_NAME_AUDIT;
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
   public static final String PLANNING = JobStatus.PLANNING.getPossibleValueId();
   public static final String AWAITING_CONFIRMATION = JobStatus.AWAITING_CONFIRMATION.getPossibleValueId();
   public static final String QUEUED = JobStatus.QUEUED.getPossibleValueId();
   public static final String RUNNING = JobStatus.RUNNING.getPossibleValueId();
   public static final String PAUSING = JobStatus.PAUSING.getPossibleValueId();
   public static final String PAUSED = JobStatus.PAUSED.getPossibleValueId();
   public static final String CANCELLING = JobStatus.CANCELLING.getPossibleValueId();
   public static final String CANCELLED = JobStatus.CANCELLED.getPossibleValueId();
   public static final String COMPLETED = JobStatus.COMPLETED.getPossibleValueId();
   public static final String COMPLETED_WITH_ERRORS = JobStatus.COMPLETED_WITH_ERRORS.getPossibleValueId();
   public static final String FAILED = JobStatus.FAILED.getPossibleValueId();
   public static final String NEEDS_REVIEW = JobStatus.NEEDS_REVIEW.getPossibleValueId();
   public static final String DONE = ChunkStatus.DONE.getPossibleValueId();

   public static final String JOBS_EFFECT = DurableJobsTables.TABLE_EFFECT;
   public static final String JOBS_EFFECT_STATUS = EffectStatus.NAME;
   public static final String JOBS_EFFECT_TYPE = JobEffectType.NAME;
   public static final String ALERT_EFFECT_UNRESOLVED = JobSignals.ALERT_EFFECT_UNRESOLVED;
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
   public static final String PENDING = EffectStatus.PENDING.getPossibleValueId();
   public static final String SENDING = EffectStatus.SENDING.getPossibleValueId();
   public static final String SENT = EffectStatus.SENT.getPossibleValueId();
   public static final String UNKNOWN = EffectStatus.UNKNOWN.getPossibleValueId();
   public static final String LOCAL_TX = JobEffectType.LOCAL_TX.getPossibleValueId();
   public static final String IDEMPOTENT_EXTERNAL = JobEffectType.IDEMPOTENT_EXTERNAL.getPossibleValueId();
   public static final String NON_IDEMPOTENT_EXTERNAL = JobEffectType.NON_IDEMPOTENT_EXTERNAL.getPossibleValueId();

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
      return (DurableJobsMetaDataProvider.childJoinName(child));
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
      try
      {
         return (JobStatus.valueOf(status).getPossibleValueLabel());
      }
      catch(IllegalArgumentException e)
      {
         return (status);
      }
   }
}
