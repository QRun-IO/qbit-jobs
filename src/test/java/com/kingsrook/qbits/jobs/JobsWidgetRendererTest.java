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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.jobs.widgets.JobsFormat;
import com.kingsrook.qbits.jobs.widgets.JobsWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.CompositeWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.StatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.progressbar.ProgressBarBlockData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextBlockData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED_WITH_ERRORS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FINISHED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EVENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LAST_PROGRESS_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PRIORITY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROCESS_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_IN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RUNNING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SEQ;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STARTED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS_REASON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TYPE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Real widget rendering, temporal buckets, empty states and owner isolation.
 *******************************************************************************/
class JobsWidgetRendererTest extends BaseJobsTest
{
   /*******************************************************************************
    ** Summary: two groups, each statistic labelled once, counts reflect status
    ** groups and the viewer's records, and the Now statistics link to the jobs.
    *******************************************************************************/
   @Test
   void summaryCountsAndOwnerIsolation() throws Exception
   {
      seed();
      QSession            viewer = login(USER_A, false);
      MultiStatisticsData data   = (MultiStatisticsData) render("jobsHomeSummary", null);
      assertThat(data.getTitle()).isNull();
      assertThat(data.getStatisticsGroupData()).extracting(MultiStatisticsData.StatisticsGroupData::getHeader).containsExactly("Now", "Last 24 hours");
      assertThat(data.getStatisticsGroupData()).allSatisfy(group -> assertThat(group.getStatisticList()).hasSize(3));
      assertThat(count(data, "Running")).isEqualTo(4);
      assertThat(count(data, "Queued")).isEqualTo(2);
      assertThat(count(data, "Needs attention")).isEqualTo(3);
      assertThat(count(data, "Completed")).isEqualTo(2);
      assertThat(count(data, "Rows processed")).isEqualTo(4050);
      assertThat(count(data, "Rows/min (last hour)")).isEqualTo(90);
      assertThat(URLDecoder.decode(stat(data, "Needs attention").getUrl(), StandardCharsets.UTF_8)).contains(JOBS_JOB, "FAILED", "NEEDS_REVIEW");
      assertThat(stat(data, "Completed").getUrl()).isNull();
      assertThat(QContext.getQSession()).isSameAs(viewer);
      assertThat(viewer.getSecurityKeyValues(JOBS_USER)).containsExactly(USER_A);
      login(USER_B, false);
      assertThat(count((MultiStatisticsData) render("jobsHomeSummary", null), "Running")).isEqualTo(1);
      login("admin", true);
      assertThat(count((MultiStatisticsData) render("jobsHomeSummary", null), "Running")).isEqualTo(5);
   }



   /*******************************************************************************
    ** Old completions do not count in current summary windows.
    *******************************************************************************/
   @Test
   void summaryExcludesOldData() throws Exception
   {
      QRecord old = job(USER_A, COMPLETED, now.minus(8, ChronoUnit.DAYS));
      update(JOBS_JOB, old.getValue(ID), LAST_PROGRESS_AT, now.minus(2, ChronoUnit.HOURS));
      job(USER_A, FAILED, now.minus(8, ChronoUnit.DAYS));
      login(USER_A, false);
      MultiStatisticsData data = (MultiStatisticsData) render("jobsHomeSummary", null);
      assertThat(count(data, "Completed")).isZero();
      assertThat(count(data, "Needs attention")).isZero();
      assertThat(count(data, "Rows processed")).isZero();
      assertThat(count(data, "Rows/min (last hour)")).isEqualTo(8);
   }



