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


package com.kingsrook.qbits.jobs.widgets;


import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsSecurity;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.CompositeWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.StatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockLink;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.progressbar.ProgressBarBlockData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.progressbar.ProgressBarValues;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextBlockData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextValues;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ATTENTION_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.AUDIT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.CHUNKS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.COMPLETED_WITH_ERRORS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DRAINING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ERROR_MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.FINISHED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.HEARTBEAT_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ADMIN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_CHUNK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_EFFECT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_JOB;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_WORKER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOB_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LABEL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.LAST_PROGRESS_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.MESSAGE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.NEEDS_REVIEW_FIELD;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.POD_ID;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PRIORITY;
import static com.kingsrook.qbits.jobs.JobsCoreContract.PROCESS_NAME;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.QUEUED_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_DONE;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_FAILED;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_IN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_OK;
import static com.kingsrook.qbits.jobs.JobsCoreContract.ROWS_TOTAL;
import static com.kingsrook.qbits.jobs.JobsCoreContract.RUNNING_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENDING;
import static com.kingsrook.qbits.jobs.JobsCoreContract.SENT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STARTED_AT;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.STATUS_REASON;
import static com.kingsrook.qbits.jobs.JobsCoreContract.TERMINAL_STATUSES;
import static com.kingsrook.qbits.jobs.JobsCoreContract.THREADS;
import static com.kingsrook.qbits.jobs.JobsCoreContract.UNKNOWN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.statusLabel;


/*******************************************************************************
 ** Typed dashboard and record panels, always queried in the viewer's context.
 ** Multi-line table cells are composite widget blocks (text with a record link,
 ** progress bars); each row also carries its plain values under readable keys.
 ** No HTML anywhere. The widget label is the card title, so the data itself
 ** carries no title.
 *******************************************************************************/
public class JobsWidgetRenderer extends AbstractWidgetRenderer
{
   public static final String JOB_CELL      = "jobCell";
   public static final String PROGRESS_CELL = "progressCell";
   public static final String STATUS_CELL   = "statusCell";
   public static final String POD_CELL      = "podCell";
   public static final String CAPACITY_CELL = "capacityCell";

   private static final String SMALL = "13";



   /*******************************************************************************
    ** Dispatch the named panel without changing the current session.
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      DurableJobsSecurity.ensureSessionKeys(QContext.getQSession());
      Instant now = Instant.now();
      QWidgetData data = switch(input.getWidgetMetaData().getName())
      {
         case "jobsHomeSummary" -> summary(now);
         case "jobsHomeActive" -> active(now);
         case "jobsHomeThroughput" -> throughput(now);
         case "jobsHomeAttention" -> attention(now);
         case "jobsHomeWorkers" -> workers(now);
         case "jobsHomeHistory" -> history(now);
         case "jobsJobProgress" -> progress(record(input), now);
         case "jobsJobAudit" -> audit(record(input));
         default -> throw new QException("Unknown Jobs widget: " + input.getWidgetMetaData().getName());
      };
      return (new RenderWidgetOutput(data));
   }



   /*******************************************************************************
    ** Read through QueryAction so core's record locks apply, including child joins.
    *******************************************************************************/
   public static List<QRecord> query(String table, QQueryFilter filter) throws QException
   {
      DurableJobsSecurity.ensureSessionKeys(QContext.getQSession());
      return (new QueryAction().execute(new QueryInput().withTableName(table).withFilter(filter)
         .withShouldFetchHeavyFields(true)).getRecords());
   }



   /*******************************************************************************
    ** The label of a process, falling back to its name when it is not defined here.
    *******************************************************************************/
   public static String processLabel(String processName)
   {
      if(processName == null)
      {
         return ("Unknown process");
      }
      QProcessMetaData process = QContext.getQInstance().getProcess(processName);
      return (process == null || process.getLabel() == null ? processName : process.getLabel());
   }



