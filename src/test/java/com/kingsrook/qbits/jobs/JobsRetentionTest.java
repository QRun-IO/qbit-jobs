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


import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.jobs.processes.JobsPurgeFinishedStep;
import com.kingsrook.qbits.jobs.processes.JobsStorage;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QSupplementalInstanceMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.jobs.JobsCoreContract.AUDIT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED_WITH_ERRORS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DURABLE_JOBS_METADATA;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.INPUT_STORAGE_REF;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_UPLOAD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RUNNING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TERMINAL_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Retention boundaries, pagination, input cleanup, failure recovery and summaries.
 *******************************************************************************/
class JobsRetentionTest extends BaseJobsTest
{
   /*******************************************************************************
    ** Only old terminal jobs and their children are purged; audit remains intact.
    *******************************************************************************/
   @Test
   void purgesOldTerminalJobsAndIsIdempotent() throws Exception
   {
      for(String status : TERMINAL_STATUSES)
      {
         children(job(USER_A, status, now.minus(31, ChronoUnit.DAYS)));
      }
      children(job(USER_B, RUNNING, now.minus(40, ChronoUnit.DAYS)));
      children(job(USER_A, COMPLETED, now.minus(2, ChronoUnit.DAYS)));
      children(job(USER_A, NEEDS_REVIEW, now.minus(40, ChronoUnit.DAYS)));
      RecordingStorage storage = new RecordingStorage();
      QSession caller = login("admin", true);
      List<ProcessSummaryLine> summary = run(new JobsPurgeFinishedStep(storage));
      assertThat(QContext.getQSession()).isSameAs(caller);
      assertThat(summary).extracting(ProcessSummaryLine::getCount).containsExactly(4, 4, 4);
      JobsSecurity.ensureSessionKeys(caller);
      assertThat(records(JOBS_JOB)).hasSize(3).extracting(record -> record.getValueString(STATUS)).containsExactly(RUNNING, COMPLETED, NEEDS_REVIEW);
      for(String table : List.of(JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EVENT, JOBS_UPLOAD))
      {
         assertThat(records(table)).hasSize(3);
      }
      assertThat(records(AUDIT)).hasSize(7);
      assertThat(storage.deleted).hasSize(8);
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(0, 0, 0);
   }

   /*******************************************************************************
    ** Clean successful inputs expire independently of their retained job history.
    *******************************************************************************/
   @Test
   void cleansOnlyOldSuccessfulInputs() throws Exception
   {
      QRecord clean = job(USER_A, COMPLETED, now.minus(8, ChronoUnit.DAYS));
      clearFailures(clean);
      children(clean);
      children(job(USER_A, COMPLETED, now.minus(8, ChronoUnit.DAYS)));
      QRecord recent = job(USER_A, COMPLETED, now.minus(6, ChronoUnit.DAYS));
      clearFailures(recent);
      children(recent);
      children(job(USER_A, COMPLETED_WITH_ERRORS, now.minus(8, ChronoUnit.DAYS)));
      RecordingStorage storage = new RecordingStorage();
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(0, 0, 1);
      assertThat(records(JOBS_JOB)).hasSize(4);
      assertThat(records(JOBS_CHUNK).getFirst().getValue(INPUT_STORAGE_REF)).isNull();
      assertThat(records(JOBS_CHUNK).subList(1, 4)).allSatisfy(chunk -> assertThat(chunk.getValue(INPUT_STORAGE_REF)).isNotNull());
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(0, 0, 0);
      assertThat(storage.deleted).containsExactly("objects:chunk/" + clean.getValue(ID));
   }

   /*******************************************************************************
    ** Both parent and child batches span more than 200 records without skips.
    *******************************************************************************/
   @Test
   void pagesJobsAndChildren() throws Exception
   {
      for(int i = 0; i < 205; i++)
      {
         job(USER_A, FAILED, now.minus(40, ChronoUnit.DAYS));
      }
      QRecord old = records(JOBS_JOB).getFirst();
      for(int i = 0; i < 205; i++)
      {
         insert(JOBS_CHUNK, new QRecord().withValue(JOB_ID, old.getValue(ID)).withValue(SEQ, i).withValue(INPUT_STORAGE_REF, "old/" + i));
      }
      QRecord clean = job(USER_B, COMPLETED, now.minus(8, ChronoUnit.DAYS));
      clearFailures(clean);
      for(int i = 0; i < 205; i++)
      {
         insert(JOBS_CHUNK, new QRecord().withValue(JOB_ID, clean.getValue(ID)).withValue(SEQ, i).withValue(INPUT_STORAGE_REF, "clean/" + i));
      }
      RecordingStorage storage = new RecordingStorage();
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(205, 205, 205);
      assertThat(records(JOBS_JOB)).hasSize(1);
      assertThat(records(JOBS_CHUNK)).hasSize(205).allSatisfy(chunk -> assertThat(chunk.getValue(INPUT_STORAGE_REF)).isNull());
      assertThat(storage.deleted).hasSize(410);
   }

