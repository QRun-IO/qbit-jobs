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


package com.kingsrook.qbits.jobs.processes;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.jobs.JobsQBitConfig;
import com.kingsrook.qbits.jobs.JobsQBitProducer;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FINISHED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.INPUT_STORAGE_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ADMIN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_UPLOAD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_REFERENCE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STORAGE_TABLE_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TERMINAL_STATUSES;


/*******************************************************************************
 ** Retention for core's standard tables. Batches use increasing primary keys so
 ** updates and deletes cannot cause skipped rows. Only this step uses system access.
 *******************************************************************************/
public class JobsPurgeFinishedStep implements BackendStep
{
   private static final int BATCH_SIZE = 200;
   private final JobsStorage storage;

   /*******************************************************************************
    ** Framework constructor.
    *******************************************************************************/
   public JobsPurgeFinishedStep()
   {
      this(new JobsStorage());
   }

   /*******************************************************************************
    ** Storage seam for testing RC.7 deletion semantics against RC.5.
    *******************************************************************************/
   public JobsPurgeFinishedStep(JobsStorage storage)
   {
      this.storage = storage;
   }

   /*******************************************************************************
    ** Restore the caller even when storage or a table action fails.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      if(QContext.getQSession() == null || !QContext.getQSession().hasPermission(JOBS_ADMIN))
      {
         throw (new QException("jobsPurgeFinished requires jobs_admin or a system session"));
      }
      JobsQBitConfig config = (JobsQBitConfig) QContext.getQInstance().getQBits().get(JobsQBitProducer.QBIT_NAME).getConfig();
      QSession caller = QContext.getQSession();
      int[] counts = new int[4];
      try
      {
         QContext.setQSession(new QSystemUserSession());
         Instant now = Instant.now();
         purge(config, now, counts);
         cleanInputs(config, now, counts);
      }
      finally
      {
         QContext.setQSession(caller);
      }
      ArrayList<ProcessSummaryLineInterface> summary = new ArrayList<>();
      summary.add(new ProcessSummaryLine(Status.INFO, counts[0], "jobs purged"));
      summary.add(new ProcessSummaryLine(Status.INFO, counts[1], "chunk objects deleted"));
      summary.add(new ProcessSummaryLine(Status.INFO, counts[2], "input objects deleted"));
      if(counts[3] > 0)
      {
         summary.add(new ProcessSummaryLine(Status.INFO, counts[3], "jobs kept: effects still outstanding"));
      }
      output.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, summary);
   }

   /*******************************************************************************
    ** Remove old final jobs, deleting children before their parent.
    *******************************************************************************/
   private void purge(JobsQBitConfig config, Instant now, int[] counts) throws QException
   {
      int lastId = 0;
      while(true)
      {
         QQueryFilter filter = new QQueryFilter().withCriteria(STATUS, QCriteriaOperator.IN, TERMINAL_STATUSES)
            .withCriteria(FINISHED_AT, QCriteriaOperator.LESS_THAN, now.minus(config.getRetentionDays(), ChronoUnit.DAYS));
         List<QRecord> jobs = batch(JOBS_JOB, filter, lastId);
         if(jobs.isEmpty())
         {
            return;
         }
         for(QRecord job : jobs)
         {
            lastId = job.getValueInteger(ID);
            if(hasOutstandingEffects(lastId))
            {
               counts[3]++;
               continue;
            }
            if(QContext.getQInstance().getTable(JOBS_EFFECT) != null)
            {
               removeChildren(JOBS_EFFECT, lastId, counts);
            }
            removeChildren(JOBS_ROW_ERROR, lastId, counts);
            removeChildren(JOBS_EVENT, lastId, counts);
            removeChildren(JOBS_CHUNK, lastId, counts);
            removeChildren(JOBS_UPLOAD, lastId, counts);
            deleteRecord(JOBS_JOB, job);
            counts[0]++;
         }
      }
   }

   /*******************************************************************************
    ** Preserve unresolved outbox work, including unknown or missing statuses.
    *******************************************************************************/
   private boolean hasOutstandingEffects(int jobId) throws QException
   {
      if(QContext.getQInstance().getTable(JOBS_EFFECT) == null)
      {
         return (false);
      }
      QQueryFilter outstanding = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withCriteria(STATUS, QCriteriaOperator.NOT_IN, List.of(SENT, FAILED))
         .withCriteria(STATUS, QCriteriaOperator.IS_BLANK)
         .withCriteria(NEEDS_REVIEW_FIELD, QCriteriaOperator.EQUALS, true);
      QQueryFilter filter = new QQueryFilter().withCriteria(JOB_ID, QCriteriaOperator.EQUALS, jobId).withSubFilter(outstanding).withLimit(1);
      return (!new QueryAction().execute(new QueryInput().withTableName(JOBS_EFFECT).withFilter(filter)).getRecords().isEmpty());
   }