   /*******************************************************************************
    ** Resolve the same id parameter used by core's ChildRecordListRenderer.
    ** Verify ownership before querying audit records, which may have broader locks.
    *******************************************************************************/
   private QRecord record(RenderWidgetInput input) throws QException
   {
      String primaryKey = QContext.getQInstance().getTable(JOBS_JOB).getPrimaryKeyField();
      String id         = input.getQueryParams().getOrDefault(ID, input.getQueryParams().get(primaryKey));
      if(id == null || id.isBlank())
      {
         return (null);
      }
      List<QRecord> records = query(JOBS_JOB, new QQueryFilter().withCriteria(primaryKey, QCriteriaOperator.EQUALS, id).withLimit(1));
      return (records.isEmpty() ? null : records.getFirst());
   }



   /*******************************************************************************
    ** Restrict to the named statuses.
    *******************************************************************************/
   private static QQueryFilter statuses(List<String> values)
   {
      return (new QQueryFilter().withCriteria(STATUS, QCriteriaOperator.IN, values));
   }



   /*******************************************************************************
    ** Recent finish filter shared by attention, history and summary.
    *******************************************************************************/
   private static QQueryFilter finishedSince(Instant since)
   {
      return (new QQueryFilter().withCriteria(FINISHED_AT, QCriteriaOperator.GREATER_THAN_OR_EQUALS, since));
   }



   /*******************************************************************************
    ** Two groups: what is happening now (each linking to the matching jobs), and
    ** the last 24 hours. Every statistic shows its label once and a whole number.
    *******************************************************************************/
   private MultiStatisticsData summary(Instant now) throws QException
   {
      Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withSubFilter(statuses(RUNNING_STATUSES)).withSubFilter(statuses(QUEUED_STATUSES))
         .withSubFilter(finishedSince(weekAgo))
         .withSubFilter(new QQueryFilter().withCriteria(LAST_PROGRESS_AT, QCriteriaOperator.GREATER_THAN_OR_EQUALS, now.minus(1, ChronoUnit.HOURS)));
      long running   = 0;
      long queued    = 0;
      long attention = 0;
      long completed = 0;
      long rows      = 0;
      long hourRows  = 0;
      for(QRecord job : query(JOBS_JOB, filter))
      {
         String  status = job.getValueString(STATUS);
         boolean live   = RUNNING_STATUSES.contains(status);
         boolean day    = since(job, FINISHED_AT, now.minus(1, ChronoUnit.DAYS));
         running += live ? 1 : 0;
         queued += QUEUED_STATUSES.contains(status) ? 1 : 0;
         attention += ATTENTION_STATUSES.contains(status) && since(job, FINISHED_AT, weekAgo) ? 1 : 0;
         completed += (COMPLETED.equals(status) || COMPLETED_WITH_ERRORS.equals(status)) && day ? 1 : 0;
         rows += live || day ? number(job, ROWS_DONE) : 0;
         hourRows += since(job, LAST_PROGRESS_AT, now.minus(1, ChronoUnit.HOURS)) ? number(job, ROWS_DONE) : 0;
      }

      MultiStatisticsData.StatisticsGroupData nowGroup = new MultiStatisticsData.StatisticsGroupData().withHeader("Now").withStatisticList(List.of(
         statistic("Running", running, jobsUrl(statuses(RUNNING_STATUSES))),
         statistic("Queued", queued, jobsUrl(statuses(QUEUED_STATUSES))),
         statistic("Needs attention", attention, jobsUrl(statuses(ATTENTION_STATUSES).withSubFilter(finishedSince(weekAgo))))));
      MultiStatisticsData.StatisticsGroupData dayGroup = new MultiStatisticsData.StatisticsGroupData().withHeader("Last 24 hours").withStatisticList(List.of(
         statistic("Completed", completed, null),
         statistic("Rows processed", rows, null),
         statistic("Rows/min (last hour)", Math.round(hourRows / 60.0), null)));
      return (new MultiStatisticsData(null, List.of(nowGroup, dayGroup)));
   }