   /*******************************************************************************
    ** Three block columns; priority ordering; labels, links, bars and honest
    ** rate/ETA values; plain values kept on each row.
    *******************************************************************************/
   @Test
   void activeTable() throws Exception
   {
      instance.addProcess(new QProcessMetaData().withName("importOrders").withLabel("Import orders"));
      List<QRecord> jobs   = seed();
      QRecord       queued = jobs.get(2);
      update(JOBS_JOB, queued.getValue(ID), PRIORITY, 99);
      update(JOBS_JOB, queued.getValue(ID), STATUS_REASON, "Waiting for capacity");
      login(USER_A, false);
      TableData table = (TableData) render("jobsHomeActive", null);
      assertThat(table.getLabel()).isNull();
      assertThat(table.getColumns()).extracting(TableData.Column::getHeader).containsExactly("Job", "Progress", "Status");
      assertThat(table.getColumns()).allSatisfy(column -> assertThat(column.getWidth()).isNull());
      assertThat(table.getRows()).hasSize(6);

      Map<String, Object> first = table.getRows().getFirst();
      assertThat(first).containsEntry("Status", "Waiting for capacity").containsEntry("Progress", "45%").containsEntry("Rows", "450 / 1,000")
         .containsEntry("Process", "Import orders").containsEntry("Started", "Started 10m ago");
      assertThat(first.get("Path").toString()).endsWith("/" + queued.getValue(ID));
      CompositeWidgetData jobCell = (CompositeWidgetData) first.get(JobsWidgetRenderer.JOB_CELL);
      assertThat(texts(jobCell)).containsExactly(USER_A + " " + QUEUED, "Import orders");
      assertThat(jobCell.getBlocks().getFirst().getLink().getHref()).isEqualTo(first.get("Path"));
      CompositeWidgetData progressCell = (CompositeWidgetData) first.get(JobsWidgetRenderer.PROGRESS_CELL);
      ProgressBarBlockData bar = (ProgressBarBlockData) progressCell.getBlocks().getFirst();
      assertThat(bar.getValues().getPercent()).isEqualByComparingTo("45");
      assertThat(bar.getValues().getValue()).isEqualTo("45%");
      assertThat(texts((CompositeWidgetData) first.get(JobsWidgetRenderer.STATUS_CELL))).containsExactly("Waiting for capacity", "Started 10m ago");

      Map<String, Object> running = table.getRows().stream().filter(row -> "Running".equals(row.get("Status"))).findFirst().orElseThrow();
      assertThat(running).containsEntry("Rate", "45 rows/min").containsEntry("ETA", "in 12m");
      assertThat(texts((CompositeWidgetData) running.get(JobsWidgetRenderer.STATUS_CELL))).containsExactly("Running", "45 rows/min, ETA in 12m");
      assertThat(table.getRows()).extracting(row -> row.get("Status")).contains("Awaiting Confirmation", "Planning", "Pausing", "Cancelling");

      login(USER_B, false);
      assertThat(((TableData) render("jobsHomeActive", null)).getRows()).hasSize(1);
      login("admin", true);
      assertThat(((TableData) render("jobsHomeActive", null)).getRows()).hasSize(7);
   }



   /*******************************************************************************
    ** Missing totals fall back to chunks; missing times do not invent a rate;
    ** with neither total the cell says so instead of drawing a bar.
    *******************************************************************************/
   @Test
   void unknownProgressAndLimit() throws Exception
   {
      QRecord job = job(USER_A, RUNNING, null);
      update(JOBS_JOB, job.getValue(ID), ROWS_TOTAL, null);
      update(JOBS_JOB, job.getValue(ID), STARTED_AT, null);
      login(USER_A, false);
      TableData table = (TableData) render("jobsHomeActive", null);
      assertThat(table.getRows().getFirst()).containsEntry("Progress", "40%").containsEntry("Rows", "450 / Unknown")
         .containsEntry("Rate", "Not started").containsEntry("ETA", "Unknown").containsEntry("Process", "importOrders");
      QContext.setQSession(new QSystemUserSession());
      update(JOBS_JOB, job.getValue(ID), CHUNKS_TOTAL, null);
      for(int i = 0; i < 30; i++)
      {
         job(USER_A, QUEUED, null);
      }
      login(USER_A, false);
      table = (TableData) render("jobsHomeActive", null);
      assertThat(table.getRows()).hasSize(25);
      Map<String, Object> first = table.getRows().getFirst();
      assertThat(first).containsEntry("Progress", "Unknown");
      assertThat(texts((CompositeWidgetData) first.get(JobsWidgetRenderer.PROGRESS_CELL))).containsExactly("Unknown total", "450 / Unknown rows");
   }



