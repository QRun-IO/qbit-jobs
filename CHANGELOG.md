# Changelog

All notable changes follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - Unreleased

### Added

- Jobs app with six typed home widgets for live progress, throughput, attention,
  worker health and 30-day history, scoped to each viewer's record permissions.
- Job progress, ordered chunk/error/event child lists, and filtered audit links.
- Core-compatible failure/stall alert declarations and a reaper heartbeat monitor.
- Configurable, hidden, scheduled retention with system-session isolation, paged
  deletion, object cleanup compatibility and standard process summary lines.
- Opt-in ESB, Quick Search and Data Analysis registration lists.
- Postgres DDL for the six core tables and indexes.
- Released RC.5 contract/security/storage stand-ins and memory-backed tests.
- Offline Maven wrapper build with checkstyle and JaCoCo coverage enforcement.
- Readable home widgets:
  - block cells with record links and progress bars;
  - process and status labels;
  - relative or UTC times;
  - grouped overview statistics;
  - no repeated titles.
