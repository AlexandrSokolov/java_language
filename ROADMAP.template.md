# ROADMAP

> **Last updated:** 2026‑01‑29  

> **Owner:** Alexandr Sokolov / Product & Platform Team

> **Status:** Living document — updated as priorities evolve

---

## 1. Vision

Deliver a reliable, scalable, and observable backend service that enables seamless order processing, 
integrates safely with external systems, and provides a foundation for future automation and analytics.

---

## 2. Themes

### **Platform Stability**
Improve reliability, observability, and operational resilience.

### **Developer Experience**
Faster onboarding, cleaner structure, improved testing capabilities.

### **Scalability**
Ensure the system handles 5× expected load by Q4.

### **Extensibility**
Introduce well-defined APIs and event contracts for downstream teams.

---

## 3. Roadmap Timeline

### **Q1 2026 — Foundation & Observability**
- [ ] Introduce OpenTelemetry tracing across API → DB → external clients
- [ ] Implement structured JSON logging
- [ ] Add metrics dashboards for API latency and worker throughput
- [ ] Publish API specification (`openapi.yaml`)
- [ ] Migrate CI to GitHub Actions (build, test, lint, security scan)
- [ ] Create onboarding docs (`LOCAL_DEV.md`, updated `README.md`)

### **Q2 2026 — Reliability & Integrations**
- [ ] Implement retry + circuit breaker policies for external payment provider
- [ ] Introduce idempotency for webhook processing
- [ ] Add outbox pattern for reliable event publishing
- [ ] Build automated load test pipelines (k6 or Gatling)
- [ ] Release Domain V2 model (normalized order & payment flow)

### **Q3 2026 — Scalability & Data**
- [ ] Add read‑replica support for reporting
- [ ] Integrate Redis caching for hot paths
- [ ] Introduce partitioning strategy for high‑volume tables
- [ ] Event schema registry for internal consumers
- [ ] Add performance regression tests to CI

### **Q4 2026 — Advanced Features & Hardening**
- [ ] Payment flow V2 with fallback routing
- [ ] Internal admin panel (metrics + operations)
- [ ] Automated disaster recovery tests
- [ ] Compliance: data retention automation + audit logging
- [ ] Prepare for multi-region deployment (active/standby)

---

## 4. Milestones

### **Milestone 1 — Observability MVP (Q1 2026)**
- End-to-end tracing
- Dashboard for API performance
- Error rate alerting
- Log correlation with request IDs

### **Milestone 2 — Reliable Processing (Q2 2026)**
- Retry logic
- Circuit breakers
- Idempotent webhook handling
- Outbox + event publishing

### **Milestone 3 — Scale to 5× Load (Q3–Q4 2026)**
- Query optimization
- Read replicas
- Redis caching
- Load testing + tuning

### **Milestone 4 — Operational Excellence (Q4 2026)**
- DR automation
- Compliance & retention policies
- Internal admin tools

---

## 5. Proposed Enhancements (Backlog)

These items are planned but not yet scheduled:

- Support for multiple payment providers
- Bulk import API
- Async email service
- GraphQL read-only API
- Authentication improvements (fine-grained scopes)
- Migration to Java 21
- Configurable rate limiting per API key

---
