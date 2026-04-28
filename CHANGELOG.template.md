# Changelog

## [Unreleased]

### Added
- (No JIRA) Feature flag bootstrap for experimental HTTP/3 support.

### Changed
- PROJ-512 Switch default JSON library to Jackson 2.16.
- (No JIRA) Raise default connection pool from 50 → 100.

### Fixed
- PROJ-689 Resolve intermittent NPE during shutdown sequence.

---

## [2.3.0] - 2026-01-28

### Added
- PROJ-701 Add `/health/ready` endpoint with dependency checks.
- (No JIRA) Introduce `--dry-run` flag to CLI for safe previews.

### Changed
- PROJ-677 Migrate token storage to AES-256-GCM with key rotation policy.

### Fixed
- PROJ-715 Fix race condition in `CacheRefresher`.
- (No JIRA) Correct log level for startup banner from `WARN` to `INFO`.

### Security
- PROJ-722 Bump `netty` to 4.1.112.Final to address CVE-2025-12345.

---

## [2.2.1] - 2025-12-12

### Fixed
- PROJ-655 Prevent duplicate job scheduling on leader re-election.
- (No JIRA) Resolve flaky test in `RetryPolicyTest` by eliminating real-time sleeps.

---

## [2.2.0] - 2025-11-03

### Added
- PROJ-630 Add OpenTelemetry traces for outbound HTTP calls.
- (No JIRA) Add Docker HEALTHCHECK to base image.

### Changed
- PROJ-639 Refactor pagination API to return stable cursors.

### Deprecated
- PROJ-641 Deprecate `/v1/users/findByName`; use `/v2/users?name=` instead.

---

## [2.1.0] - 2025-07-21

### Added
- PROJ-598 Introduce RBAC roles `viewer`, `editor`, `admin`.

### Fixed
- (No JIRA) Avoid `IllegalStateException` when `shutdownNow()` called twice.

### Security
- PROJ-602 Enforce HTTPS-only cookies and `SameSite=Strict`.

---

## [2.0.0] - 2025-03-14

### Changed
- PROJ-570 BREAKING: Replace legacy XML config with YAML.
- (No JIRA) BREAKING: Drop Java 11; require **Java 17**.

### Removed
- PROJ-571 Remove deprecated `/v1/legacy-auth` endpoint.

### Migration Notes
- Update `application.yaml` to new schema (see `/docs/migration-2.0.md`).
- Ensure runtime is **Java 17+**.

---

## [1.9.3] - 2024-12-02

### Fixed
- PROJ-548 Handle null `tenantId` for service tokens.
- (No JIRA) Fix typo in `README.md` badges.

---

## [1.9.0] - 2024-09-05

### Added
- PROJ-512 Add pluggable authentication providers.

### Changed
- (No JIRA) Tune GC defaults for containerized environments.

---

## [1.0.0] - 2024-01-10

### Added
- PROJ-100 Initial public release with REST API, CLI, and SDK.

---

[Unreleased]: https://github.com/your-org/your-repo/compare/v2.3.0...HEAD
[com/your-org/your-repo/compare/v2.2.1...v2.3.0
https://github.com/your-org/your-repo/compare/v2.2.0...v2.2.1
[2.2.0]: https://github.com/your-org/your-repo/compare/v2.1.0...v2.2.0
[thub.com/your-org/your-repo/compare/v2.0.0...v2.1.0
https://github.com/your-org/your-repo/compare/v1.9.3...v2.0.0
[1.9.3]: https://github.com/your-org/your-repo/compare/v1.9.0...v1.9.3
[thub.com/your-org/your-repo/compare/v1.0.0...v1.9.0
[1.0.0]: https://github.com/your-org/your-repo/releases/tag/v1.0.0