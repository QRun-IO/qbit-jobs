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


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.kingsrook.qbits.jobs.processes.JobsPurgeFinishedStep;
import com.kingsrook.qbits.jobs.widgets.JobsChildRecordListRenderer;
import com.kingsrook.qbits.jobs.widgets.JobsWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.ChildRecordListRenderer;
import com.kingsrook.qqq.backend.core.actions.permissions.UseOtherPermissionNameCustomPermissionChecker;
import com.kingsrook.qqq.backend.core.alerts.AlertDefaultMonitor;
import com.kingsrook.qqq.backend.core.alerts.AlertSeverity;
import com.kingsrook.qqq.backend.core.alerts.AlertsInstanceMetaData;
import com.kingsrook.qqq.backend.core.alerts.QAlertTypeMetaData;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.DenyBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_EFFECT_UNRESOLVED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_JOB_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALERT_JOB_STALLED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ALL_TABLES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.AUDIT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.HEARTBEAT_REAPER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ADMIN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_LOG;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ROW_ERROR;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LEVEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LOGGED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PHASE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROW_NUMBER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEQ;


/*******************************************************************************
 ** Jobs app entry point. Core must define its durable jobs tables first.
 *******************************************************************************/
public class JobsQBitProducer implements QBitMetaDataProducer<JobsQBitConfig>
{
   public static final String QBIT_NAME = "com.kingsrook.qbits:qbit-jobs:jobs";
   public static final String PURGE_PROCESS = "jobsPurgeFinished";
   public static final List<String> HOME_WIDGETS = List.of("jobsHomeSummary", "jobsHomeActive", "jobsHomeThroughput", "jobsHomeAttention", "jobsHomeWorkers", "jobsHomeHistory");
   private JobsQBitConfig qBitConfig = new JobsQBitConfig();

   /*******************************************************************************
    ** Configure this instance of the app.
    *******************************************************************************/
   public JobsQBitProducer withQBitConfig(JobsQBitConfig config)
   {
      qBitConfig = config;
      return (this);
   }

   /*******************************************************************************
    ** Host settings.
    *******************************************************************************/
   @Override
   public JobsQBitConfig getQBitConfig()
   {
      return (qBitConfig);
   }

   /*******************************************************************************
    ** Stable namespace, independent of the host's navigation label.
    *******************************************************************************/
   @Override
   public String getNamespace()
   {
      return ("jobs");
   }

   /*******************************************************************************
    ** Artifact identifier with the Maven-filtered version.
    *******************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      return (new QBitMetaData().withGroupId("com.kingsrook.qbits").withArtifactId("qbit-jobs")
         .withVersion(version()).withNamespace(getNamespace()).withConfig(qBitConfig));
   }

   /*******************************************************************************
    ** Read a packaged resource, never a filesystem file.
    *******************************************************************************/
   private static String version()
   {
      try(InputStream stream = JobsQBitProducer.class.getResourceAsStream("/jobs.properties"))
      {
         Properties properties = new Properties();
         if(stream != null)
         {
            properties.load(stream);
         }
         return (properties.getProperty("version", "unknown"));
      }
      catch(IOException e)
      {
         throw (new IllegalStateException("Cannot read qbit-jobs version resource", e));
      }
   }

