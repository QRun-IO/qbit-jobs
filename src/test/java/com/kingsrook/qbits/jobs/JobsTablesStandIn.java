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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ACTOR_USER_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ATTEMPT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHECKPOINT_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNK_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNK_SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNK_SIZE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CLIENT_CHANNEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CONTENT_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CREATE_DATE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CREDENTIAL_SCOPE_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DETAIL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DRAINING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.EFFECT_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.EFFECT_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ERROR_CODE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ERROR_MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.EXPIRES_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FIELD_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FILE_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FINISHED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.HEARTBEAT_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.IDEMPOTENCY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.IDEMPOTENCY_KEY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.IDEMPOTENT_EXTERNAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.INPUT_STORAGE_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT_STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_UPLOAD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER_ALL_ACCESS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_WORKER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LABEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LAST_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LAST_PROGRESS_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEASE_EXPIRES_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEASE_OWNER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEASE_TOKEN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEASE_VERSION;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LOCAL_TX;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MAX_ATTEMPTS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MAX_PARALLEL_CHUNKS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MODE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MODIFY_DATE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEXT_ATTEMPT_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NON_IDEMPOTENT_EXTERNAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NOTIFY_EMAIL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.OUTPUT_STORAGE_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PARAMS_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PARENT_JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PAYLOAD_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PAYLOAD_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.POD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.POD_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PRIORITY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROCESS_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROVIDER_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RANGE_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RECONCILE_ATTEMPTS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RECORD_PK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RESOLVED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RETRY_JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_IN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_OK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROW_KEY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROW_NUMBER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SAVED_BULK_LOAD_PROFILE_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEMANTICS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SIZE_BYTES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SOURCE_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SOURCE_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STARTED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS_REASON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_REFERENCE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_TABLE_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SUMMARY_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TABLE_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TARGET_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.THREADS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.UNKNOWN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.USER_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.VERSION;


/*******************************************************************************
 ** STAND-IN(core=QRun-IO/qqq#1178, removeBy=RC.6): test-only mirror of the core
 ** tables, field types, primary keys, owner locks and child joins. Includes uploads
 ** so retention is exercised against the complete contract.
 *******************************************************************************/
public final class JobsTablesStandIn
{
   /*******************************************************************************
    ** No instances.
    *******************************************************************************/
   private JobsTablesStandIn()
   {
   }