   /*******************************************************************************
    ** Active jobs, ordered by priority then queued time and capped at 25.
    ** Three columns, so it reads at phone width: job, progress, status.
    *******************************************************************************/
   private TableData active(Instant now) throws QException
   {
      List<String> activeStatuses = new ArrayList<>(RUNNING_STATUSES);
      activeStatuses.addAll(QUEUED_STATUSES);
      List<Map<String, Object>> rows = new ArrayList<>();
      for(QRecord job : query(JOBS_JOB, statuses(activeStatuses).withOrderBy(new QFilterOrderBy(PRIORITY, false))
         .withOrderBy(new QFilterOrderBy(QUEUED_AT)).withLimit(25)))
      {
         String  status  = job.getValueString(STATUS);
         String  reason  = job.getValueString(STATUS_REASON);
         boolean waiting = QUEUED_STATUSES.contains(status) && reason != null && !reason.isBlank();
         String  state   = waiting ? reason : statusLabel(status);
         String  percent = percent(job);
         String  rowText = count(job, ROWS_DONE) + " / " + total(job, ROWS_TOTAL);
         String  rate    = job.getValue(STARTED_AT) == null ? "Not started" : JobsFormat.count(Math.round(rate(job, now))) + " rows/min";
         String  eta     = eta(job, now);
         String  started = job.getValue(STARTED_AT) == null ? "Not started" : "Started " + JobsFormat.time(job.getValueInstant(STARTED_AT), now);

         Map<String, Object> row = row("Job", job.getValueString(LABEL), "Process", processLabel(job.getValueString(PROCESS_NAME)),
            "Status", state, "Progress", percent, "Rows", rowText, "Rate", rate, "ETA", eta, "Started", started, "Path", jobPath(job));

         CompositeWidgetData progress = column();
         if(percentValue(job) != null)
         {
            progress.withBlock(new ProgressBarBlockData().withValues(new ProgressBarValues().withPercent(percentValue(job)).withValue(percent)));
         }
         else
         {
            progress.withBlock(text("Unknown total", null, null));
         }
         progress.withBlock(small(rowText + " rows"));

         row.put(JOB_CELL, jobCell(job, null));
         row.put(PROGRESS_CELL, progress);
         row.put(STATUS_CELL, column().withBlock(text(state, "semibold", null))
            .withBlock(small(RUNNING_STATUSES.contains(status) ? rate + ", ETA " + eta : started)));
         rows.add(row);
      }
      return (table("No jobs are running", rows, col("Job", JOB_CELL), col("Progress", PROGRESS_CELL), col("Status", STATUS_CELL)));
   }