   /*******************************************************************************
    ** Attention: failed and needs-review first, then completed with errors;
    ** old errors excluded; the error clipped to 120 characters on its own line.
    *******************************************************************************/
   @Test
   void attentionTable() throws Exception
   {
      seed();
      job(USER_A, FAILED, now.minus(8, ChronoUnit.DAYS));
      job(USER_A, COMPLETED_WITH_ERRORS, now.minus(1, ChronoUnit.MINUTES));
      job(USER_B, FAILED, now);
      login(USER_A, false);
      TableData data = (TableData) render("jobsHomeAttention", null);
      assertThat(data.getLabel()).isNull();
      assertThat(data.getColumns()).extracting(TableData.Column::getHeader).containsExactly("Job", "Status");
      assertThat(data.getRows()).hasSize(4);
      assertThat(data.getRows()).extracting(row -> row.get("Status"))
         .containsExactly("Failed", "Needs Review", "Completed With Errors", "Completed With Errors");
      assertThat(data.getRows().get(2)).containsEntry("Finished", "Finished 1m ago");
      assertThat(data.getRows()).allSatisfy(row ->
      {
         assertThat(row.get("Rows failed")).isEqualTo(10L);
         assertThat(row.get("Error").toString()).hasSize(120).endsWith(JobsFormat.ELLIPSIS);
         assertThat(texts((CompositeWidgetData) row.get(JobsWidgetRenderer.JOB_CELL))).hasSize(3).last().isEqualTo(row.get("Error"));
         assertThat(texts((CompositeWidgetData) row.get(JobsWidgetRenderer.STATUS_CELL))).contains("10 rows failed");
      });
      QContext.setQSession(new QSystemUserSession());
      update(JOBS_JOB, data.getRows().getFirst().get("Path").toString().replaceAll(".*/", ""), ROWS_FAILED, 0);
      login(USER_A, false);
      Map<String, Object> failedFirst = ((TableData) render("jobsHomeAttention", null)).getRows().getFirst();
      assertThat(texts((CompositeWidgetData) failedFirst.get(JobsWidgetRenderer.STATUS_CELL))).containsExactly("Failed", "Finished 30m ago");
      login(USER_B, false);
      assertThat(((TableData) render("jobsHomeAttention", null)).getRows()).hasSize(1);
      login("admin", true);
      assertThat(((TableData) render("jobsHomeAttention", null)).getRows()).hasSize(5);
   }



   /*******************************************************************************
    ** Throughput reads secured chunks, names series by process label and groups
    ** overflow processes as Other.
    *******************************************************************************/
   @Test
   void throughputGroupingAndIsolation() throws Exception
   {
      seed();
      for(int i = 0; i < 6; i++)
      {
         instance.addProcess(new QProcessMetaData().withName("process" + i).withLabel("Process " + i));
         QRecord job = job(USER_A, COMPLETED, now);
         update(JOBS_JOB, job.getValue(ID), PROCESS_NAME, "process" + i);
         insert(JOBS_CHUNK, new QRecord().withValue(JOB_ID, job.getValue(ID)).withValue(SEQ, 1)
            .withValue(STATUS, DONE).withValue(ROWS_IN, (i + 1) * 100).withValue(FINISHED_AT, now.minusSeconds(10)));
      }
      login(USER_A, false);
      ChartData chart = (ChartData) render("jobsHomeThroughput", null);
      assertThat(chart.getType()).isEqualTo(WidgetType.STACKED_BAR_CHART.getType());
      assertThat(chart.getTitle()).isNull();
      assertThat(chart.getChartData().getLabels()).hasSize(24).allSatisfy(label -> assertThat(label).matches("\\d\\d:00"));
      assertThat(chart.getChartData().getDatasets()).hasSize(6).extracting(ChartData.Data.Dataset::getLabel).contains("Process 5", "Other");
      assertThat(chartTotal(chart)).isEqualTo(3300);
      login(USER_B, false);
      assertThat(chartTotal((ChartData) render("jobsHomeThroughput", null))).isEqualTo(100);
      login("admin", true);
      assertThat(chartTotal((ChartData) render("jobsHomeThroughput", null))).isEqualTo(3400);
   }