   /*******************************************************************************
    ** Page all children, including uploads, before removing a job.
    *******************************************************************************/
   private void removeChildren(String table, int jobId, int[] counts) throws QException
   {
      int lastId = 0;
      while(true)
      {
         List<QRecord> records = batch(table, new QQueryFilter().withCriteria(JOB_ID, QCriteriaOperator.EQUALS, jobId), lastId);
         if(records.isEmpty())
         {
            return;
         }
         for(QRecord record : records)
         {
            lastId = record.getValueInteger(ID);
            if(JOBS_CHUNK.equals(table) && storage.delete(storage.chunkStorageTable(), record.getValueString(INPUT_STORAGE_REF)))
            {
               counts[1]++;
            }
            if(JOBS_UPLOAD.equals(table) && storage.delete(record.getValueString(STORAGE_TABLE_NAME), record.getValueString(STORAGE_REFERENCE)))
            {
               counts[2]++;
            }
            deleteRecord(table, record);
         }
      }
   }

   /*******************************************************************************
    ** Keep successful job history while releasing its old input objects.
    *******************************************************************************/
   private void cleanInputs(JobsQBitConfig config, Instant now, int[] counts) throws QException
   {
      int lastId = 0;
      while(true)
      {
         QQueryFilter filter = new QQueryFilter().withCriteria(STATUS, QCriteriaOperator.EQUALS, COMPLETED)
            .withCriteria(ROWS_FAILED, QCriteriaOperator.EQUALS, 0)
            .withCriteria(FINISHED_AT, QCriteriaOperator.LESS_THAN, now.minus(config.getInputRetentionDays(), ChronoUnit.DAYS));
         List<QRecord> jobs = batch(JOBS_JOB, filter, lastId);
         if(jobs.isEmpty())
         {
            return;
         }
         for(QRecord job : jobs)
         {
            lastId = job.getValueInteger(ID);
            if(!hasOutstandingEffects(lastId))
            {
               cleanJobInputs(lastId, counts);
            }
         }
      }
   }

   /*******************************************************************************
    ** Null a reference only after storage reports successful deletion.
    *******************************************************************************/
   private void cleanJobInputs(int jobId, int[] counts) throws QException
   {
      int lastId = 0;
      while(true)
      {
         List<QRecord> chunks = batch(JOBS_CHUNK, new QQueryFilter().withCriteria(JOB_ID, QCriteriaOperator.EQUALS, jobId)
            .withCriteria(INPUT_STORAGE_REF, QCriteriaOperator.IS_NOT_BLANK), lastId);
         if(chunks.isEmpty())
         {
            return;
         }
         for(QRecord chunk : chunks)
         {
            lastId = chunk.getValueInteger(ID);
            if(storage.delete(storage.chunkStorageTable(), chunk.getValueString(INPUT_STORAGE_REF)))
            {
               QRecord update = new QRecord().withValue(ID, lastId).withValue(INPUT_STORAGE_REF, null);
               List<QRecord> results = new UpdateAction().execute(new UpdateInput().withTableName(JOBS_CHUNK).withRecord(update)).getRecords();
               if(results.isEmpty() || results.stream().anyMatch(record -> record.getErrors() != null && !record.getErrors().isEmpty()))
               {
                  throw (new QException("Could not clear deleted chunk input reference"));
               }
               counts[2]++;
            }
         }
      }
   }

   /*******************************************************************************
    ** Keyset pagination remains correct when rows are deleted or updated.
    *******************************************************************************/
   private List<QRecord> batch(String table, QQueryFilter filter, int lastId) throws QException
   {
      return (new QueryAction().execute(new QueryInput().withTableName(table).withFilter(filter.withCriteria(ID, QCriteriaOperator.GREATER_THAN, lastId)
         .withOrderBy(new QFilterOrderBy(ID)).withLimit(BATCH_SIZE))).getRecords());
   }

   /*******************************************************************************
    ** Treat action-level record errors as failures, never as a successful purge.
    *******************************************************************************/
   private void deleteRecord(String table, QRecord record) throws QException
   {
      DeleteOutput result = new DeleteAction().execute(new DeleteInput().withTableName(table).withPrimaryKey(record.getValue(ID)));
      if(result.getRecordsWithErrors() != null && !result.getRecordsWithErrors().isEmpty())
      {
         throw (new QException("Could not purge record from " + table));
      }
   }
}