   /*******************************************************************************
    ** Failures and partial successes in the last seven days: failed and
    ** needs-review first, then completed with errors, newest first within each.
    *******************************************************************************/
   private TableData attention(Instant now) throws QException
   {
      List<QRecord> jobs = new ArrayList<>(query(JOBS_JOB, statuses(ATTENTION_STATUSES).withSubFilter(finishedSince(now.minus(7, ChronoUnit.DAYS)))
         .withOrderBy(new QFilterOrderBy(FINISHED_AT, false)).withLimit(200)));
      jobs.sort(Comparator.comparingInt((QRecord job) -> COMPLETED_WITH_ERRORS.equals(job.getValueString(STATUS)) ? 1 : 0)
         .thenComparing((QRecord job) -> job.getValueInstant(FINISHED_AT), Comparator.nullsLast(Comparator.reverseOrder())));

      List<QRecord> listedJobs = jobs.subList(0, Math.min(25, jobs.size()));
      Map<Integer, Integer> reviewCounts = new HashMap<>();
      if(!listedJobs.isEmpty() && QContext.getQInstance().getTable(JOBS_EFFECT) != null)
      {
         QQueryFilter filter = new QQueryFilter().withCriteria(JOB_ID, QCriteriaOperator.IN, listedJobs.stream().map(job -> job.getValueInteger(ID)).toList())
            .withCriteria(NEEDS_REVIEW_FIELD, QCriteriaOperator.EQUALS, true);
         for(QRecord effect : query(JOBS_EFFECT, filter))
         {
            reviewCounts.merge(effect.getValueInteger(JOB_ID), 1, Integer::sum);
         }
      }
      List<Map<String, Object>> rows = new ArrayList<>();
      for(QRecord job : listedJobs)
      {
         String error    = JobsFormat.clip(job.getValueString(ERROR_MESSAGE), 120);
         String failed   = count(job, ROWS_FAILED) + (number(job, ROWS_FAILED) == 1 ? " row failed" : " rows failed");
         String finished = "Finished " + JobsFormat.time(job.getValueInstant(FINISHED_AT), now);
         String status   = statusLabel(job.getValueString(STATUS));
         Map<String, Object> row = row("Job", job.getValueString(LABEL), "Process", processLabel(job.getValueString(PROCESS_NAME)),
            "Status", status, "Rows failed", number(job, ROWS_FAILED), "Error", error, "Finished", finished, "Path", jobPath(job));
         row.put(JOB_CELL, jobCell(job, error.isEmpty() ? null : error));
         CompositeWidgetData statusCell = column().withBlock(text(status, "semibold", COMPLETED_WITH_ERRORS.equals(job.getValueString(STATUS)) ? "WARNING" : "ERROR"));
         if(number(job, ROWS_FAILED) > 0)
         {
            statusCell.withBlock(small(failed));
         }
         int effectsNeedingReview = reviewCounts.getOrDefault(job.getValueInteger(ID), 0);
         if(effectsNeedingReview > 0)
         {
            statusCell.withBlock(small(effectsNeedingReview + " effects need review"));
         }
         row.put(STATUS_CELL, statusCell.withBlock(small(finished)));
         rows.add(row);
      }
      return (table("No jobs need attention", rows, col("Job", JOB_CELL), col("Status", STATUS_CELL)));
   }



   /*******************************************************************************
    ** Defense in depth: direct renderer calls also require jobs_admin.
    *******************************************************************************/
   private TableData workers(Instant now) throws QException
   {
      List<Map<String, Object>> rows  = new ArrayList<>();
      boolean                   admin = QContext.getQSession() != null && QContext.getQSession().hasPermission(JOBS_ADMIN);
      if(admin)
      {
         for(QRecord worker : query(JOBS_WORKER, new QQueryFilter().withOrderBy(new QFilterOrderBy(POD_ID))))
         {
            long   threads   = number(worker, THREADS);
            String capacity  = threads + (threads == 1 ? " thread" : " threads");
            String draining  = Boolean.TRUE.equals(worker.getValueBoolean(DRAINING)) ? "Yes" : "No";
            String heartbeat = JobsFormat.relative(worker.getValueInstant(HEARTBEAT_AT), now);
            String started   = "Started " + JobsFormat.time(worker.getValueInstant(STARTED_AT), now);
            Map<String, Object> row = row("Pod", worker.getValueString(POD_ID), "Threads", capacity, "Draining", draining,
               "Last heartbeat", heartbeat, "Started", started);
            row.put(POD_CELL, column().withBlock(text(worker.getValueString(POD_ID), "semibold", null)).withBlock(small(started)));
            row.put(CAPACITY_CELL, column().withBlock(text(capacity, null, null)).withBlock(small("Draining: " + draining)));
            rows.add(row);
         }
      }
      TableData table = table(admin ? "No workers have registered" : "Requires jobs_admin permission", rows,
         col("Pod", POD_CELL), col("Capacity", CAPACITY_CELL), new TableData.Column("default", "Last heartbeat", "Last heartbeat", null, "left"));
      return (table);
   }



