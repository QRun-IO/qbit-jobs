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
import com.kingsrook.qqq.backend.core.alerts.AlertMonitorKind;
import com.kingsrook.qqq.backend.core.alerts.AlertSeverity;
import com.kingsrook.qqq.backend.core.alerts.AlertsInstanceMetaData;
import com.kingsrook.qqq.backend.core.alerts.QAlertTypeMetaData;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaData;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_EFFECT_UNRESOLVED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_JOB_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_JOB_STALLED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ATTEMPT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNK_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.HEARTBEAT_REAPER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT_STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT_TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_UPLOAD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_WORKER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEASE_VERSION;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RECONCILE_ATTEMPTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Metadata contract, configuration validation and integration registrations.
 *******************************************************************************/
class JobsQBitProducerTest extends BaseJobsTest
{
   /*******************************************************************************
    ** The real kernel configuration passes startup validation without waivers.
    *******************************************************************************/
   @Test
   void durableJobsMetadataValidates()
   {
      QInstanceValidator validator = new QInstanceValidator();
      DurableJobsMetaData.of(instance).validate(instance, validator);
      assertThat(validator.getErrors()).isEmpty();
   }

   /*******************************************************************************
    ** Fail before registering anything when the kernel has not been enabled.
    *******************************************************************************/
   @Test
   void missingTablesFailClearly()
   {
      QInstance empty = new QInstance();
      assertThatThrownBy(() -> new JobsQBitProducer().produce(empty)).isInstanceOf(QException.class)
         .hasMessage("Durable jobs are not enabled: call DurableJobsMetaDataProvider.defineAll(...) before producing qbit-jobs");
      assertThat(empty.getQBits()).isEmpty();
      instance.getTables().remove(JOBS_UPLOAD);
      assertThatThrownBy(() -> new JobsQBitProducer().produce(instance)).hasMessageContaining("Durable jobs are not enabled");
   }

