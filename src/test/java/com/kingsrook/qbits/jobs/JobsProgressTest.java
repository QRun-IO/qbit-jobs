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
import java.util.List;
import com.kingsrook.qbits.jobs.processes.JobsPurgeFinishedStep;
import com.kingsrook.qbits.jobs.widgets.JobsFormat;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.jobs.JobsCoreContract.BYTES_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.BYTES_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CURRENT_PHASE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ETA_SECONDS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_LOG;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEVEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LOGGED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LOG_DROPPED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LOG_SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PERCENT_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PHASE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROGRESS_JSON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROGRESS_MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RUNNING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Optional RC.8 progress and logs, including the actual core log permission checker.
 *******************************************************************************/
class JobsProgressTest extends BaseJobsTest
{
   /*******************************************************************************
    ** Add optional fields and a log using the real kernel permission checker by
    ** name, preserving the default suite's coverage of older kernel metadata.
    *******************************************************************************/
   private void enableProgress() throws Exception
   {
      for(String field : List.of(CURRENT_PHASE, PROGRESS_MESSAGE, PROGRESS_JSON))
      {
         instance.getTable(JOBS_JOB).withField(new QFieldMetaData(field, QFieldType.TEXT));
      }
      for(String field : List.of(PERCENT_DONE, LOG_DROPPED))
      {
         instance.getTable(JOBS_JOB).withField(new QFieldMetaData(field, QFieldType.INTEGER));
      }
      for(String field : List.of(BYTES_DONE, BYTES_TOTAL, ETA_SECONDS, LOG_SEQ))
      {
         instance.getTable(JOBS_JOB).withField(new QFieldMetaData(field, QFieldType.LONG));
      }
      instance.setEnforcePermissionsInCoreActions(true);
      //////////////////////////////////////////////////////////////////////
      // core 4.1.0-RC.8 defines jobs_log; older metadata gets this mirror //
      //////////////////////////////////////////////////////////////////////
      if(instance.getTable(JOBS_LOG) != null)
      {
         return;
      }
      instance.addTable(new QTableMetaData().withName(JOBS_LOG).withBackendName("memory").withPrimaryKeyField(ID)
         .withField(new QFieldMetaData(ID, QFieldType.LONG))
         .withField(new QFieldMetaData(JOB_ID, QFieldType.INTEGER))
         .withField(new QFieldMetaData(LEVEL, QFieldType.STRING))
         .withField(new QFieldMetaData(LOGGED_AT, QFieldType.DATE_TIME))
         .withField(new QFieldMetaData(PHASE, QFieldType.STRING))
         .withField(new QFieldMetaData(MESSAGE, QFieldType.STRING))
         .withPermissionRules(new QPermissionRules().withCustomPermissionChecker(new QCodeReference(
            Class.forName("com.kingsrook.qqq.backend.core.jobs.progress.JobLogPermissions"))))
         .withRecordSecurityLock(new RecordSecurityLock().withSecurityKeyType(JOBS_USER).withFieldName(JOBS_JOB + "." + USER_ID)
            .withJoinNameChain(List.of(JobsCoreContract.childJoin(JOBS_LOG)))));
      instance.addJoin(new QJoinMetaData().withLeftTable(JOBS_JOB).withRightTable(JOBS_LOG).withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn(ID, JOB_ID)).withInferredName());
      clearPresentation();
      new JobsQBitProducer().withQBitConfig(config).produce(instance);
      new QInstanceEnricher(instance).enrich();
   }

   /*******************************************************************************
    ** Rich fields override counters, resolve labels, and remain typed and flexible.
    *******************************************************************************/
   @Test
   void richProgressOnHomeAndRecord() throws Exception
   {
      enableProgress();
      QRecord job = job(USER_A, RUNNING, null);
      update(job, new QRecord().withValue(CURRENT_PHASE, "upload").withValue(PERCENT_DONE, 73)
         .withValue(PROGRESS_MESSAGE, "Uploading manifest").withValue(BYTES_DONE, 1536L).withValue(BYTES_TOTAL, 1048576L)
         .withValue(ETA_SECONDS, 90L).withValue(LOG_DROPPED, 12)
         .withValue(PROGRESS_JSON, "{\"phases\":[{\"name\":\"upload\",\"label\":\"Upload backup\"}]}"));
      login(USER_A, false).withPermission(config.getHasAccessPermissionName());
      TableData home = (TableData) render("jobsHomeActive", null);
      assertThat(home.getRows()).singleElement().satisfies(row -> assertThat(row)
         .containsEntry("Progress", "73%").containsEntry("Phase", "Upload backup")
         .containsEntry("Message", "Uploading manifest").containsEntry("Bytes", "1.5 KiB / 1.0 MiB").containsEntry("ETA", "in 1m"));
      assertThat(home.getColumns()).hasSize(3).allSatisfy(column -> assertThat(column.getWidth()).isNull());
      MultiStatisticsData record = (MultiStatisticsData) render("jobsJobProgress", job.getValueString(ID));
      assertThat(record.getStatisticsGroupData()).extracting(MultiStatisticsData.StatisticsGroupData::getSubheader)
         .contains("Upload backup | Uploading manifest | 1.5 KiB / 1.0 MiB", "73% done", "ETA in 1m", "12 earlier lines not kept");
      login(USER_B, false).withPermission(config.getHasAccessPermissionName());
      assertThat(((TableData) render("jobsHomeActive", null)).getRows()).isEmpty();
      assertThat(((MultiStatisticsData) render("jobsJobProgress", job.getValueString(ID))).getStatisticsGroupData()).isEmpty();
   }

   /*******************************************************************************
    ** Null percent uses rows then chunks, zero is explicit, malformed JSON is safe.
    *******************************************************************************/
   @Test
   void fallbacksAndPartialProgress() throws Exception
   {
      enableProgress();
      QRecord job = job(USER_A, RUNNING, null);
      update(job, new QRecord().withValue(CURRENT_PHASE, "copy").withValue(PROGRESS_JSON, "bad json").withValue(BYTES_TOTAL, 1024L));
      assertThat(((TableData) render("jobsHomeActive", null)).getRows().getFirst())
         .containsEntry("Progress", "45%").containsEntry("Phase", "copy").containsEntry("Bytes", "1.0 KiB total");
      update(job, new QRecord().withValue(ROWS_TOTAL, null).withValue(PROGRESS_JSON, "null"));
      assertThat(((TableData) render("jobsHomeActive", null)).getRows().getFirst()).containsEntry("Progress", "40%");
      update(job, new QRecord().withValue(PERCENT_DONE, 0).withValue(BYTES_TOTAL, null).withValue(BYTES_DONE, 0L));
      assertThat(((TableData) render("jobsHomeActive", null)).getRows().getFirst()).containsEntry("Progress", "0%").containsEntry("Bytes", "0 B");
      update(job, new QRecord().withValue(PERCENT_DONE, null).withValue(CHUNKS_TOTAL, null));
      assertThat(((TableData) render("jobsHomeActive", null)).getRows().getFirst()).containsEntry("Progress", "Unknown");
      assertThat(JobsFormat.bytes(1073741824L)).isEqualTo("1.0 GiB");
      assertThat(JobsFormat.bytes(Long.MAX_VALUE)).isEqualTo("8.0 EiB");
   }

   /*******************************************************************************
    ** The log is ordered, full width and owner scoped, with no user mutations.
    *******************************************************************************/
   @Test
   void logPanelAndPermissions() throws Exception
   {
      enableProgress();
      QRecord job = job(USER_A, RUNNING, null);
      log(job, 1000000002L);
      log(job, 1000000001L);
      assertThat(instance.getWidget("jobsJobLog").getGridColumns()).isEqualTo(12);
      assertThat(instance.getTable(JOBS_JOB).getSections()).extracting(QFieldSection::getWidgetName)
         .containsSubsequence("jobsJobProgress", "jobsJobLog", "jobsJobEvents");
      login(USER_A, false).withPermission("jobs_job.read");
      ChildRecordListData data = (ChildRecordListData) render("jobsJobLog", job.getValueString(ID));
      assertThat(data.getQueryOutput().getRecords()).extracting(record -> record.getValueLong(ID)).containsExactly(1000000002L, 1000000001L);
      assertThat(data.getOnlyIncludeFieldNames()).containsExactly(LEVEL, LOGGED_AT, PHASE, MESSAGE);
      assertThat(data.getCanAddChildRecord()).isFalse();
      assertThatThrownBy(() -> new DeleteAction().execute(new DeleteInput().withTableName(JOBS_LOG).withPrimaryKey(1000000001L))).hasMessageContaining("read-only");
      assertThatThrownBy(() -> log(job, 1000000003L)).hasMessageContaining("read-only");
      login("admin", true).withPermission("jobs_job.read");
      assertThatThrownBy(() -> new UpdateAction().execute(new UpdateInput().withTableName(JOBS_LOG)
         .withRecord(new QRecord().withValue(ID, 1000000001L).withValue(MESSAGE, "changed")))).hasMessageContaining("read-only");
      login(USER_B, false).withPermission("jobs_job.read");
      assertThatThrownBy(() -> render("jobsJobLog", job.getValueString(ID))).hasMessageContaining("Could not find");
   }

   /*******************************************************************************
    ** The system purge crosses a full batch with LONG IDs; active logs remain.
    *******************************************************************************/
   @Test
   void purgeLogBatchesWithSystemPermission() throws Exception
   {
      enableProgress();
      QRecord old = job(USER_A, COMPLETED, now.minus(31, ChronoUnit.DAYS));
      QRecord active = job(USER_B, RUNNING, null);
      for(long id = 1000000000L; id < 1000000205L; id++)
      {
         log(old, id);
      }
      log(active, 1000000300L);
      QRecord held = job(USER_A, COMPLETED, now.minus(31, ChronoUnit.DAYS));
      effect(held, JobsCoreContract.PENDING, false);
      log(held, 1000000301L);
      var caller = login("admin", true);
      new JobsPurgeFinishedStep().run(new RunBackendStepInput(), new RunBackendStepOutput());
      assertThat(QContext.getQSession()).isSameAs(caller);
      QContext.setQSession(new QSystemUserSession());
      assertThat(records(JOBS_LOG)).extracting(record -> record.getValue(JOB_ID)).containsExactlyInAnyOrder(active.getValue(ID), held.getValue(ID));
      assertThat(records(JOBS_JOB)).extracting(record -> record.getValue(ID)).containsExactlyInAnyOrder(active.getValue(ID), held.getValue(ID));
   }

   /*******************************************************************************
    ** Log metadata is optional; hosts choose the Admin/Operations parent.
    *******************************************************************************/
   @Test
   void optionalLogAndHostNavigation() throws Exception
   {
      assertThat(instance.getWidget("jobsJobLog")).as("core RC.8 defines jobs_log, so the Log list is produced").isNotNull();
      enableProgress();
      instance.getJoins().remove(JobsCoreContract.childJoin(JOBS_LOG));
      instance.getWidgets().remove("jobsJobLog");
      clearPresentation();
      new JobsQBitProducer().produce(instance);
      assertThat(instance.getWidget("jobsJobLog")).isNull();
      QAppMetaData admin = new QAppMetaData().withName("admin").withLabel("Admin");
      QAppMetaData operations = new QAppMetaData().withName("operations").withLabel("Operations");
      operations.setParentAppName("admin");
      instance.addApp(admin.withChild(operations));
      instance.addApp(operations);
      config.withAppName("backgroundJobs").withParentAppName("operations");
      clearPresentation();
      new JobsQBitProducer().withQBitConfig(config).produce(instance);
      assertThat(instance.getApp("backgroundJobs").getParentAppName()).isEqualTo("operations");
      assertThat(operations.getChildren()).extracting(child -> child.getName()).containsExactly("backgroundJobs");
      config.setParentAppName("missing");
      assertThatThrownBy(() -> config.validate(instance)).hasMessageContaining("parentAppName");
      config.setParentAppName("backgroundJobs");
      assertThatThrownBy(() -> config.validate(instance)).hasMessageContaining("parentAppName");
      config.setParentAppName(null);
      config.validate(instance);
   }

   /*******************************************************************************
    ** Rebuild presentation against changed optional metadata in these tests.
    *******************************************************************************/
   private void clearPresentation()
   {
      instance.getQBits().clear();
      instance.getWidgets().clear();
      instance.getApps().remove("jobs");
      instance.getProcesses().remove(JobsQBitProducer.PURGE_PROCESS);
      instance.getTable(JOBS_JOB).getSections().removeIf(section -> section.getWidgetName() != null);
   }

   /*******************************************************************************
    ** Persist progress through core actions.
    *******************************************************************************/
   private void update(QRecord job, QRecord values) throws Exception
   {
      var output = new UpdateAction().execute(new UpdateInput().withTableName(JOBS_JOB).withRecord(values.withValue(ID, job.getValue(ID))));
      assertThat(output.getRecords().getFirst().getErrors()).isNullOrEmpty();
   }

   /*******************************************************************************
    ** Explicit LONG cursors make ordering assertions deterministic. Core memory
    ** sequence bookkeeping currently restricts inserts to the integer range.
    *******************************************************************************/
   private void log(QRecord job, long id) throws Exception
   {
      insert(JOBS_LOG, new QRecord().withValue(ID, id).withValue(JOB_ID, job.getValue(ID)).withValue(LEVEL, "INFO")
         .withValue(LOGGED_AT, now).withValue(PHASE, "upload").withValue(MESSAGE, "Wrote manifest"));
   }
}