   /*******************************************************************************
    ** Aggregate chunks into 24 UTC hour buckets and the top five processes,
    ** named by their labels. Job process names come from one query.
    *******************************************************************************/
   private ChartData throughput(Instant now) throws QException
   {
      Instant       start  = now.truncatedTo(ChronoUnit.HOURS).minus(23, ChronoUnit.HOURS);
      List<QRecord> chunks = query(JOBS_CHUNK, statuses(List.of(DONE)).withSubFilter(finishedSince(start)));
      Set<Serializable> jobIds = new HashSet<>();
      chunks.forEach(chunk -> jobIds.add(chunk.getValue(JOB_ID)));
      Map<Serializable, String> processes = new HashMap<>();
      if(!jobIds.isEmpty())
      {
         for(QRecord job : query(JOBS_JOB, new QQueryFilter().withCriteria(ID, QCriteriaOperator.IN, jobIds)))
         {
            processes.put(job.getValue(ID), processLabel(job.getValueString(PROCESS_NAME)));
         }
      }
      Map<String, Long> totals = new HashMap<>();
      for(QRecord chunk : chunks)
      {
         String process = processes.get(chunk.getValue(JOB_ID));
         if(process != null)
         {
            totals.merge(process, number(chunk, ROWS_IN), Long::sum);
         }
      }
      List<String> top = totals.keySet().stream().sorted(Comparator.<String>comparingLong(totals::get).reversed().thenComparing(Comparator.naturalOrder())).limit(5).toList();
      Map<String, long[]> series = new LinkedHashMap<>();
      top.forEach(process -> series.put(process, new long[24]));
      for(QRecord chunk : chunks)
      {
         String process = processes.get(chunk.getValue(JOB_ID));
         if(process != null)
         {
            String group  = top.contains(process) ? process : "Other";
            int    bucket = (int) Duration.between(start, chunk.getValueInstant(FINISHED_AT)).toHours();
            if(bucket >= 0 && bucket < 24)
            {
               series.computeIfAbsent(group, key -> new long[24])[bucket] += number(chunk, ROWS_IN);
            }
         }
      }
      List<String>      labels = new ArrayList<>();
      DateTimeFormatter hour   = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
      for(int i = 0; i < 24; i++)
      {
         labels.add(start.plus(i, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC).format(hour));
      }
      return (chart("Rows processed per hour, last 24 hours (UTC)", labels, series));
   }



   /*******************************************************************************
    ** Thirty UTC dates including today, split by final status.
    *******************************************************************************/
   private ChartData history(Instant now) throws QException
   {
      LocalDate           first  = now.atOffset(ZoneOffset.UTC).toLocalDate().minusDays(29);
      Map<String, long[]> series = new LinkedHashMap<>();
      TERMINAL_STATUSES.forEach(status -> series.put(statusLabel(status), new long[30]));
      for(QRecord job : query(JOBS_JOB, statuses(TERMINAL_STATUSES).withSubFilter(finishedSince(first.atStartOfDay().toInstant(ZoneOffset.UTC)))))
      {
         int bucket = (int) ChronoUnit.DAYS.between(first, job.getValueInstant(FINISHED_AT).atOffset(ZoneOffset.UTC).toLocalDate());
         if(bucket >= 0 && bucket < 30)
         {
            series.get(statusLabel(job.getValueString(STATUS)))[bucket]++;
         }
      }
      List<String>      labels = new ArrayList<>();
      DateTimeFormatter day    = DateTimeFormatter.ofPattern("MMM d", Locale.US);
      for(int i = 0; i < 30; i++)
      {
         labels.add(first.plusDays(i).format(day));
      }
      return (chart("Jobs finished per day, last 30 days (UTC)", labels, series));
   }