   /*******************************************************************************
    ** Only the main table appears in navigation; every panel uses typed data.
    *******************************************************************************/
   @Test
   void navigationWidgetsAndRecordSections()
   {
      assertThat(instance.getTables()).hasSize(11);
      assertThat(instance.getApp("jobs").getChildren()).extracting(child -> child.getName()).containsExactly(JOBS_JOB);
      assertThat(instance.getApp("jobs").getWidgets()).containsExactlyElementsOf(JobsQBitProducer.HOME_WIDGETS);
      assertThat(instance.getWidgets()).hasSize(12);
      assertThat(instance.getWidgets().values()).allSatisfy(widget -> assertThat(widget.getType()).isNotEqualTo(WidgetType.HTML.getType()));
      assertThat(instance.getTable(JOBS_JOB).getSections()).extracting(QFieldSection::getWidgetName)
         .containsSubsequence("jobsJobProgress", "jobsJobChunks", "jobsJobRowErrors", "jobsJobEffects", "jobsJobEvents", "jobsJobAudit");
      for(String name : JobsQBitProducer.HOME_WIDGETS)
      {
         assertThat(instance.getWidget(name).getDefaultValues()).containsEntry("refreshSeconds", 5);
      }
      assertThat(instance.getWidget("jobsJobProgress").getDefaultValues()).containsEntry("refreshSeconds", 3);
      assertThat(instance.getProcess(JobsQBitProducer.PURGE_PROCESS).getIsHidden()).isTrue();
      assertThat(instance.getProcess(JobsQBitProducer.PURGE_PROCESS).getSchedule()).isNull();
      assertThat(instance.getWidget("jobsHomeWorkers").getPermissionRules().getCustomPermissionChecker()).isNotNull();
      QBitMetaData metadata = instance.getQBits().get(JobsQBitProducer.QBIT_NAME);
      assertThat(metadata.getGroupId()).isEqualTo("com.kingsrook.qbits");
      assertThat(metadata.getArtifactId()).isEqualTo("qbit-jobs");
      assertThat(metadata.getNamespace()).isEqualTo("jobs");
      assertThat(metadata.getVersion()).matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT|-RC\\.\\d+)?");
   }

   /*******************************************************************************
    ** Optional audit and scheduler behavior, including configured refresh values.
    *******************************************************************************/
   @Test
   void schedulerAndAuditAreOptional() throws Exception
   {
      QInstance other = new QInstance();
      new DurableJobsMetaDataProvider().defineAll(other, "memory", null);
      other.withSupplementalMetaData(new DurableJobsMetaData());
      JobsQBitConfig custom = new JobsQBitConfig().withAppName("backgroundJobs").withSchedulerName("scheduler")
         .withPurgeCronExpression("0 0 4 * * ?").withHomeRefreshSeconds(9).withRecordRefreshSeconds(8);
      new JobsQBitProducer().withQBitConfig(custom).produce(other);
      assertThat(other.getWidget("jobsJobAudit")).isNull();
      assertThat(other.getWidgets()).hasSize(11);
      assertThat(other.getApp("backgroundJobs")).isNotNull();
      QScheduleMetaData schedule = other.getProcess(JobsQBitProducer.PURGE_PROCESS).getSchedule();
      assertThat(schedule.getSchedulerName()).isEqualTo("scheduler");
      assertThat(schedule.getCronExpression()).isEqualTo("0 0 4 * * ?");
      assertThat(schedule.getCronTimeZoneId()).isEqualTo("UTC");
      assertThat(other.getWidget("jobsHomeSummary").getDefaultValues()).containsEntry("refreshSeconds", 9);
      assertThat(other.getWidget("jobsJobProgress").getDefaultValues()).containsEntry("refreshSeconds", 8);
   }

   /*******************************************************************************
    ** Match the kernel's failure, stall and heartbeat signal identifiers.
    *******************************************************************************/
   @Test
   void alertTypes()
   {
      QAlertTypeMetaData failed = QAlertTypeMetaData.of(instance, ALERT_JOB_FAILED);
      assertThat(failed.getDefaultSeverity()).isEqualTo(AlertSeverity.ERROR);
      assertThat(failed.getSubjectTableName()).isEqualTo(JOBS_JOB);
      assertThat(failed.getHowToFix()).isNotBlank();
      assertThat(QAlertTypeMetaData.of(instance, ALERT_JOB_STALLED).getDefaultSeverity()).isEqualTo(AlertSeverity.WARNING);
      QAlertTypeMetaData effect = QAlertTypeMetaData.of(instance, ALERT_EFFECT_UNRESOLVED);
      assertThat(effect.getLabel()).isEqualTo("Job effect needs review");
      assertThat(effect.getDefaultSeverity()).isEqualTo(AlertSeverity.WARNING);
      assertThat(effect.getSubjectTableName()).isEqualTo(JOBS_JOB);
      assertThat(effect.getHowToFix()).isNotBlank();
      QAlertTypeMetaData reaper = QAlertTypeMetaData.of(instance, HEARTBEAT_REAPER);
      assertThat(reaper.getLabel()).isEqualTo("Job reaper stopped");
      assertThat(reaper.getDefaultMonitors()).singleElement().satisfies(monitor ->
      {
         assertThat(monitor.getKind()).isEqualTo(AlertMonitorKind.HEARTBEAT);
         assertThat(monitor.getDefinition()).containsEntry("heartbeatKey", HEARTBEAT_REAPER);
      });
   }

   /*******************************************************************************
    ** Configuration rejects unsafe retention and malformed schedules.
    *******************************************************************************/
   @Test
   void validateConfiguration() throws Exception
   {
      assertThatThrownBy(() -> new JobsQBitProducer().withQBitConfig(null).produce(instance)).hasMessageContaining("JobsQBitConfig is required");
      JobsQBitConfig bad = new JobsQBitConfig().withAppName(" ").withRetentionDays(0).withInputRetentionDays(-1)
         .withPurgeCronExpression("not cron").withHomeRefreshSeconds(0).withRecordRefreshSeconds(0).withSchedulerName("");
      List<String> errors = new ArrayList<>();
      bad.validate(instance, errors);
      assertThat(errors).hasSize(6);
      assertThatThrownBy(() -> new JobsQBitProducer().withQBitConfig(bad).produce(instance)).isInstanceOf(QException.class);
      config.setAppName("jobs2");
      config.setRetentionDays(20);
      config.setInputRetentionDays(3);
      config.setHomeRefreshSeconds(7);
      config.setRecordRefreshSeconds(4);
      config.setPurgeCronExpression("0 30 3 * * ?");
      config.setSchedulerName("daily");
      assertThat(config.getAppName()).isEqualTo("jobs2");
      assertThat(config.getRetentionDays()).isEqualTo(20);
      assertThat(config.getInputRetentionDays()).isEqualTo(3);
      assertThat(config.getHomeRefreshSeconds()).isEqualTo(7);
      assertThat(config.getRecordRefreshSeconds()).isEqualTo(4);
      assertThat(config.getSchedulerName()).isEqualTo("daily");
      assertThat(config.getPurgeCronExpression()).isEqualTo("0 30 3 * * ?");
      config.validate(instance);
      assertThat(new JobsQBitProducer().withQBitConfig(config).getQBitConfig()).isSameAs(config);
   }

   /*******************************************************************************
    ** Integration hooks expose only intended standard tables.
    *******************************************************************************/
   @Test
   void integrationLists()
   {
      assertThat(JobsIntegrations.esbTableNames()).containsExactly(JOBS_JOB);
      assertThat(JobsIntegrations.quickSearchTables()).containsExactly(JOBS_JOB);
      assertThat(JobsIntegrations.dataAnalysisTables()).containsExactly(JOBS_JOB, JOBS_CHUNK, JOBS_ROW_ERROR, JOBS_EFFECT, JOBS_EVENT, JOBS_WORKER);
   }

   /*******************************************************************************
    ** The jobs alert types pass the alerts validator (dotted, no underscores).
    *******************************************************************************/
   @Test
   void alertTypesValidate()
   {
      AlertsInstanceMetaData alerts = new AlertsInstanceMetaData();
      alerts.withAlertType(QAlertTypeMetaData.of(instance, ALERT_JOB_FAILED));
      alerts.withAlertType(QAlertTypeMetaData.of(instance, ALERT_JOB_STALLED));
      alerts.withAlertType(QAlertTypeMetaData.of(instance, ALERT_EFFECT_UNRESOLVED));
      QInstanceValidator validator = new QInstanceValidator();
      alerts.validate(instance, validator);
      assertThat(validator.getErrors()).isEmpty();
   }

   /*******************************************************************************
    ** Hosts can omit the effects panel by removing its table or join.
    *******************************************************************************/
   @Test
   void effectsTableAndJoinAreOptional() throws Exception
   {
      QInstance withoutTable = new QInstance();
      new DurableJobsMetaDataProvider().defineAll(withoutTable, "memory", null);
      withoutTable.withSupplementalMetaData(new DurableJobsMetaData());
      withoutTable.getTables().remove(JOBS_EFFECT);
      new JobsQBitProducer().produce(withoutTable);
      assertThat(withoutTable.getWidget("jobsJobEffects")).isNull();
      QInstance withoutJoin = new QInstance();
      new DurableJobsMetaDataProvider().defineAll(withoutJoin, "memory", null);
      withoutJoin.withSupplementalMetaData(new DurableJobsMetaData());
      withoutJoin.getJoins().remove(JobsCoreContract.childJoin(JOBS_EFFECT));
      new JobsQBitProducer().produce(withoutJoin);
      assertThat(withoutJoin.getWidget("jobsJobEffects")).isNull();
   }

   /*******************************************************************************
    ** Outbox metadata carries the core field defaults, references and labels.
    *******************************************************************************/
   @Test
   void effectsCoreContract() throws Exception
   {
      assertThat(instance.getTable(JOBS_EFFECT).getFields()).hasSize(26);
      assertThat(instance.getTable(JOBS_EFFECT).getField(JOB_ID).getPossibleValueSourceName()).isEqualTo(JOBS_JOB);
      assertThat(instance.getTable(JOBS_EFFECT).getField(CHUNK_ID).getPossibleValueSourceName()).isEqualTo(JOBS_CHUNK);
      assertThat(instance.getTable(JOBS_EFFECT).getField(NEEDS_REVIEW_FIELD).getDefaultValue()).isEqualTo(false);
      for(String field : List.of(ATTEMPT, RECONCILE_ATTEMPTS, LEASE_VERSION))
      {
         assertThat(instance.getTable(JOBS_EFFECT).getField(field).getDefaultValue()).isEqualTo(0);
      }
      assertThat(instance.getPossibleValueSource(JOBS_EFFECT_STATUS).getEnumValues()).extracting(value -> value.getLabel())
         .containsExactly("Pending", "Sending", "Sent", "Failed", "Unknown");
      assertThat(instance.getPossibleValueSource(JOBS_EFFECT_TYPE).getEnumValues()).extracting(value -> value.getLabel())
         .containsExactly("Local Transaction", "Idempotent External", "Non-Idempotent External");
   }
}
