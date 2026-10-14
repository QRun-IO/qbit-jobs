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


import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QWidgetData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static com.kingsrook.qbits.jobs.JobsCoreContract.AUDIT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.AWAITING_CONFIRMATION;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CANCELLED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CANCELLING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED_WITH_ERRORS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DETAIL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DRAINING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.EFFECT_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.EFFECT_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ERROR_MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FINISHED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.HEARTBEAT_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.IDEMPOTENCY_KEY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.IDEMPOTENT_EXTERNAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.INPUT_STORAGE_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ADMIN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_UPLOAD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_WORKER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LABEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LAST_PROGRESS_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PAUSED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PAUSING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PLANNING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.POD_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PRIORITY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROCESS_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_IN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_OK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROW_NUMBER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RUNNING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STARTED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_REFERENCE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_TABLE_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TERMINAL_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.THREADS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.USER_ID;


/*******************************************************************************
 ** Memory instance and fixtures covering every job status and two owners.
 *******************************************************************************/
public class BaseJobsTest
{
   protected QInstance instance;
   protected JobsQBitConfig config;
   protected Instant now;
   protected static final String USER_A = "user-a";
   protected static final String USER_B = "user-b";

   /*******************************************************************************
    ** Fresh store and instance.
    *******************************************************************************/
   @BeforeEach
   void setUp() throws Exception
   {
      MemoryRecordStore.fullReset();
      instance = new QInstance();
      instance.addBackend(new QBackendMetaData().withName("memory").withBackendType(MemoryBackendModule.class));
      instance.withInstanceDefaultAuthentication(new QAuthenticationMetaData().withName("mock").withType(QAuthenticationType.MOCK));
      JobsTablesStandIn.defineAll(instance);
      instance.addTable(new QTableMetaData().withName(AUDIT).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.INTEGER)).withField(new QFieldMetaData(MESSAGE, QFieldType.STRING)));
      config = new JobsQBitConfig();
      new JobsQBitProducer().withQBitConfig(config).produce(instance);
      new QInstanceEnricher(instance).enrich();
      QContext.init(instance, new QSystemUserSession(instance));
      now = Instant.now();
   }

   /*******************************************************************************
    ** Clear thread locals and memory between tests.
    *******************************************************************************/
   @AfterEach
   void tearDown()
   {
      QContext.clear();
      MemoryRecordStore.fullReset();
   }

   /*******************************************************************************
    ** Start without pre-applied jobs keys.
    *******************************************************************************/
   protected QSession login(String user, boolean admin)
   {
      QSession session = new QSession().withUser(new QUser().withIdReference(user));
      if(admin)
      {
         session.withPermission(JOBS_ADMIN);
      }
      QContext.setQSession(session);
      return (session);
   }

   /*******************************************************************************
    ** Create data through core actions.
    *******************************************************************************/
   protected QRecord insert(String table, QRecord record) throws QException
   {
      QRecord inserted = new InsertAction().execute(new InsertInput().withTableName(table).withRecord(record)).getRecords().getFirst();
      Assertions.assertThat(inserted.getErrors()).isNullOrEmpty();
      return (inserted);
   }

   /*******************************************************************************
    ** Job with known counters.
    *******************************************************************************/
   protected QRecord job(String user, String status, Instant finished) throws QException
   {
      return (insert(JOBS_JOB, new QRecord().withValue(USER_ID, user).withValue(LABEL, user + " " + status)
         .withValue(PROCESS_NAME, "importOrders").withValue(STATUS, status).withValue(PRIORITY, 5)
         .withValue(QUEUED_AT, now.minus(11, ChronoUnit.MINUTES)).withValue(STARTED_AT, now.minus(10, ChronoUnit.MINUTES))
         .withValue(FINISHED_AT, finished).withValue(LAST_PROGRESS_AT, now.minus(2, ChronoUnit.MINUTES))
         .withValue(ROWS_TOTAL, 1000).withValue(ROWS_DONE, 450).withValue(ROWS_OK, 440).withValue(ROWS_FAILED, 10)
         .withValue(CHUNKS_TOTAL, 10).withValue(CHUNKS_DONE, 4).withValue(ERROR_MESSAGE, "x".repeat(140))));
   }

   /*******************************************************************************
    ** Related rows for join and retention tests.
    *******************************************************************************/
   protected void children(QRecord job) throws QException
   {
      Serializable id = job.getValue(ID);
      insert(JOBS_CHUNK, new QRecord().withValue(JOB_ID, id).withValue(SEQ, 1).withValue(STATUS, DONE)
         .withValue(ROWS_IN, 100).withValue(FINISHED_AT, now.minus(2, ChronoUnit.HOURS)).withValue(INPUT_STORAGE_REF, "chunk/" + id));
      insert(JOBS_ROW_ERROR, new QRecord().withValue(JOB_ID, id).withValue(ROW_NUMBER, 5).withValue(MESSAGE, "Bad row"));
      insert(JOBS_EVENT, new QRecord().withValue(JOB_ID, id).withValue(TYPE, COMPLETED).withValue(DETAIL, "Finished"));
      insert(JOBS_UPLOAD, new QRecord().withValue(JOB_ID, id).withValue(USER_ID, job.getValue(USER_ID))
         .withValue(REF, "upload/" + id).withValue(STORAGE_TABLE_NAME, "objects").withValue(STORAGE_REFERENCE, "input/" + id));
      insert(AUDIT, new QRecord().withValue(MESSAGE, "job:" + id + "/chunk:1 changed a row"));
   }

   /*******************************************************************************
    ** An outbox entry with a unique idempotency key, independent of other fixtures.
    *******************************************************************************/
   protected QRecord effect(QRecord job, String status, boolean needsReview) throws QException
   {
      return (insert(JOBS_EFFECT, new QRecord().withValue(JOB_ID, job.getValue(ID)).withValue(EFFECT_NAME, "Send notification")
         .withValue(STATUS, status).withValue(NEEDS_REVIEW_FIELD, needsReview).withValue(EFFECT_TYPE, IDEMPOTENT_EXTERNAL)
         .withValue(IDEMPOTENCY_KEY, UUID.randomUUID().toString())));
   }

   /*******************************************************************************
    ** Every status plus another owner's running job and one worker.
    *******************************************************************************/
   protected List<QRecord> seed() throws QException
   {
      List<String> statuses = List.of(PLANNING, AWAITING_CONFIRMATION, QUEUED, RUNNING, PAUSING, PAUSED, CANCELLING,
         CANCELLED, COMPLETED, COMPLETED_WITH_ERRORS, FAILED, NEEDS_REVIEW);
      List<QRecord> jobs = new ArrayList<>();
      for(String status : statuses)
      {
         boolean finished = TERMINAL_STATUSES.contains(status) || NEEDS_REVIEW.equals(status);
         QRecord job = job(USER_A, status, finished ? now.minus(30, ChronoUnit.MINUTES) : null);
         children(job);
         jobs.add(job);
      }
      children(job(USER_B, RUNNING, null));
      insert(JOBS_WORKER, new QRecord().withValue(POD_ID, "pod-1").withValue(THREADS, 4).withValue(DRAINING, false)
         .withValue(HEARTBEAT_AT, now.minusSeconds(12)).withValue(STARTED_AT, now.minus(1, ChronoUnit.DAYS)));
      return (jobs);
   }

   /*******************************************************************************
    ** Use the widget action so metadata default query parameters are applied.
    *******************************************************************************/
   protected QWidgetData render(String name, String id) throws QException
   {
      RenderWidgetInput input = new RenderWidgetInput().withWidgetMetaData(instance.getWidget(name));
      if(id != null)
      {
         input.addQueryParam("id", id);
      }
      return (new RenderWidgetAction().execute(input).getWidgetData());
   }

   /*******************************************************************************
    ** Read fixtures as the current session.
    *******************************************************************************/
   protected List<QRecord> records(String table) throws QException
   {
      return (new QueryAction().execute(new QueryInput().withTableName(table)).getRecords());
   }
}