   /*******************************************************************************
    ** Record progress: rows, chunks, speed and optional effects, scoped to the viewer.
    *******************************************************************************/
   private MultiStatisticsData progress(QRecord job, Instant now) throws QException
   {
      if(job == null)
      {
         return (new MultiStatisticsData(null, List.of()));
      }
      String status  = job.getValueString(STATUS);
      String percent = percent(job);

      List<MultiStatisticsData.StatisticsGroupData.Statistic> rowStats = new ArrayList<>();
      rowStats.add(statistic("Succeeded", number(job, ROWS_OK), null));
      rowStats.add(statistic("Failed", number(job, ROWS_FAILED), null));
      if(job.getValue(ROWS_TOTAL) != null)
      {
         rowStats.add(statistic("Total", number(job, ROWS_TOTAL), null));
      }
      MultiStatisticsData.StatisticsGroupData rows = new MultiStatisticsData.StatisticsGroupData().withHeader("Rows")
         .withSubheader(job.getValue(ROWS_TOTAL) == null ? "Total not known yet" : percent + " done").withStatisticList(rowStats);

      List<MultiStatisticsData.StatisticsGroupData.Statistic> chunkStats = new ArrayList<>();
      chunkStats.add(statistic("Done", number(job, CHUNKS_DONE), null));
      chunkStats.add(statistic("Failed", number(job, CHUNKS_FAILED), null));
      if(job.getValue(CHUNKS_TOTAL) != null)
      {
         chunkStats.add(statistic("Total", number(job, CHUNKS_TOTAL), null));
      }
      MultiStatisticsData.StatisticsGroupData chunks = new MultiStatisticsData.StatisticsGroupData().withHeader("Chunks").withStatisticList(chunkStats);

      String speedNote;
      if(job.getValue(FINISHED_AT) != null)
      {
         speedNote = statusLabel(status) + " " + JobsFormat.time(job.getValueInstant(FINISHED_AT), now);
      }
      else if(RUNNING_STATUSES.contains(status))
      {
         speedNote = "ETA " + eta(job, now);
      }
      else
      {
         speedNote = statusLabel(status);
      }
      MultiStatisticsData.StatisticsGroupData speed = new MultiStatisticsData.StatisticsGroupData().withHeader("Speed").withSubheader(speedNote)
         .withStatisticList(List.of(statistic("Rows per minute", Math.round(rate(job, now)), null)));
      List<MultiStatisticsData.StatisticsGroupData> groups = new ArrayList<>(List.of(rows, chunks, speed));
      if(QContext.getQInstance().getTable(JOBS_EFFECT) != null)
      {
         List<QRecord> effects = query(JOBS_EFFECT, new QQueryFilter().withCriteria(JOB_ID, QCriteriaOperator.EQUALS, job.getValue(ID)));
         if(!effects.isEmpty())
         {
            long sent = 0;
            long pending = 0;
            long failed = 0;
            long review = 0;
            for(QRecord effect : effects)
            {
               if(Boolean.TRUE.equals(effect.getValueBoolean(NEEDS_REVIEW_FIELD)))
               {
                  review++;
               }
               else
               {
                  String effectStatus = effect.getValueString(STATUS);
                  sent += SENT.equals(effectStatus) ? 1 : 0;
                  failed += FAILED.equals(effectStatus) ? 1 : 0;
                  pending += PENDING.equals(effectStatus) || SENDING.equals(effectStatus) || UNKNOWN.equals(effectStatus) ? 1 : 0;
               }
            }
            groups.add(new MultiStatisticsData.StatisticsGroupData().withHeader("Effects").withStatisticList(List.of(
               statistic("Sent", sent, null), statistic("Pending", pending, null),
               statistic("Failed", failed, null), statistic("Needs review", review, null))));
         }
      }
      return (new MultiStatisticsData(null, groups));
   }



   /*******************************************************************************
    ** Link encoding follows core ChildRecordListRenderer's filtered table link.
    *******************************************************************************/
   private StatisticsData audit(QRecord job) throws QException
   {
      StatisticsData data = new StatisticsData().withCount(0).withCountContext("Job not found or not accessible");
      data.setLabel("Changes made by this job");
      if(job != null && QContext.getQInstance().getTable(AUDIT) != null)
      {
         QQueryFilter filter = new QQueryFilter().withCriteria(MESSAGE, QCriteriaOperator.CONTAINS, "job:" + job.getValue(ID) + "/");
         int          count  = new CountAction().execute(new CountInput(AUDIT).withFilter(filter)).getCount();
         data.withCount(count).withCountContext("Audit rows").withCountURL(tableUrl(AUDIT, filter));
      }
      return (data);
   }



