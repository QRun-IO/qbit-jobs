# QBit Jobs

The Jobs app for QQQ durable background jobs. Its home dashboard shows live progress, throughput,
failures, workers and 30 days of history. It adds progress panels and child lists on the job record,
declares the failure, stall and effect-review alert types, and purges old jobs. The durable job kernel itself
(tables, leases, plans, control actions, uploads) is in QQQ core. Design: [docs/DESIGN.md](docs/DESIGN.md),
from QRun-IO/qqq `docs/specs/DURABLE-JOBS-AND-ASSISTED-BULK-LOAD.md`.

## Requirements
- Java 21.
- QQQ 4.1 with durable jobs enabled: `DurableJobsMetaDataProvider.defineAll(...)` and
  `DurableJobsMetaData` on the instance.
- A storage table for chunk objects and uploads, which should be object storage (S3).

## Install
```xml
<dependency>
   <groupId>com.kingsrook.qbits</groupId>
   <artifactId>qbit-jobs</artifactId>
   <version>0.1.0</version>
</dependency>
```
```java
new DurableJobsMetaDataProvider().defineAll(qInstance, "rdbms", DurableJobsMetaDataProvider.snakeCaseColumnNames());
qInstance.withSupplementalMetaData(new DurableJobsMetaData().withStorageTableName("objectStorageJobs"));
new JobsQBitProducer().withQBitConfig(new JobsQBitConfig()).produce(qInstance);
```

## Tables
The tables are core's, prefixed `jobs_`. This qbit adds no tables.

| Table | What it holds |
|---|---|
| `jobs_job` | One row per job: status, progress counters, owner, channel, inputs, outputs. **Nav entry.** |
| `jobs_chunk` | The leased units of work, with attempts, checkpoints and per-chunk counts. Child list on the job. |
| `jobs_row_error` | Which rows failed and why. Child list on the job; reportable. |
| `jobs_effect` | Side effects outside the database (outbox): email, HTTP, S3, other databases; child list on the job. |
| `jobs_event` | The job's timeline (launched, reclaimed, paused, completed and so on). Child list on the job. |
| `jobs_worker` | The pods that run jobs. |
| `jobs_upload` | Uploaded files (refs, owners, expiry). |

## Permissions
- Users see their own jobs: core's record lock on `jobs_job.userId` through the `jobs_user` key.
- `jobs_admin` sees and controls every job, and sees the Workers widget.
- Apply the keys in your auth customizer with `DurableJobsSecurity.applyToSession(session)`.

## Configuration
`JobsQBitConfig`:
- `retentionDays` (30);
- `inputRetentionDays` (7);
- `purgeCronExpression` (`0 30 3 * * ?`, daily at 03:30 UTC);
- `schedulerName` (null; set to a host scheduler name to enable the schedule);
- `homeRefreshSeconds` (5);
- `recordRefreshSeconds` (3);
- `appName` (`jobs`).

## Adopting in another app
1. Enable durable jobs in core (above) and create the tables. `docs/ddl/postgres.sql` is the Postgres
   DDL with snake_case columns.
2. Produce this qbit.
3. Wire the optional integrations from `JobsIntegrations`: ESB, Quick Search and Data Analysis.
4. If qbit-alerts is installed, the alert types appear in it automatically.

## Build
```bash
./mvnw verify    # tests, checkstyle (main and test sources), JaCoCo gate
```

The local demo server (`src/demo/java`, used for the screenshots) runs against a locally installed
QQQ build that has the durable job kernel, under a private version so it never replaces a shared
SNAPSHOT:
```bash
# in a QQQ checkout of feature/durable-jobs-chunked:
mvn install -DskipTests -Drevision=4.1.0-JOBS-SNAPSHOT
# here:
./mvnw -Pdemo -Dqqq.snapshot.version=4.1.0-JOBS-SNAPSHOT test-compile exec:java    -Dexec.classpathScope=test -Dexec.mainClass=com.kingsrook.qbits.jobs.demo.JobsDemoServer
# then open http://localhost:8000/app/jobs
```

## Compatibility and display

The default build uses released core **4.1.0-RC.5**, which has no durable jobs kernel.
Production requires the kernel and the following core fixes (expected in RC.6):

- `QRun-IO/qqq#1178`: the jobs tables, constants and security metadata (alert types
  `jobs.job-failed` and `jobs.job-stalled`).
- `QRun-IO/qqq#1191`: the optional `jobs_effect` outbox and `jobs.effect-unresolved` alert.
  Effects child lists require the table and its job join; effect statistics and retention
  checks are enabled when the table exists. The app also works without that table.
- `QRun-IO/qqq#1173`: `StorageAction.deleteObject`. Until present, object deletion logs
  once and is skipped. Job/child row retention still runs. Cleanup of inputs on retained
  clean jobs preserves their references when deletion is unavailable, allowing retry.
  Objects belonging to fully purged jobs may need the host's storage lifecycle policy.

The app produces typed data only, with no HTML. The widget label is each card's title,
so the data carries no second title.
- **Tables.** The running, attention and worker tables have two or three flexible
  columns. Their cells are composite widget blocks:
  - the job's label links to its record, with the process label under it;
  - a progress bar shows the percent and rows done;
  - the status line is a label (or the waiting reason) with the rate and ETA.
  Each row also keeps its plain values (`Job`, `Process`, `Status`, `Rows`, `Rate`,
  `ETA`, `Path` and so on) for exports and other clients.
- **Labels.** Statuses show core's labels, and processes show their metadata labels.
- **Times.** Times within a day are relative ("4m ago", "in 3m"). Older times are
  absolute UTC ("Oct 14, 08:46 UTC"). Chart buckets use UTC.
- **Statistics.** Statistic values are whole numbers, so rates are rounded to rows per
  minute. Rows/min (last hour) is the rows done by jobs that progressed in the last
  hour, divided by 60.

Retention is admin/system-only and restores the original session even on failure.
Its output contains standard process summary lines for jobs purged, chunk objects
removed during purge, and input objects removed (uploads plus retained-job inputs).
Jobs with effects outside SENT/FAILED, or with effects needing review, keep their rows
and inputs. When any are skipped, the summary includes "N jobs kept: effects still outstanding".
Needs attention shows each listed job's effect-review count; Live Progress adds an Effects
group (Sent, Pending, Failed, Needs review) when the job has effects.


## License
Apache License 2.0. See [LICENSE](LICENSE).