   /*******************************************************************************
    ** Thirty daily buckets contain only recent terminal jobs, labelled by status.
    *******************************************************************************/
   @Test
   void history() throws Exception
   {
      seed();
      job(USER_A, COMPLETED, now.minus(40, ChronoUnit.DAYS));
      job(USER_B, FAILED, now);
      login(USER_A, false);
      ChartData chart = (ChartData) render("jobsHomeHistory", null);
      assertThat(chart.getTitle()).isNull();
      assertThat(chart.getChartData().getLabels()).hasSize(30).allSatisfy(label -> assertThat(label).matches("[A-Z][a-z]{2} \\d{1,2}"));
      assertThat(chart.getChartData().getDatasets()).extracting(ChartData.Data.Dataset::getLabel)
         .containsExactly("Completed", "Completed With Errors", "Failed", "Cancelled");
      assertThat(chartTotal(chart)).isEqualTo(4);
      login(USER_B, false);
      assertThat(chartTotal((ChartData) render("jobsHomeHistory", null))).isEqualTo(1);
      login("admin", true);
      assertThat(chartTotal((ChartData) render("jobsHomeHistory", null))).isEqualTo(5);
   }



   /*******************************************************************************
    ** Workers are permission-gated even when a caller bypasses widget dispatch.
    *******************************************************************************/
   @Test
   void workers() throws Exception
   {
      seed();
      login(USER_A, false);
      RenderWidgetInput input  = new RenderWidgetInput().withWidgetMetaData(instance.getWidget("jobsHomeWorkers"));
      TableData         denied = (TableData) new JobsWidgetRenderer().render(input).getWidgetData();
      assertThat(denied.getRows()).isEmpty();
      assertThat(denied.getNoRowsFoundHTML()).contains("jobs_admin");
      login("admin", true);
      TableData table = (TableData) render("jobsHomeWorkers", null);
      assertThat(table.getColumns()).extracting(TableData.Column::getHeader).containsExactly("Pod", "Capacity", "Last heartbeat");
      assertThat(table.getRows()).singleElement().satisfies(row ->
      {
         assertThat(row).containsEntry("Pod", "pod-1").containsEntry("Threads", "4 threads").containsEntry("Draining", "No");
         assertThat(row.get("Last heartbeat").toString()).matches("1[2-9]s ago");
         assertThat(row.get("Started").toString()).matches("Started [A-Z][a-z]{2} \\d{1,2}, \\d\\d:\\d\\d UTC");
         assertThat(texts((CompositeWidgetData) row.get(JobsWidgetRenderer.CAPACITY_CELL))).containsExactly("4 threads", "Draining: No");
      });
   }



