# Changelog

All notable changes follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - Unreleased

### Security

- The Jobs app and its five non-admin home widgets now require `<appName>.hasAccess`
  (default `jobs.hasAccess`) and are hidden otherwise. Behaviour change: hosts must grant
  this permission to the users who should see the Jobs app. Workers still needs `jobs_admin`.

### Changed

- Built on QQQ 4.1.0-RC.7; stand-ins removed.

### Added

- RC.8 optional job progress fields: phase labels, explicit percent with legacy fallback,
  message, binary byte counts, ETA and dropped-log notice in typed widgets.
- Full-width owner-scoped Log child list after Live Progress, ascending by log ID.
- System-session retention of log lines, including 64-bit cursors and 200-row batches.
- Host `parentAppName` configuration for Admin > Operations > Jobs placement.
- Progress/log, owner isolation, real core log permission and navigation tests.

### Changed

- Default QQQ BOM from 4.1.0-RC.5 to 4.1.0-RC.8; existing `qqq.snapshot.version`
  override verifies against locally installed 4.1.0-SNAPSHOT without the demo profile.
- Storage adapter test now verifies failure/reference preservation with the current API.

### Earlier additions

- Optional effects outbox child list, viewer-scoped attention review counts and typed
  Live Progress effect statistics; Data Analysis includes `jobs_effect`.
- `jobs.effect-unresolved` warning alert for effects requiring outcome review.
- Retention deletes settled effects and preserves jobs and inputs with outstanding
  effects, reporting the number kept in the process summary.
- Outbox Postgres table and indexes, and coverage for effect counts, owner isolation
  and retention.

- Jobs app with six typed home widgets for live progress, throughput, attention,
  worker health and 30-day history, scoped to each viewer's record permissions.
- Job progress, ordered chunk/error/event child lists, and filtered audit links.
- Core-compatible failure/stall alert declarations and a reaper heartbeat monitor.
- Configurable, hidden, scheduled retention with system-session isolation, paged
  deletion, object cleanup and standard process summary lines.
- Opt-in ESB, Quick Search and Data Analysis registration lists.
- Postgres DDL for the core tables and indexes.
- Memory-backed tests using core durable jobs metadata and storage actions.
- Offline Maven wrapper build with checkstyle and JaCoCo coverage enforcement.
- Readable home widgets:
  - block cells with record links and progress bars;
  - process and status labels;
  - relative or UTC times;
  - grouped overview statistics;
  - no repeated titles.
