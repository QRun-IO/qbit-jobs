# Changelog

All notable changes follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - Unreleased

### Changed

- Built on QQQ 4.1.0-RC.7; stand-ins removed.

### Added

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