   /*******************************************************************************
    ** Record progress and audit links never disclose another owner's job.
    *******************************************************************************/
   @Test
   void progressAndAudit() throws Exception
   {
      List<QRecord> jobs = seed();
      String id = jobs.get(3).getValueString(ID);
      login(USER_A, false);
      MultiStatisticsData progress = (MultiStatisticsData) render("jobsJobProgress", id);
      assertThat(progress.getStatisticsGroupData()).extracting(MultiStatisticsData.StatisticsGroupData::getHeader).containsExactly("Rows", "Chunks", "Speed");
      assertThat(groupStat(progress, "Rows", "Succeeded")).isEqualTo(440);
      assertThat(groupStat(progress, "Rows", "Failed")).isEqualTo(10);
      assertThat(groupStat(progress, "Rows", "Total")).isEqualTo(1000);
      assertThat(group(progress, "Rows").getSubheader()).isEqualTo("45% done");
      assertThat(groupStat(progress, "Chunks", "Done")).isEqualTo(4);
      assertThat(groupStat(progress, "Speed", "Rows per minute")).isEqualTo(45);
      assertThat(group(progress, "Speed").getSubheader()).isEqualTo("ETA in 12m");
      String completed = jobs.get(8).getValueString(ID);
      assertThat(group((MultiStatisticsData) render("jobsJobProgress", completed), "Speed").getSubheader()).isEqualTo("Completed 30m ago");
      String paused = jobs.get(5).getValueString(ID);
      assertThat(group((MultiStatisticsData) render("jobsJobProgress", paused), "Speed").getSubheader()).isEqualTo("Paused");
      QContext.setQSession(new QSystemUserSession());
      update(JOBS_JOB, jobs.get(5).getValue(ID), ROWS_TOTAL, null);
      update(JOBS_JOB, jobs.get(5).getValue(ID), CHUNKS_TOTAL, null);
      login(USER_A, false);
      MultiStatisticsData unknown = (MultiStatisticsData) render("jobsJobProgress", paused);
      assertThat(group(unknown, "Rows").getSubheader()).isEqualTo("Total not known yet");
      assertThat(group(unknown, "Rows").getStatisticList()).hasSize(2);
      assertThat(group(unknown, "Chunks").getStatisticList()).hasSize(2);
      StatisticsData audit = (StatisticsData) render("jobsJobAudit", id);
      assertThat(audit.getCount()).isEqualTo(1);
      assertThat(URLDecoder.decode(audit.getCountURL(), StandardCharsets.UTF_8)).contains("CONTAINS", "job:" + id + "/", "message");
      login(USER_B, false);
      assertThat(((MultiStatisticsData) render("jobsJobProgress", id)).getStatisticsGroupData()).isEmpty();
      assertThat(((StatisticsData) render("jobsJobAudit", id)).getCount()).isEqualTo(0);
      assertThat(((StatisticsData) render("jobsJobAudit", id)).getCountURL()).isNull();
      login("admin", true);
      assertThat(((StatisticsData) render("jobsJobAudit", id)).getCount()).isEqualTo(1);
   }

   /*******************************************************************************
    ** Child renderer ordering, owner locks and parent-record selection.
    *******************************************************************************/
   @Test
   void childrenUseCoreRenderer() throws Exception
   {
      QRecord job = job(USER_A, RUNNING, null);
      children(job);
      insert(JOBS_CHUNK, new QRecord().withValue(JOB_ID, job.getValue(ID)).withValue(SEQ, 0).withValue(STATUS, DONE));
      insert(JOBS_EVENT, new QRecord().withValue(JOB_ID, job.getValue(ID)).withValue(TYPE, RUNNING));
      login(USER_A, false);
      String id = job.getValueString(ID);
      ChildRecordListData chunks = (ChildRecordListData) render("jobsJobChunks", id);
      assertThat(chunks.getQueryOutput().getRecords()).extracting(record -> record.getValueInteger(SEQ)).containsExactly(0, 1);
      ChildRecordListData errors = (ChildRecordListData) render("jobsJobRowErrors", id);
      assertThat(errors.getTotalRows()).isEqualTo(1);
      ChildRecordListData events = (ChildRecordListData) render("jobsJobEvents", id);
      assertThat(events.getQueryOutput().getRecords()).extracting(record -> record.getValueString(TYPE)).containsExactly(RUNNING, COMPLETED);
      login(USER_B, false);
      assertThatThrownBy(() -> render("jobsJobChunks", id)).isInstanceOf(QException.class);
      login("admin", true);
      assertThat(((ChildRecordListData) render("jobsJobChunks", id)).getTotalRows()).isEqualTo(2);
   }