   /*******************************************************************************
    ** A table's path with a filter, as core's filtered table links encode it.
    *******************************************************************************/
   private static String tableUrl(String table, QQueryFilter filter) throws QException
   {
      String path = QContext.getQInstance().getTablePath(table);
      return ((path == null ? "/" + table : path) + "?filter=" + URLEncoder.encode(JsonUtils.toJson(filter), StandardCharsets.UTF_8));
   }



   /*******************************************************************************
    ** The jobs table filtered to a summary statistic's jobs.
    *******************************************************************************/
   private static String jobsUrl(QQueryFilter filter) throws QException
   {
      return (tableUrl(JOBS_JOB, filter));
   }



   /*******************************************************************************
    ** A table with no inner title (the widget label is the title), at most 25 rows.
    *******************************************************************************/
   private static TableData table(String empty, List<Map<String, Object>> rows, TableData.Column... columns)
   {
      return (new TableData(null, Arrays.asList(columns), rows).withNoRowsFoundHTML(empty).withRowsPerPage(25).withHidePaginationDropdown(true));
   }



   /*******************************************************************************
    ** A flexible-width composite (block) column.
    *******************************************************************************/
   private static TableData.Column col(String header, String accessor)
   {
      return (new TableData.Column("composite", header, accessor, null, "left"));
   }



   /*******************************************************************************
    ** Preserve key order.
    *******************************************************************************/
   private static Map<String, Object> row(Object... pairs)
   {
      Map<String, Object> row = new LinkedHashMap<>();
      for(int i = 0; i < pairs.length; i += 2)
      {
         row.put((String) pairs[i], pairs[i + 1]);
      }
      return (row);
   }



   /*******************************************************************************
    ** The job cell: its label linked to the job record, the process label under
    ** it, and optionally a note (the error) on its own line.
    *******************************************************************************/
   private static CompositeWidgetData jobCell(QRecord job, String note) throws QException
   {
      String label = Objects.toString(job.getValueString(LABEL), "Job " + job.getValue(ID));
      CompositeWidgetData cell = column()
         .withBlock(text(label, "semibold", null).withLink(new BlockLink(jobPath(job))))
         .withBlock(small(processLabel(job.getValueString(PROCESS_NAME))));
      if(note != null)
      {
         cell.withBlock(text(note, null, "ERROR").withStyles(new TextStyles().withSize(SMALL).withColor("ERROR")));
      }
      return (cell);
   }



   /*******************************************************************************
    ** A vertical stack of blocks.
    *******************************************************************************/
   private static CompositeWidgetData column()
   {
      return (new CompositeWidgetData().withLayout(CompositeWidgetData.Layout.FLEX_COLUMN));
   }



   /*******************************************************************************
    ** A text block, with optional weight and standard color.
    *******************************************************************************/
   private static TextBlockData text(String value, String weight, String color)
   {
      TextStyles styles = new TextStyles();
      if(weight != null)
      {
         styles.withWeight(weight);
      }
      if(color != null)
      {
         styles.withColor(color);
      }
      return (new TextBlockData().withValues(new TextValues(value)).withStyles(styles));
   }



   /*******************************************************************************
    ** A small muted secondary line.
    *******************************************************************************/
   private static TextBlockData small(String value)
   {
      return (new TextBlockData().withValues(new TextValues(value)).withStyles(new TextStyles().withSize(SMALL).withColor("MUTED")));
   }



   /*******************************************************************************
    ** One whole-number statistic (rates are rounded; huge values are capped).
    *******************************************************************************/
   private static MultiStatisticsData.StatisticsGroupData.Statistic statistic(String label, long value, String url)
   {
      return (new MultiStatisticsData.StatisticsGroupData.Statistic(label, (int) Math.min(Integer.MAX_VALUE, value), url));
   }