   /*******************************************************************************
    ** Validate first, then register presentation, alerts and retention. No tables
    ** are added. Return empty output because registration has already occurred.
    *******************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance instance) throws QException
   {
      if(ALL_TABLES.stream().anyMatch(table -> instance.getTable(table) == null))
      {
         throw (new QException("Durable jobs are not enabled: call DurableJobsMetaDataProvider.defineAll(...) before producing qbit-jobs"));
      }
      if(qBitConfig == null)
      {
         throw (new QException("JobsQBitConfig is required"));
      }
      qBitConfig.validate(instance);
      instance.addQBit(getQBitMetaData());
      registerWidget(instance, "jobsHomeSummary", "Overview", WidgetType.MULTI_STATISTICS, qBitConfig.getHomeRefreshSeconds());
      registerWidget(instance, "jobsHomeActive", "Running and queued", WidgetType.TABLE, qBitConfig.getHomeRefreshSeconds());
      registerWidget(instance, "jobsHomeThroughput", "Rows per hour", WidgetType.STACKED_BAR_CHART, qBitConfig.getHomeRefreshSeconds());
      registerWidget(instance, "jobsHomeAttention", "Needs attention", WidgetType.TABLE, qBitConfig.getHomeRefreshSeconds());
      QWidgetMetaData workers = registerWidget(instance, "jobsHomeWorkers", "Workers", WidgetType.TABLE, qBitConfig.getHomeRefreshSeconds());
      workers.withPermissionRules(adminRules());
      registerWidget(instance, "jobsHomeHistory", "Job history", WidgetType.STACKED_BAR_CHART, qBitConfig.getHomeRefreshSeconds());
      for(String name : HOME_WIDGETS)
      {
         if(!"jobsHomeWorkers".equals(name))
         {
            instance.getWidget(name).setPermissionRules(appAccessRules());
         }
      }
      QAppMetaData app = new QAppMetaData().withName(qBitConfig.getAppName()).withLabel("Jobs").withIcon(new QIcon("pending_actions"))
         .withChild(instance.getTable(JOBS_JOB)).withWidgets(new ArrayList<>(HOME_WIDGETS))
         .withPermissionRules(appAccessRules());
      app.setParentAppName(qBitConfig.getParentAppName());
      instance.addApp(app);
      if(qBitConfig.getParentAppName() != null)
      {
         instance.getApp(qBitConfig.getParentAppName()).withChild(app);
      }
      registerWidget(instance, "jobsJobProgress", "Live Progress", WidgetType.MULTI_STATISTICS, qBitConfig.getRecordRefreshSeconds());
      addRecordSection(instance, "jobsJobProgress", "Live Progress");
      if(instance.getTable(JOBS_LOG) != null && instance.getJoin(JobsCoreContract.childJoin(JOBS_LOG)) != null)
      {
         child(instance, JOBS_LOG, "jobsJobLog", "Log", ID, false);
      }
      child(instance, JOBS_CHUNK, "jobsJobChunks", "Chunks", SEQ, true);
      child(instance, JOBS_ROW_ERROR, "jobsJobRowErrors", "Row Errors", ROW_NUMBER, true);
      if(instance.getTable(JOBS_EFFECT) != null && instance.getJoin(JobsCoreContract.childJoin(JOBS_EFFECT)) != null)
      {
         child(instance, JOBS_EFFECT, "jobsJobEffects", "Effects", ID, true);
      }
      child(instance, JOBS_EVENT, "jobsJobEvents", "Events", ID, false);
      if(instance.getTable(AUDIT) != null)
      {
         registerWidget(instance, "jobsJobAudit", "Changes made by this job", WidgetType.STATISTICS, qBitConfig.getRecordRefreshSeconds());
         addRecordSection(instance, "jobsJobAudit", "Changes made by this job");
      }
      registerRetention(instance);
      registerAlerts(instance);
      return (new MetaDataProducerMultiOutput());
   }

   /*******************************************************************************
    ** Every panel uses flexible grid sizing and typed data.
    *******************************************************************************/
   private QWidgetMetaData registerWidget(QInstance instance, String name, String label, WidgetType type, int refresh) throws QException
   {
      QWidgetMetaData widget = new QWidgetMetaData().withName(name).withLabel(label).withType(type.getType()).withGridColumns(12)
         .withCodeReference(new QCodeReference(JobsWidgetRenderer.class)).withDefaultValue("refreshSeconds", refresh);
      instance.addWidget(widget);
      return (widget);
   }

   /*******************************************************************************
    ** Use the core builder, retaining parent/child security and ordering.
    *******************************************************************************/
   private void child(QInstance instance, String table, String name, String label, String order, boolean ascending) throws QException
   {
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(instance.getJoin(JobsCoreContract.childJoin(table)))
         .withOrderBys(List.of(new QFilterOrderBy(order, ascending))).withMaxRows(25).getWidgetMetaData()
         .withName(name).withLabel(label).withGridColumns(12).withCodeReference(new QCodeReference(JobsChildRecordListRenderer.class))
         .withDefaultValue("refreshSeconds", qBitConfig.getRecordRefreshSeconds());
      if(JOBS_LOG.equals(table))
      {
         widget.withDefaultValue(ChildRecordListRenderer.KEY_ONLY_INCLUDE_FIELD_NAMES, new ArrayList<>(List.of(LEVEL, LOGGED_AT, PHASE, MESSAGE)));
      }
      instance.addWidget(widget);
      addRecordSection(instance, name, label);
   }

