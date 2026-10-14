# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project overview

QBit Jobs is the typed Jobs dashboard, record panels, alert declarations and retention
process for QQQ durable jobs. Read `docs/DESIGN.md` and `README.md` first. Core owns
all six `jobs_*` tables and control actions. This qbit adds no tables and no client UI.

## Build commands

Java 21; always use the wrapper and offline mode in this workspace:

```bash
./mvnw -o compile
./mvnw -o test
./mvnw -o test -Dtest=JobsWidgetRendererTest
./mvnw -o verify
./mvnw -o -Pdemo test-compile
```

Windows: `./mvnw.cmd -o ...`. Verify enforces checkstyle on main/test sources and
JaCoCo >=70% instructions, >=90% classes. Default tests use JUnit 5, AssertJ, MOCK auth
and the memory backend. No Docker or external services. Do not commit in this sandbox.

## Architecture by package

- `com.kingsrook.qbits.jobs`: `JobsQBitProducer` registers the app and metadata directly;
  `JobsQBitConfig` supplies validated host settings; `JobsCoreContract` holds temporary
  core identifiers; `JobsSecurity` derives owner/admin session keys; `JobsIntegrations`
  supplies opt-in ESB, Quick Search and Data Analysis table lists.
- `widgets`: `JobsWidgetRenderer` dispatches the eight home/record panels;
  `JobsChartData` provides typed stacked bars; `JobsChildRecordListRenderer` ensures
  session keys before delegating to core's child-list renderer.
- `processes`: `JobsPurgeFinishedStep` performs retention with 200-row keyset batches;
  `JobsStorage` isolates reflective core storage/metadata compatibility.
- Tests: `BaseJobsTest`, `JobsTablesStandIn`, `JobsAlertsValidationStandIn`, plus producer,
  renderer, security and retention suites. Test storage emulates the future deletion API
  without filesystem access; the real RC.5 adapter's skip behavior is tested separately.

## Invariants

- Navigation contains only `jobs_job`. Processes are hidden, with plain labels.
- Query dashboard and record data in the current viewer's session through core actions.
  Never elevate a renderer. Apply missing jobs session keys before any read; owner locks
  and joined child locks are core's responsibility. Workers require exact `jobs_admin`.
- Only retention uses a system session, after authorization, and restores its caller.
- Runtime code reads/writes no filesystem files. Version comes from a packaged resource.
  Storage operations use core StorageAction. Never replace object storage with temp files.
- Delete row errors/events/chunks/uploads before jobs. Purge only old terminal jobs.
  Input-only cleanup requires COMPLETED and exactly zero failures. Clear a reference
  only after successful deletion. Storage errors fail the run for retry.
- Typed widgets only; no HTML markup. Group table information into a few flexible
  columns, without fixed widths. Child lists use core's builder and record id parameter.
- Keep config instance-local. No static config, sessions, data caches or tables.

## Naming and code style

- Package `com.kingsrook.qbits.jobs`; qbit `com.kingsrook.qbits:qbit-jobs`, namespace `jobs`.
- Widgets `jobsHome*` / `jobsJob*`; process `jobsPurgeFinished`; app name configurable.
- All core table/field/status/join/security/alert names belong in `JobsCoreContract`.
- Three-space indentation, braces on new lines, `return (x);`, flower-box Javadoc on
  every class/method, fluent `withX` setters and the Kingsrook Apache license header.
- Enforce the repository's `checkstyle/config.xml`; do not weaken gates to pass builds.

## Dependencies and stand-ins

Parent `com.kingsrook:qbit-build-parent:2.0.0`; default `qqq-snapshot` profile imports
released core BOM `4.1.0-RC.5`. Main code and default tests must not import `core.jobs`.
The opt-in `demo` profile selects locally installed `4.1.0-SNAPSHOT`, adds `src/demo/java`
and test RDBMS/Javalin/H2 dependencies. No demo server is provided yet.

Remove stand-ins when RC.6 supplies the contracts:

- `JobsCoreContract`, `JobsSecurity`, test `JobsTablesStandIn`: core #1178.
- `JobsStorage`: #1173 deleteObject method and #1178 durableJobs metadata getter.
- Test `JobsAlertsValidationStandIn`: #1178 kernel identifiers use underscores rejected
  by the released alert-name validator. Only those two errors are waived in tests;
  production validation remains strict. A core validator fix is required for startup.

## Release flow

CircleCI uses `kingsrook/qqq-orb@0.6.8`: feature branches test; `develop` publishes
SNAPSHOTs; `release/*` publishes RCs; `v*` tags publish stable releases. The GitHub
release-candidate workflow follows the repository standard. Publishing requires explicit
owner authorization. Update CHANGELOG and resolve the RC.6 compatibility requirements
before a release; do not change another repository from this workspace.