   /*******************************************************************************
    ** Empty widgets still return typed, renderable payloads.
    *******************************************************************************/
   @Test
   void emptyStates() throws Exception
   {
      login(USER_A, false);
      assertThat(((TableData) render("jobsHomeActive", null)).getNoRowsFoundHTML()).isEqualTo("No jobs are running");
      assertThat(((TableData) render("jobsHomeAttention", null)).getRows()).isEmpty();
      assertThat(count((MultiStatisticsData) render("jobsHomeSummary", null), "Running")).isZero();
      assertThat(chartTotal((ChartData) render("jobsHomeThroughput", null))).isZero();
      assertThat(chartTotal((ChartData) render("jobsHomeHistory", null))).isZero();
      assertThat(((MultiStatisticsData) render("jobsJobProgress", null)).getStatisticsGroupData()).isEmpty();
      assertThat(((StatisticsData) render("jobsJobAudit", null)).getCount()).isEqualTo(0);
      login("admin", true);
      assertThat(((TableData) render("jobsHomeWorkers", null)).getNoRowsFoundHTML()).isEqualTo("No workers have registered");
      assertThatThrownBy(() -> new JobsWidgetRenderer().render(new RenderWidgetInput().withWidgetMetaData(new QWidgetMetaData().withName("unknown"))))
         .hasMessageContaining("Unknown Jobs widget");
   }

   /*******************************************************************************
    ** Review counts belong only to listed jobs; core controls attention eligibility.
    *******************************************************************************/
   @Test
   void attentionEffectsAndOwnerIsolation() throws Exception
   {
      QRecord review = job(USER_A, NEEDS_REVIEW, now);
      effect(review, UNKNOWN, true);
      effect(review, UNKNOWN, true);
      effect(review, PENDING, false);
      effect(job(USER_A, RUNNING, now), UNKNOWN, true);
      QRecord other = job(USER_B, FAILED, now.minusSeconds(60));
      effect(other, UNKNOWN, true);
      QSession viewer = login(USER_A, false);
      TableData attention = (TableData) render("jobsHomeAttention", null);
      assertThat(attention.getRows()).hasSize(1);
      assertThat(texts((CompositeWidgetData) attention.getRows().getFirst().get(JobsWidgetRenderer.STATUS_CELL))).contains("2 effects need review");
      assertThat(QContext.getQSession()).isSameAs(viewer);
      login(USER_B, false);
      attention = (TableData) render("jobsHomeAttention", null);
      assertThat(attention.getRows()).hasSize(1);
      assertThat(texts((CompositeWidgetData) attention.getRows().getFirst().get(JobsWidgetRenderer.STATUS_CELL)))
         .contains("1 effects need review").doesNotContain("2 effects need review");
      assertThat(records(JOBS_EFFECT)).hasSize(1).allSatisfy(record -> assertThat(record.getValue(JOB_ID)).isEqualTo(other.getValue(ID)));
   }

   /*******************************************************************************
    ** Effects use disjoint statistics and the secured core child renderer.
    *******************************************************************************/
   @Test
   void effectsProgressAndChildList() throws Exception
   {
      QRecord job = job(USER_A, NEEDS_REVIEW, now);
      for(String status : List.of(SENT, SENT, PENDING, SENDING, UNKNOWN, FAILED))
      {
         effect(job, status, false);
      }
      QRecord firstReview = effect(job, UNKNOWN, true);
      effect(job, PENDING, true);
      effect(job(USER_B, RUNNING, null), SENT, false);
      String id = job.getValueString(ID);
      QSession viewer = login(USER_A, false);
      MultiStatisticsData progress = (MultiStatisticsData) render("jobsJobProgress", id);
      assertThat(progress.getStatisticsGroupData()).extracting(MultiStatisticsData.StatisticsGroupData::getHeader)
         .containsExactly("Rows", "Chunks", "Speed", "Effects");
      assertThat(groupStat(progress, "Effects", "Sent")).isEqualTo(2);
      assertThat(groupStat(progress, "Effects", "Pending")).isEqualTo(3);
      assertThat(groupStat(progress, "Effects", "Failed")).isEqualTo(1);
      assertThat(groupStat(progress, "Effects", "Needs review")).isEqualTo(2);
      ChildRecordListData effects = (ChildRecordListData) render("jobsJobEffects", id);
      assertThat(effects.getTotalRows()).isEqualTo(8);
      assertThat(effects.getQueryOutput().getRecords()).extracting(record -> record.getValueInteger(ID)).isSorted();
      assertThat(effects.getQueryOutput().getRecords()).extracting(record -> record.getValue(ID)).contains(firstReview.getValue(ID));
      assertThat(QContext.getQSession()).isSameAs(viewer);
      login(USER_B, false);
      assertThat(((MultiStatisticsData) render("jobsJobProgress", id)).getStatisticsGroupData()).isEmpty();
      assertThatThrownBy(() -> render("jobsJobEffects", id)).isInstanceOf(QException.class);
      login("admin", true);
      assertThat(((ChildRecordListData) render("jobsJobEffects", id)).getTotalRows()).isEqualTo(8);
   }

