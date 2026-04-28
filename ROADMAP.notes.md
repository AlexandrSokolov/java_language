
## Notes

- This roadmap is **not** a commitment — priorities may shift based on business needs.
- Each roadmap item should have:
    - A linked Jira ticket
    - Acceptance criteria
    - Owner(s)
- Keep this document short and high-level. Detailed implementation lives in Jira, ADRs, and `ARCHITECTURE.md`.

## Roadmap Timeline vs Milestones

### What Are Milestones?
Milestones represent major achievements in the project.  
They are outcome‑focused, high‑level, and not tied to specific dates.

Characteristics:
- Define big goals and success criteria
- Describe *what* must be accomplished
- Rarely change (unless strategy shifts)
- Represent strategic outcomes rather than tasks

Examples:
- "Observability MVP delivered"
- "Reliable Processing (Payment Flow v2)"
- "System scaled to 5× throughput"

---

### What Is the Roadmap Timeline?
The timeline is the time‑ordered plan for delivering work.  
It lists concrete tasks or initiatives grouped by quarters or releases.

Characteristics:
- Organized by date (e.g., Q1, Q2, Q3)
- Contains tactical items contributing to milestones
- Focuses on execution details
- Likely to change as priorities shift

Examples:
- Q1 → Add tracing, metrics, dashboards
- Q2 → Add idempotent webhook handling
- Q3 → Add read replicas, introduce Redis caching
- Q4 → Add disaster recovery automation

---

### How They Work Together
Milestones describe **WHY and WHAT**.  
The timeline describes **WHEN and HOW**.

A milestone may contain several timeline items.

Examples:

Milestone: Reliable Processing
├─ Retry logic
├─ Circuit breaker for payment provider
├─ Idempotent webhook handling
└─ Outbox pattern for event publishing

---

### Summary Comparison Table

| Concept          | Purpose                            | Time‑based | Granularity    | Focus     |
|------------------|------------------------------------|------------|----------------|-----------|
| Milestones       | Major accomplishments and outcomes | No         | High-level     | Results   |
| Roadmap Timeline | Plan for delivering work           | Yes        | Detailed items | Execution |

---

### Rule of Thumb
- **Milestones = the WHY (strategic outcomes)**
- **Timeline = the WHEN + HOW (tactical work)**

Together, they form a complete planning model:  
Milestones give direction; the timeline delivers it.