   /*******************************************************************************
    ** Typed stacked chart with deterministic series colors and no inner title.
    *******************************************************************************/
   private static ChartData chart(String description, List<String> labels, Map<String, long[]> series)
   {
      String[]                     colors   = {"#2563eb", "#16a34a", "#d97706", "#dc2626", "#7c3aed", "#64748b"};
      List<ChartData.Data.Dataset> datasets = new ArrayList<>();
      for(Map.Entry<String, long[]> entry : series.entrySet())
      {
         List<Number> values = Arrays.stream(entry.getValue()).mapToObj(value -> (Number) value).toList();
         datasets.add(new ChartData.Data.Dataset().withLabel(entry.getKey()).withData(values).withBackgroundColor(colors[datasets.size() % colors.length]));
      }
      return (new JobsChartData().withDescription(description).withChartData(new ChartData.Data().withLabels(labels).withDatasets(datasets)));
   }



   /*******************************************************************************
    ** Null counters mean no recorded progress.
    *******************************************************************************/
   private static long number(QRecord record, String field)
   {
      Long value = record.getValueLong(field);
      return (value == null ? 0 : value);
   }



   /*******************************************************************************
    ** A counter with thousands separators.
    *******************************************************************************/
   private static String count(QRecord record, String field)
   {
      return (JobsFormat.count(number(record, field)));
   }



   /*******************************************************************************
    ** Inclusive lower bound, with unfinished jobs excluded.
    *******************************************************************************/
   private static boolean since(QRecord record, String field, Instant cutoff)
   {
      Instant value = record.getValueInstant(field);
      return (value != null && !value.isBefore(cutoff));
   }



   /*******************************************************************************
    ** Distinguish an unknown total from a known zero.
    *******************************************************************************/
   private static String total(QRecord job, String field)
   {
      return (job.getValue(field) == null ? "Unknown" : count(job, field));
   }



   /*******************************************************************************
    ** Percent done from rows, else from chunks; null when neither total is known.
    *******************************************************************************/
   private static BigDecimal percentValue(QRecord job)
   {
      long total = number(job, ROWS_TOTAL);
      long done  = number(job, ROWS_DONE);
      if(total <= 0)
      {
         total = number(job, CHUNKS_TOTAL);
         done = number(job, CHUNKS_DONE);
      }
      return (total <= 0 ? null : BigDecimal.valueOf(Math.min(100, Math.round(100.0 * done / total))));
   }



   /*******************************************************************************
    ** Percent done as text ("45%"), or "Unknown".
    *******************************************************************************/
   private static String percent(QRecord job)
   {
      BigDecimal value = percentValue(job);
      return (value == null ? "Unknown" : value.toPlainString() + "%");
   }



   /*******************************************************************************
    ** Lifetime processing rate in rows per minute, frozen at finishedAt.
    *******************************************************************************/
   private static double rate(QRecord job, Instant now)
   {
      Instant start   = job.getValueInstant(STARTED_AT);
      Instant finish  = job.getValueInstant(FINISHED_AT);
      long    seconds = start == null ? 0 : Duration.between(start, finish == null ? now : finish).getSeconds();
      return (seconds <= 0 ? 0 : number(job, ROWS_DONE) * 60.0 / seconds);
   }



   /*******************************************************************************
    ** Relative ETA ("in 8m") for a running job with a rate and a known total.
    *******************************************************************************/
   private static String eta(QRecord job, Instant now)
   {
      double rate = rate(job, now);
      if(!RUNNING_STATUSES.contains(job.getValueString(STATUS)) || rate <= 0 || job.getValue(ROWS_TOTAL) == null)
      {
         return ("Unknown");
      }
      long seconds = (long) Math.ceil(Math.max(0, number(job, ROWS_TOTAL) - number(job, ROWS_DONE)) * 60.0 / rate);
      return ("in " + JobsFormat.span(seconds));
   }



   /*******************************************************************************
    ** The job's record path, from the instance's table path (frontends map it).
    *******************************************************************************/
   private static String jobPath(QRecord job) throws QException
   {
      String path = QContext.getQInstance().getTablePath(JOBS_JOB);
      return ((path == null ? "/" + JOBS_JOB : path) + "/" + job.getValue(ID));
   }
}