   /*******************************************************************************
    ** Core storage failures preserve input references for retry.
    *******************************************************************************/
   @Test
   void missingStoragePreservesReferences() throws Exception
   {
      QRecord clean = job(USER_A, COMPLETED, now.minus(8, ChronoUnit.DAYS));
      clearFailures(clean);
      children(clean);
      instance.withSupplementalMetaData(new StorageMetadata());
      assertThatThrownBy(() -> run(new JobsPurgeFinishedStep())).hasMessageContaining("Could not delete durable jobs storage object");
      assertThat(records(JOBS_CHUNK).getFirst().getValue(INPUT_STORAGE_REF)).isNotNull();
      JobsStorage storage = new JobsStorage();
      assertThat(storage.chunkStorageTable()).isEqualTo("objects");
      assertThatThrownBy(() -> storage.delete("objects", "another-object")).hasMessageContaining("Could not delete durable jobs storage object");
      assertThat(storage.delete(null, "object")).isFalse();
      assertThat(storage.delete("objects", null)).isFalse();
   }

   /*******************************************************************************
    ** A storage failure preserves the parent and always restores the caller.
    *******************************************************************************/
   @Test
   void storageFailureRestoresSession() throws Exception
   {
      children(job(USER_A, FAILED, now.minus(40, ChronoUnit.DAYS)));
      QSession caller = login("admin", true);
      RecordingStorage storage = new RecordingStorage();
      storage.fail = true;
      assertThatThrownBy(() -> run(new JobsPurgeFinishedStep(storage))).hasMessageContaining("storage unavailable");
      assertThat(QContext.getQSession()).isSameAs(caller);
      JobsSecurity.ensureSessionKeys(caller);
      assertThat(records(JOBS_JOB)).hasSize(1);
      assertThat(records(JOBS_CHUNK)).hasSize(1);
      storage.fail = false;
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(1, 1, 1);
   }

   /*******************************************************************************
    ** Hidden does not mean authorized: direct invocation checks permission too.
    *******************************************************************************/
   @Test
   void normalViewerCannotPurge()
   {
      QSession viewer = login(USER_A, false);
      assertThatThrownBy(() -> run(new JobsPurgeFinishedStep())).hasMessageContaining("requires jobs_admin");
      assertThat(QContext.getQSession()).isSameAs(viewer);
   }

   /*******************************************************************************
    ** No supplemental metadata means object cleanup is skipped.
    *******************************************************************************/
   @Test
   void absentStorageMetadata() throws Exception
   {
      assertThat(new JobsStorage().chunkStorageTable()).isNull();
      assertThat(run(new JobsPurgeFinishedStep())).extracting(ProcessSummaryLine::getCount).containsExactly(0, 0, 0);
   }

