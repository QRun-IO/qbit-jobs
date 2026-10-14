# QBit Jobs: design

The approved design is QRun-IO/qqq `docs/specs/DURABLE-JOBS-AND-ASSISTED-BULK-LOAD.md` (#1171), section 2.12
and phase 4. Owner decisions: all of that spec's recommendations are accepted (2026-10-12/13).

**Split of responsibilities.**
- **Core** (QQQ 4.1.x) owns the durable job kernel: the `jobs_*` tables
  (`DurableJobsMetaDataProvider.defineAll`), leases, the per-pod runtime, the plans, the control
  processes (record actions), uploads, realtime events, alerts and notifications. Without this qbit,
  jobs still run and the tables still have standard screens.
- **This qbit** owns the **Jobs app**: the home dashboard, the job record panels, the alert type
  declarations, retention, and the Quick Search, Data Analysis and ESB registration hooks. It adds no
  tables and no UI code: everything is typed widget data served through `/qqq/v1`, so any client (Next,
  a CLI, iOS) can render it.

## Requirements traced (owner R1-R6)
- **R3, reportable.** Jobs, chunks, row errors and events are standard tables, with full record
  screens, reports, Data Analysis and Quick Search. The home shows live progress, throughput, failures
  and history. Every job links to its audit trail.
- **R4, notifications.** The alert types `jobs.job-failed` and `jobs.job-stalled`, and the `jobs.reaper`
  heartbeat monitor, are declared here so that qbit-alerts routes them. Completion notifications come
  from core.
- **R6, storage.** Retention deletes finished jobs and their chunk objects from object storage. The
  qbit reads and writes no files.

## The app
- `jobs` app, label **Jobs**. The nav lists `jobs_job` only. Chunks, row errors and events are child
  lists on the job record.
- **Home**, built from typed widgets read in the viewer's session (record locks apply: a user sees
  their own jobs, `jobs_admin` sees all). Live widgets set `defaultValues.refreshSeconds` (polling; the
  realtime `job.progress` channel is the push path for clients that subscribe).
  1. **Overview** (`MULTI_STATISTICS`): two groups. **Now** has Running, Queued and Needs
     attention, each linking to the matching jobs. **Last 24 hours** has Completed, Rows
     processed and Rows/min (last hour). It refreshes every 5s.
  2. **Running and queued** (`TABLE`, three block columns): the job (its label links to the
     record, with the process label under it), the progress (a bar, the percent, and rows done
     of the total), and the status (a label or the waiting reason, then the rate and ETA, or
     the start time). It refreshes every 5s.
  3. **Rows per hour** (`STACKED_BAR_CHART`): rows per hour over the last 24 hours, by
     process label.
  4. **Needs attention** (`TABLE`): FAILED, NEEDS_REVIEW and COMPLETED_WITH_ERRORS jobs from the
     last 7 days. Failed and needs-review jobs come first, newest first within each group.
     Each row shows the job, the error on its own line, the rows failed and the finish time.
  5. **Workers** (`TABLE`): pods from `jobs_worker`, with threads, draining, and last heartbeat
     age (admins only).
  6. **Job history** (`STACKED_BAR_CHART`): jobs per day over the last 30 days, by final
     status label.
  Times within a day are relative; older ones are absolute UTC. Data carries no inner titles,
  because the widget label is the card title.
- **Job record**
  - A **Live Progress** panel (`MULTI_STATISTICS`) has three groups: Rows (succeeded, failed,
    total, and the percent done), Chunks (done, failed, total) and Speed (rows per minute, with
    the ETA, the finish time or the status).
    It refreshes every 3s while the job is active.
  - Child lists for **Chunks**, **Row errors** and **Events**.
  - A **Changes made by this job** link to the audit table, filtered to messages containing
    `job:<id>/` (core writes `job:<id>/chunk:<seq>` into every audit the job causes).
  - The record actions are core's: Start, Pause, Resume, Cancel, Retry Failed, Retry Failed Rows,
    Download Failed Rows, Restart, Run Again.

## Retention
`jobsPurgeFinished` is a hidden scheduled process, not in the nav. It runs daily by default.
- It deletes jobs that finished more than `retentionDays` ago (default 30), with their chunks, row
  errors, events and chunk objects.
- For COMPLETED jobs with no failed rows, it deletes the chunk input objects after
  `inputRetentionDays` (default 7).
- Each run's summary is the standard process summary.

## Integrations (opt-in, no dependency on the other modules)
`JobsIntegrations.esbTableNames()`, `quickSearchTables()` and `dataAnalysisTables()`: a host passes
these to qqq-esb, qbit-quick-search and qbit-data-analysis.

## Dependencies and stand-ins
The qbit builds against the released QQQ RC through the default `qqq-snapshot` profile. Until the
kernel ships in an RC, the core names it needs (table, field and process names, and the jobs session
keys) are mirrored in `JobsCoreContract`, tagged
`STAND-IN(core=QRun-IO/qqq#1178, removeBy=RC.6)`. When RC.6 is released, those constants switch to
core's `DurableJobsTables`, and `JobsCoreContract` and the test-only table mirror are deleted.
The demo server (`-Pdemo`, under `src/demo/java`) runs against a locally installed 4.1.0-SNAPSHOT of
the kernel branch. It is how the screenshots are taken.
