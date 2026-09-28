## Claude:

1. **Consistency_Across_Systems.** You write to the DB and send to a broker; one succeeds and the other doesn't. Covers the dual-write problem, the outbox pattern, sagas versus two-phase commit, and eventual consistency (read-your-own-writes). Probably the strongest architect signal of the lot.
2. **Shared_State_Across_Instances.** `synchronized` stops at the JVM, so two instances cause lost updates. Covers optimistic locking with version columns, DB row locks, distributed locks and why they need fencing tokens, and single-writer designs.
3. **Duplicates_and_Idempotency.** At-least-once delivery is the norm, and retries from 7.2 create duplicates. Covers idempotency keys, unique constraints as dedup, idempotent consumers, and the "exactly-once" myth. If 7.2 already covers idempotency, this folds into it.
4. **Change_and_Compatibility.** During a rolling deploy, old and new code run at the same time against one DB and one message format. Covers expand/contract migrations, API and message schema evolution, and serialization traps.
5. **Caching_and_Stale_Data.** Invalidation, stampedes when a hot key expires, local versus shared cache, and cache versus DB consistency.
6. **Ordering_of_Events.** Out-of-order delivery, partition keys, per-key ordering versus parallelism, and late events.
7. **Startup_Shutdown_and_Draining.** In-flight requests and messages on SIGTERM, executor shutdown, readiness versus liveness, and acking on shutdown. May touch 7.4.
8. **Data_Volume.** N+1 queries, offset versus keyset pagination, streaming large results, batch sizes, and memory.
9. **Observability.** Finding the failure at 3am: logs, metrics and traces, correlation across services, and what to alert on. Links naturally to 7.3.


## Gemini

## 7.5_State_and_Consistency.md

* **The Core Problem:** Handling data that must be mutated, shared, or cached across distributed nodes without sacrificing performance or correctness.
* **Layers Affected:** Database (CAP theorem, isolation levels, replication lag), Caching (Redis/Memcached, local heap caches), and Application state (stateless vs stateful routing).
* **Architectural Traps:**
* **The Dual-Write Problem:** Updating a database and publishing an event to a message broker in the same method. If the database commits but the broker connection drops, the system state is permanently corrupted. (The fix is the Outbox Pattern).
* **Read-Your-Own-Writes Failure:** Implementing eventual consistency for performance, but failing to realize the user will refresh their web page and see old data because the read hit a stale replica.
* **Cache Stampedes:** A highly requested, expensive-to-compute key expires in the cache. Suddenly, 5,000 threads bypass the cache simultaneously and hit the database, instantly taking it down.



## 7.6_Communication_and_Coupling.md

* **The Core Problem:** Deciding how services interact and understanding how those protocols dictate the system's resilience and team autonomy.
* **Layers Affected:** Network protocols (REST vs gRPC), Messaging (Kafka vs RabbitMQ), and Workflow patterns (Orchestration vs Choreography).
* **Architectural Traps:**
* **The Distributed Monolith:** Chaining synchronous REST calls across four microservices to fulfill one request. If one service slows down, thread pools exhaust across the entire chain, causing cascading failures.
* **Poison Pills in Queues:** A malformed message arrives in a queue. The consumer reads it, throws an exception, and the broker puts it back at the front of the queue. The consumer reads it again, failing infinitely and completely blocking the partition.
* **Non-Idempotent Event Processing:** Assuming message brokers deliver exactly once. When the network partitions and the broker delivers the message twice, the system charges the customer's credit card twice.



## 7.7_Evolution_and_Compatibility.md

* **The Core Problem:** Changing schemas, APIs, and business logic while the system is actively taking traffic, without breaking downstream consumers.
* **Layers Affected:** Databases (schema migrations), APIs (versioning strategies), and Serialization (JSON vs Protobuf/Avro).
* **Architectural Traps:**
* **Destructive Schema Migrations:** Renaming or dropping a database column. During a rolling deployment, the old version of the app is still running alongside the new version. The old app queries the dropped column and crashes. (The fix is the Expand-and-Contract pattern across multiple deployments).
* **Forward/Backward Compatibility Breaks:** Adding a mandatory field to a Kafka event without providing a default. Downstream consumers running older code crash because they don't know how to deserialize the new shape.
* **Mobile API Stagnation:** Decommissioning a "v1" API too early. Web apps can be force-refreshed, but a segment of users will refuse to update their mobile app for years, requiring long-tail API support.



## 7.8_Observability_and_Failure_Domains.md

* **The Core Problem:** Knowing exactly what the system is doing, and ensuring that a failure in an auxiliary component does not crash the critical path.
* **Layers Affected:** Instrumentation (metrics, distributed tracing, structured logs), Infrastructure (Availability Zones, blast radii), and Operational tooling.
* **Architectural Traps:**
* **Synchronous Telemetry:** Making the logging or metrics exporter block the main thread. If the observability backend experiences high latency, the application's response time spikes directly with it.
* **High-Cardinality Metric Explosions:** Tagging a metric with a user ID or request ID. The metrics database suddenly has millions of unique time-series data points and collapses under the memory pressure.
* **Alert Fatigue:** Alerting on static CPU thresholds (e.g., "CPU > 80%") instead of user-facing Service Level Objectives (SLOs) like "Latency > 200ms for 5 minutes." The team ignores the alarms until a real outage happens.

## Something was here already

You have hit the exact point where syntax ends and system design begins. This is a classic knowledge management problem: mechanics can be neatly categorized, but real-world failures cannot. A thread starvation issue inherently involves the web framework, the thread pool, and the I/O boundary all at once.

Creating a dedicated folder for these cross-cutting scenarios is a highly effective strategy. It mimics how you actually use this knowledge on the job. In production, you don't get a Jira ticket categorized under "CompletableFuture"—you just see a cascading timeout, and you have to synthesize your knowledge of threads, pools, and context to diagnose the root cause.

Here is how you might structure it alongside your existing files:

**1. The Foundation (Your current files)**
Keep the core mechanics isolated so they are easy to look up and review sequentially.

* `Threads_and_Pools/` (What is a ThreadLocal? How does a bounded queue work?)
* `Non_Blocking_IO/` (The 6.1, 6.2 ladder: What does a Future free? What is an event loop?)

**2. The Synthesis (The new folder)**
Create a new directory specifically for cross-topic diagnostics. You could name it something like `Architecture_Traps`, `System_Diagnostics`, or `Concurrency_Scenarios`. The files inside don't need to be read in a strict sequence; they act as a catalog of anti-patterns.

* `The_ThreadLocal_Leak.md` (Combines Thread Pools + Web Framework + Security Context)
* `The_Phantom_Rollback.md` (Combines `@Transactional` + Futures + DB Connections)
* `The_Common_Pool_Freeze.md` (Combines Parallel Streams + Blocking I/O + Functional pipelines)
* `Virtual_Thread_Pinning.md` (Combines modern concurrency + legacy `synchronized` blocks)

This separation gives you two distinct ways to interact with your playbook:

* **Bottom-up:** Reviewing the foundational folders to drill the exact definitions and physics of the language features.
* **Top-down:** Reviewing the Scenarios folder to practice architectural code review, spot hidden coupling, and train for incident response.

It keeps your base files tightly focused on one concept at a time, while giving these complex, multi-layered cards a proper home where they aren't constrained to a single category.

Does setting up an `Architecture_Traps` folder feel like it solves the organizational friction you were feeling?