   /*******************************************************************************
    ** RC.5 instances still render attention and progress without an effects table.
    *******************************************************************************/
   @Test
   void widgetsWithoutEffectsTable() throws Exception
   {
      QRecord job = job(USER_A, FAILED, now);
      instance.getTables().remove(JOBS_EFFECT);
      login(USER_A, false);
      assertThat(((TableData) render("jobsHomeAttention", null)).getRows()).hasSize(1);
      assertThat(((MultiStatisticsData) render("jobsJobProgress", job.getValueString(ID))).getStatisticsGroupData())
         .extracting(MultiStatisticsData.StatisticsGroupData::getHeader).containsExactly("Rows", "Chunks", "Speed");
   }

   /*******************************************************************************
    ** Test fixture update.
    *******************************************************************************/
   private void update(String table, Serializable id, String field, Serializable value) throws QException
   {
      new UpdateAction().execute(new UpdateInput().withTableName(table).withRecord(new QRecord().withValue(ID, id).withValue(field, value)));
   }

   /*******************************************************************************
    ** Find a group by its header.
    *******************************************************************************/
   private MultiStatisticsData.StatisticsGroupData group(MultiStatisticsData data, String header)
   {
      return (data.getStatisticsGroupData().stream().filter(group -> header.equals(group.getHeader())).findFirst().orElseThrow());
   }



   /*******************************************************************************
    ** Find a statistic by its label in any group (labels are unique on the home).
    *******************************************************************************/
   private MultiStatisticsData.StatisticsGroupData.Statistic stat(MultiStatisticsData data, String label)
   {
      return (data.getStatisticsGroupData().stream().flatMap(group -> group.getStatisticList().stream())
         .filter(statistic -> label.equals(statistic.getLabel())).findFirst().orElseThrow());
   }



   /*******************************************************************************
    ** Numeric statistic value by label.
    *******************************************************************************/
   private int count(MultiStatisticsData data, String label)
   {
      return (stat(data, label).getValue());
   }



   /*******************************************************************************
    ** Numeric statistic value by group header and label.
    *******************************************************************************/
   private int groupStat(MultiStatisticsData data, String header, String label)
   {
      return (group(data, header).getStatisticList().stream().filter(statistic -> label.equals(statistic.getLabel())).findFirst().orElseThrow().getValue());
   }



   /*******************************************************************************
    ** The text of each text block in a composite cell, in order.
    *******************************************************************************/
   private List<String> texts(CompositeWidgetData cell)
   {
      return (cell.getBlocks().stream().filter(block -> block instanceof TextBlockData)
         .map(block -> ((TextBlockData) block).getValues().getText()).toList());
   }



   /*******************************************************************************
    ** Total over all chart buckets and series.
    *******************************************************************************/
   private long chartTotal(ChartData chart)
   {
      return (chart.getChartData().getDatasets().stream().flatMap(dataset -> dataset.getData().stream()).mapToLong(Number::longValue).sum());
   }
}