   /*******************************************************************************
    ** Define the core contract on a memory backend without importing core.jobs.
    *******************************************************************************/
   public static void defineAll(QInstance instance) throws QException
   {
      instance.addSecurityKeyType(new QSecurityKeyType().withName(JOBS_USER).withAllAccessKeyName(JOBS_USER_ALL_ACCESS));
      instance.addTable(new QTableMetaData().withName(JOBS_JOB).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(LABEL, QFieldType.STRING).withIsEditable(true))
         .withField(new QFieldMetaData(PROCESS_NAME, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(TABLE_NAME, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(MODE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(STATUS, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(STATUS_REASON, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(PRIORITY, QFieldType.INTEGER).withIsEditable(true))
         .withField(new QFieldMetaData(USER_ID, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(CLIENT_CHANNEL, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(CREDENTIAL_SCOPE_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(SOURCE_TYPE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(SOURCE_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(PARAMS_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(IDEMPOTENCY, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(SEMANTICS, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNK_SIZE, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(MAX_PARALLEL_CHUNKS, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_TOTAL, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_DONE, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_OK, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_FAILED, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNKS_TOTAL, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNKS_DONE, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNKS_FAILED, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(SUMMARY_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(OUTPUT_STORAGE_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(PARENT_JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(SAVED_BULK_LOAD_PROFILE_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(NOTIFY_EMAIL, QFieldType.BOOLEAN).withIsEditable(true))
         .withField(new QFieldMetaData(QUEUED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(STARTED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(FINISHED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(LAST_PROGRESS_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(ERROR_MESSAGE, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_VERSION, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(MODIFY_DATE, QFieldType.DATE_TIME).withIsEditable(false)));
      instance.addTable(new QTableMetaData().withName(JOBS_CHUNK).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(SEQ, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(STATUS, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(RANGE_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(INPUT_STORAGE_REF, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(ATTEMPT, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(MAX_ATTEMPTS, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_OWNER, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_TOKEN, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_EXPIRES_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(HEARTBEAT_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_IN, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_OK, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROWS_FAILED, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(SUMMARY_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(CHECKPOINT_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(ERROR_MESSAGE, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(STARTED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(FINISHED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_VERSION, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(MODIFY_DATE, QFieldType.DATE_TIME).withIsEditable(false)));
      instance.addTable(new QTableMetaData().withName(JOBS_ROW_ERROR).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNK_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(ROW_NUMBER, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(RECORD_PK, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(FIELD_NAME, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(ERROR_CODE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(MESSAGE, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(RETRY_JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(RESOLVED, QFieldType.BOOLEAN).withIsEditable(true))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false)));
      instance.addTable(new QTableMetaData().withName(JOBS_EVENT).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CHUNK_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(TYPE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(ACTOR_USER_ID, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(CLIENT_CHANNEL, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(POD, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(DETAIL, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false)));
      instance.addTable(new QTableMetaData().withName(JOBS_WORKER).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(POD_ID, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(STARTED_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(HEARTBEAT_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(THREADS, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(DRAINING, QFieldType.BOOLEAN).withIsEditable(false))
         .withField(new QFieldMetaData(VERSION, QFieldType.STRING).withIsEditable(false)));
      instance.addTable(new QTableMetaData().withName(JOBS_UPLOAD).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(REF, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(USER_ID, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(FILE_NAME, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(CONTENT_TYPE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(SIZE_BYTES, QFieldType.LONG).withIsEditable(false))
         .withField(new QFieldMetaData(STORAGE_TABLE_NAME, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(STORAGE_REFERENCE, QFieldType.STRING).withIsEditable(false))
         .withField(new QFieldMetaData(EXPIRES_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false)));
      defineEffects(instance);
      instance.getTable(JOBS_JOB).withSection(new QFieldSection()
         .withName("fields").withLabel("Job").withTier(Tier.T1)
         .withFieldNames(new ArrayList<>(instance.getTable(JOBS_JOB).getFields().keySet())));
      instance.getTable(JOBS_JOB).withRecordSecurityLock(lock(USER_ID, null));
      instance.getTable(JOBS_UPLOAD).withRecordSecurityLock(lock(USER_ID, null));
      for(String child : List.of(JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EFFECT, JOBS_EVENT))
      {
         String join = JobsCoreContract.childJoin(child);
         instance.addJoin(new QJoinMetaData().withLeftTable(JOBS_JOB).withRightTable(child).withType(JoinType.ONE_TO_MANY)
            .withJoinOn(new JoinOn(ID, JOB_ID)).withInferredName());
         instance.getTable(child).withRecordSecurityLock(lock(JOBS_JOB + "." + USER_ID, List.of(join)));
      }
      instance.getTable(JOBS_CHUNK).withUniqueKey(new UniqueKey(JOB_ID, SEQ));
      instance.getTable(JOBS_WORKER).withUniqueKey(new UniqueKey(POD_ID));
      instance.getTable(JOBS_UPLOAD).withUniqueKey(new UniqueKey(REF));
   }

   /*******************************************************************************
    ** STAND-IN(core=QRun-IO/qqq#1191, removeBy=RC.6): outbox fields and possible values.
    *******************************************************************************/
   private static void defineEffects(QInstance instance) throws QException
   {
      instance.addPossibleValueSource(new QPossibleValueSource().withName(JOBS_JOB).withType(QPossibleValueSourceType.TABLE).withTableName(JOBS_JOB));
      instance.addPossibleValueSource(new QPossibleValueSource().withName(JOBS_CHUNK).withType(QPossibleValueSourceType.TABLE).withTableName(JOBS_CHUNK));
      instance.addPossibleValueSource(new QPossibleValueSource().withName(JOBS_EFFECT_STATUS).withType(QPossibleValueSourceType.ENUM).withEnumValues(List.of(
         new QPossibleValue<>(PENDING, "Pending"), new QPossibleValue<>(SENDING, "Sending"), new QPossibleValue<>(SENT, "Sent"),
         new QPossibleValue<>(FAILED, "Failed"), new QPossibleValue<>(UNKNOWN, "Unknown"))));
      instance.addPossibleValueSource(new QPossibleValueSource().withName(JOBS_EFFECT_TYPE).withType(QPossibleValueSourceType.ENUM).withEnumValues(List.of(
         new QPossibleValue<>(LOCAL_TX, "Local Transaction"), new QPossibleValue<>(IDEMPOTENT_EXTERNAL, "Idempotent External"),
         new QPossibleValue<>(NON_IDEMPOTENT_EXTERNAL, "Non-Idempotent External"))));
      instance.addTable(new QTableMetaData().withName(JOBS_EFFECT).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER).withIsEditable(false).withPossibleValueSourceName(JOBS_JOB))
         .withField(new QFieldMetaData(EFFECT_NAME, QFieldType.STRING).withIsEditable(false).withMaxLength(100))
         .withField(new QFieldMetaData(STATUS, QFieldType.STRING).withIsEditable(false).withMaxLength(30).withPossibleValueSourceName(JOBS_EFFECT_STATUS))
         .withField(new QFieldMetaData(NEEDS_REVIEW_FIELD, QFieldType.BOOLEAN).withIsEditable(false).withDefaultValue(false))
         .withField(new QFieldMetaData(ROW_KEY, QFieldType.STRING).withIsEditable(false).withMaxLength(250))
         .withField(new QFieldMetaData(TARGET_NAME, QFieldType.STRING).withIsEditable(false).withMaxLength(100))
         .withField(new QFieldMetaData(EFFECT_TYPE, QFieldType.STRING).withIsEditable(false).withMaxLength(30).withPossibleValueSourceName(JOBS_EFFECT_TYPE))
         .withField(new QFieldMetaData(CHUNK_ID, QFieldType.INTEGER).withIsEditable(false).withPossibleValueSourceName(JOBS_CHUNK))
         .withField(new QFieldMetaData(CHUNK_SEQ, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(IDEMPOTENCY_KEY, QFieldType.STRING).withIsEditable(false).withMaxLength(500))
         .withField(new QFieldMetaData(PAYLOAD_JSON, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(PAYLOAD_REF, QFieldType.STRING).withIsEditable(false).withMaxLength(1000))
         .withField(new QFieldMetaData(ATTEMPT, QFieldType.INTEGER).withIsEditable(false).withDefaultValue(0))
         .withField(new QFieldMetaData(MAX_ATTEMPTS, QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData(RECONCILE_ATTEMPTS, QFieldType.INTEGER).withIsEditable(false).withDefaultValue(0))
         .withField(new QFieldMetaData(NEXT_ATTEMPT_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_OWNER, QFieldType.STRING).withIsEditable(false).withMaxLength(250))
         .withField(new QFieldMetaData(LEASE_TOKEN, QFieldType.STRING).withIsEditable(false).withMaxLength(100))
         .withField(new QFieldMetaData(LEASE_EXPIRES_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(LEASE_VERSION, QFieldType.INTEGER).withIsEditable(false).withDefaultValue(0))
         .withField(new QFieldMetaData(PROVIDER_REF, QFieldType.STRING).withIsEditable(false).withMaxLength(500))
         .withField(new QFieldMetaData(LAST_ERROR, QFieldType.TEXT).withIsEditable(false))
         .withField(new QFieldMetaData(SENT_AT, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(CREATE_DATE, QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData(MODIFY_DATE, QFieldType.DATE_TIME).withIsEditable(false))
         .withUniqueKey(new UniqueKey(IDEMPOTENCY_KEY)));
   }

   /*******************************************************************************
    ** The same read/write owner lock as the kernel, including join traversal.
    *******************************************************************************/
   private static RecordSecurityLock lock(String field, List<String> joins)
   {
      RecordSecurityLock lock = new RecordSecurityLock().withSecurityKeyType(JOBS_USER).withFieldName(field)
         .withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE);
      if(joins != null)
      {
         lock.withJoinNameChain(joins);
      }
      return (lock);
   }
}