   /*******************************************************************************
    ** Outstanding effects preserve every child and input; settled effects are paged away.
    *******************************************************************************/
   @Test
   void effectsBlockPurgeUntilSettled() throws Exception
   {
      List<QRecord> kept = new ArrayList<>();
      for(String status : List.of(PENDING, SENDING, UNKNOWN))
      {
         QRecord job = job(USER_A, COMPLETED, now.minus(40, ChronoUnit.DAYS));
         clearFailures(job);
         children(job);
         effect(job, status, false);
         kept.add(job);
      }
      QRecord review = job(USER_A, FAILED, now.minus(40, ChronoUnit.DAYS));
      children(review);
      effect(review, FAILED, true);
      kept.add(review);
      QRecord settled = job(USER_B, COMPLETED, now.minus(40, ChronoUnit.DAYS));
      children(settled);
      for(int i = 0; i < 205; i++)
      {
         effect(settled, i % 2 == 0 ? SENT : FAILED, false);
      }
      RecordingStorage storage = new RecordingStorage();
      QSession caller = login("admin", true);
      List<ProcessSummaryLine> summary = run(new JobsPurgeFinishedStep(storage));
      assertThat(QContext.getQSession()).isSameAs(caller);
      assertThat(summary).extracting(ProcessSummaryLine::getCount).containsExactly(1, 1, 1, 4);
      assertThat(summary.getLast().getMessage()).isEqualTo("jobs kept: effects still outstanding");
      JobsSecurity.ensureSessionKeys(caller);
      assertThat(records(JOBS_JOB)).extracting(record -> record.getValue(ID)).containsExactlyElementsOf(kept.stream().map(record -> record.getValue(ID)).toList());
      assertThat(records(JOBS_EFFECT)).hasSize(4);
      for(String table : List.of(JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EVENT, JOBS_UPLOAD))
      {
         assertThat(records(table)).hasSize(4);
      }
      assertThat(records(JOBS_CHUNK)).allSatisfy(chunk -> assertThat(chunk.getValue(INPUT_STORAGE_REF)).isNotNull());
      assertThat(storage.deleted).containsExactly("objects:chunk/" + settled.getValue(ID), "objects:input/" + settled.getValue(ID));
      for(QRecord effect : records(JOBS_EFFECT))
      {
         new UpdateAction().execute(new UpdateInput().withTableName(JOBS_EFFECT).withRecord(new QRecord()
            .withValue(ID, effect.getValue(ID)).withValue(STATUS, SENT).withValue(NEEDS_REVIEW_FIELD, false)));
      }
      assertThat(run(new JobsPurgeFinishedStep(storage))).extracting(ProcessSummaryLine::getCount).containsExactly(4, 4, 4);
      assertThat(records(JOBS_JOB)).isEmpty();
      assertThat(records(JOBS_EFFECT)).isEmpty();
   }

   /*******************************************************************************
    ** Effects remain optional for retention against older cores.
    *******************************************************************************/
   @Test
   void purgeWithoutEffectsTable() throws Exception
   {
      instance.getTables().remove(JOBS_EFFECT);
      children(job(USER_A, FAILED, now.minus(40, ChronoUnit.DAYS)));
      assertThat(run(new JobsPurgeFinishedStep(new RecordingStorage()))).extracting(ProcessSummaryLine::getCount).containsExactly(1, 1, 1);
      assertThat(records(JOBS_JOB)).isEmpty();
   }

   /*******************************************************************************
    ** Mark the test job successful with no failed rows.
    *******************************************************************************/
   private void clearFailures(QRecord job) throws QException
   {
      new UpdateAction().execute(new UpdateInput().withTableName(JOBS_JOB)
         .withRecord(new QRecord().withValue(ID, job.getValue(ID)).withValue(ROWS_FAILED, 0)));
   }

   /*******************************************************************************
    ** The scheduled step carries standard ProcessSummaryLines in its output.
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   private List<ProcessSummaryLine> run(JobsPurgeFinishedStep step) throws QException
   {
      RunBackendStepOutput output = new RunBackendStepOutput();
      step.run(new RunBackendStepInput(), output);
      return ((List<ProcessSummaryLine>) output.getValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY));
   }

   /*******************************************************************************
    ** Test storage models successful RC.7 object deletion, without using disk.
    *******************************************************************************/
   private static class RecordingStorage extends JobsStorage
   {
      private final List<String> deleted = new ArrayList<>();
      private boolean fail;

      /*******************************************************************************
       ** Configured storage table.
       *******************************************************************************/
      @Override
      public String chunkStorageTable()
      {
         return ("objects");
      }

      /*******************************************************************************
       ** Record calls and verify they execute as system.
       *******************************************************************************/
      @Override
      public boolean delete(String table, String reference) throws QException
      {
         assertThat(QContext.getQSession()).isInstanceOf(QSystemUserSession.class);
         if(fail)
         {
            throw (new QException("storage unavailable"));
         }
         if(reference == null)
         {
            return (false);
         }
         deleted.add(table + ":" + reference);
         return (true);
      }
   }

   /*******************************************************************************
    ** Test-only supplemental metadata for the RC.5 reflection bridge.
    *******************************************************************************/
   public static class StorageMetadata implements QSupplementalInstanceMetaData
   {
      /*******************************************************************************
       ** Core metadata contract name.
       *******************************************************************************/
      @Override
      public String getName()
      {
         return (DURABLE_JOBS_METADATA);
      }

      /*******************************************************************************
       ** Future core getter discovered reflectively by the adapter.
       *******************************************************************************/
      public String getStorageTableName()
      {
         return ("objects");
      }
   }
}