   /*******************************************************************************
    ** Append record panels without replacing core's fields or action sections.
    *******************************************************************************/
   private void addRecordSection(QInstance instance, String widgetName, String label)
   {
      instance.getTable(JOBS_JOB).withSection(new QFieldSection().withName(widgetName).withLabel(label)
         .withTier(Tier.T2).withWidgetName(widgetName).withGridColumns(12));
   }

   /*******************************************************************************
    ** The Jobs app and its home widgets require "<appName>.hasAccess"; users
    ** without it never see the app or its widgets (hidden, not locked).
    *******************************************************************************/
   private QPermissionRules appAccessRules()
   {
      return (new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION)
         .withPermissionBaseName(qBitConfig.getAppName()).withDenyBehavior(DenyBehavior.HIDDEN));
   }

   /*******************************************************************************
    ** Match the exact core permission, without a .hasAccess suffix.
    *******************************************************************************/
   private static QPermissionRules adminRules()
   {
      return (new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION)
         .withCustomPermissionChecker(UseOtherPermissionNameCustomPermissionChecker.build(JOBS_ADMIN)));
   }

   /*******************************************************************************
    ** Hidden system maintenance; a scheduler is explicitly selected by the host.
    *******************************************************************************/
   private void registerRetention(QInstance instance) throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName(PURGE_PROCESS).withLabel("Purge finished jobs")
         .withIsHidden(true).withPermissionRules(adminRules()).withSourceQBitName(QBIT_NAME)
         .withStep(new QBackendStepMetaData().withName("purge").withCode(new QCodeReference(JobsPurgeFinishedStep.class)));
      if(qBitConfig.getSchedulerName() != null)
      {
         process.withSchedule(new QScheduleMetaData().withSchedulerName(qBitConfig.getSchedulerName())
            .withCronExpression(qBitConfig.getPurgeCronExpression()).withCronTimeZoneId("UTC"));
      }
      instance.addProcess(process);
   }

   /*******************************************************************************
    ** Type declarations consumed by qbit-alerts; core publishes the signals.
    *******************************************************************************/
   private void registerAlerts(QInstance instance)
   {
      AlertsInstanceMetaData.ofOrWithNew(instance)
         .withAlertType(new QAlertTypeMetaData().withName(ALERT_JOB_FAILED).withLabel("Job failed").withDefaultSeverity(AlertSeverity.ERROR)
            .withSubjectTableName(JOBS_JOB).withSourceQBitName(QBIT_NAME).withHowToFix("Inspect the job error and row errors, correct the cause, then retry failed work."))
         .withAlertType(new QAlertTypeMetaData().withName(ALERT_JOB_STALLED).withLabel("Job stalled").withDefaultSeverity(AlertSeverity.WARNING)
            .withSubjectTableName(JOBS_JOB).withSourceQBitName(QBIT_NAME).withHowToFix("Check worker heartbeats, storage access and chunk leases; restore workers before retrying."))
         .withAlertType(new QAlertTypeMetaData().withName(ALERT_EFFECT_UNRESOLVED).withLabel("Job effect needs review").withDefaultSeverity(AlertSeverity.WARNING)
            .withSubjectTableName(JOBS_JOB).withSourceQBitName(QBIT_NAME).withHowToFix("Review the effect and confirm its outcome with the provider before retrying."))
         .withAlertType(new QAlertTypeMetaData().withName(HEARTBEAT_REAPER).withLabel("Job reaper stopped").withDefaultSeverity(AlertSeverity.ERROR)
            .withSourceQBitName(QBIT_NAME).withVisibilityPermission(JOBS_ADMIN).withDescription("No pod is reaping leases.")
            .withHowToFix("Restore a durable jobs runtime so expired chunk leases are reclaimed.")
            .withDefaultMonitor(AlertDefaultMonitor.heartbeat(HEARTBEAT_REAPER, HEARTBEAT_REAPER, 60, 120).withScheduleSeconds(60)));
   }
}
