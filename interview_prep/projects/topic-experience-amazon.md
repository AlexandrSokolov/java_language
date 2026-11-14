# Project Experience — Amazon: Marketing Workflow ↔ Planning ↔ Purchasing

*Как пользоваться: сначала выучите цепочку проекта. Рассказ — около 3 минут: введение, этапы, технологии и один-два челленджа. Главный челлендж — C4 (reliability), его поднимайте сами. Остальные — на уточняющие вопросы. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Three systems → The job is the backbone → Stages → Tech → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Three systems** | Платформа — distributed system. Job Manager, Marketing Planner, Coupa через BMIS. | *"One of the most complex projects I had was the integration for Amazon's marketing organisation."* |
| 2 | **The job is the backbone** | Кампания для маркетолога — это job для Job Manager. Каждый шаг workflow запускает часть нашей логики. | *"For a marketer, a campaign is a campaign. For Job Manager, it's a job."* |
| 3 | **Stages** | Campaign → hierarchy (projects, purchase requests) → purchasing in Coupa → changes. | *"First, the campaign…"* |
| 4 | **Tech** | Java EE на WildFly, Scala-ядро, JMS, таймеры, SOAP → REST, конфигурация. | *"It was a Java EE application on WildFly…"* |
| 5 | **Challenges** | C4 reliability — главный. C1–C3, C5 — на вопросы. | *"The hardest part was reliability across three systems."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| reliability · distributed systems · consistency · duplicates · failures | **C4. Reliability across three systems** |
| architecture · clean code · testability · functional programming | **C1. Functional core, imperative shell** |
| API changes · versioning · backward compatibility · migration | **C2. An API that was still changing** |
| validation · error handling · user experience · business requirements | **C3. Feedback that business users can act on** |
| testing · debugging · production issues | **C5. Testing without access to the customer's system** |

---

## 2. Введение (около 40 секунд)

### Intro · Three systems
<details><summary>Show answer</summary>

One of the most complex projects I had was the integration for Amazon's marketing organisation.

Our platform itself was a distributed system.
The modules were independent and ran in separate processes.
They talked to each other over SOAP, REST and JMS.

Three participants were involved:
- **Job Manager**, our workflow engine;
- **Marketing Planner**, where the plan and the budgets live;
- **Coupa**, Amazon's purchasing system, which we reached through BMIS, an integration service on Amazon's side.

No single system owned the whole process. My component connected them.

</details>

---

## 3. Как это работало — этапы (около 60 секунд)

*Главное — чтобы было слышно один процесс, а не набор отдельных фич.*

**Цепочка этапов:** Campaign → Hierarchy → Purchasing → Changes

### Stage 0 · The job is the backbone
<details><summary>Show answer</summary>

For a marketer, a campaign is a campaign.
For Job Manager, it's a job that moves through a workflow.
The workflow was the backbone: each step triggered one piece of our logic.

</details>

### Stage 1 · Campaign
<details><summary>Show answer</summary>

A user creates a campaign job and fills in the data.
When it reaches a certain step, we validate it.
Then we create the campaign in Marketing Planner, with its budget, periods and dimensions,
like company and cost center, and link it back to the job.

</details>

### Stage 2 · Hierarchy
<details><summary>Show answer</summary>

Then the hierarchy grows.
Projects are created under a campaign, and purchase requests under a project.
Same pattern, one level deeper each time.

</details>

### Stage 3 · Purchasing in Coupa
<details><summary>Show answer</summary>

When a purchase request is approved, we send a requisition to Coupa.
Coupa works asynchronously, so we poll its status.
When the purchase order is ready:
- we write the PO number back to the job;
- we create the orders in the planner;
- and we confirm to Coupa that we processed it.

</details>

### Stage 4 · Changes
<details><summary>Show answer</summary>

Users could also change things later:
- change a campaign's budget or dates;
- cancel a purchase order;
- change lines of a purchase order that was already in Coupa.

For purchase orders we calculated which lines were added, deleted or changed,
and sent only those changes.

</details>

---

## 4. Технологии (около 30 секунд)

**Цепочка:** Platform → Triggers → APIs → Configuration

### Tech 1 · Platform
<details><summary>Show answer</summary>

It was a Java EE application on WildFly.
The core of the newer logic was written in Scala, in a functional style.

</details>

### Tech 2 · Triggers
<details><summary>Show answer</summary>

Three kinds of triggers:
- JMS events from Job Manager, when a job changed its workflow step;
- scheduled timers, to poll Coupa;
- REST endpoints, to trigger things manually for testing.

</details>

### Tech 3 · APIs
<details><summary>Show answer</summary>

We started with SOAP clients generated from WSDL, and later moved to REST.
All REST calls went through our shared client library,
with JWT, basic and SSO authentication, and request logging.

</details>

### Tech 4 · Configuration
<details><summary>Show answer</summary>

The behaviour was driven by configuration: workflow step names, job types, error texts.
The configuration was validated at startup and could be changed without a redeploy.

</details>

---

## 5. Челленджи

*В основной рассказ — C4. Если есть время — ещё C1. Остальные — на вопросы.*

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C4. Reliability** | no transactions → workflow step as state → inbox for duplicates → outbox for outgoing calls → repeatable writes → data checks | *In a distributed process, assume every call can fail or come twice, and design every step to be safe to repeat.* |
| **C1. Functional core** | complex, changing rules → pure core → fail fast vs fail slow → shell does I/O → SOAP to REST touched only the shell | *Keep decisions pure and push I/O to the edges.* |
| **C2. Changing API** | REST not complete → ignore unknown fields → keep unknown fields on round trip → own interfaces → raw request to product team | *Isolate an evolving API and be tolerant of what you receive.* |
| **C3. Feedback** | business spec → reject / comment / email / log → texts in configuration | *Every error needs an owner and a place where that owner will see it.* |
| **C5. Testing** | no Coupa access → mock endpoints → integration tests by job ID | *If you can't debug production, make production reproducible.* |

---

### C4 · Reliability across three systems without transactions

*Главная история проекта. Поднимайте её сами.*

### C4 · Situation
<details><summary>Show answer</summary>

We had three systems and no distributed transaction between them.
Any call could fail in the middle, and any message could come twice.
So I had to design for duplicates and partial failures from the start.

</details>

### C4 · The workflow step as state
<details><summary>Show answer</summary>

The workflow step was the state of the process.
A handler only processed jobs in its own step, and after success it moved the job forward.
So if the same job came again, it was no longer in that step and was ignored.

</details>

### C4 · Inbox: duplicate messages
<details><summary>Show answer</summary>

JMS delivers at least once, so the same event could arrive twice.
I kept a table of processed messages in our own database, with a unique constraint on the job, the step and the event.
Before processing, we inserted the record. If the insert failed on the constraint, the message was a duplicate and we skipped it.

</details>

### C4 · Outbox: the dual-write problem
<details><summary>Show answer</summary>

The classic dual-write problem: we update our state and call Coupa, and one of the two can fail.
So I used an outbox.
- In one local transaction, we saved our state and the outgoing request into an outbox table.
- A scheduled worker read the pending entries and sent them to Coupa.
- An entry was marked as sent only after Coupa confirmed it.

If anything crashed in between, the entry was simply sent again.
Each request carried the job ID as a reference, so a repeated request was recognised and didn't create a second requisition.

</details>

### C4 · Polling as built-in reconciliation
<details><summary>Show answer</summary>

For Coupa we didn't wait for callbacks.
Every run we asked Coupa for the current purchase orders and matched them to jobs by requisition ID.
If a run failed, the next one picked everything up again. Nothing depended on a single message arriving.

</details>

### C4 · Writes safe to repeat
<details><summary>Show answer</summary>

Every write had to be safe to repeat.
For example, when we wrote orders to the planner, we first deleted the existing orders for that line,
and then created the new ones.
Running it twice gave the same result, not double orders.

</details>

### C4 · Checking the data, not only the delivery
<details><summary>Show answer</summary>

We also checked the content, not only that a message arrived.
- If the total from Coupa didn't match the total in the job, the job was rejected instead of silently accepted.
- If Coupa returned a purchase order that matched no job, for example because the job was deleted or cancelled,
  an email went to the responsible people.

</details>

### C4 · Result
<details><summary>Show answer</summary>

Lost or duplicated purchase orders stopped being a problem,
and when something did fail, it recovered on the next run without manual work.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

In a distributed process, assume every call can fail or come twice,
and design every step to be safe to repeat.

</details>

### C4 · Follow-up: why not a distributed transaction?
<details><summary>Show answer</summary>

The three systems belonged to different teams and companies. Coupa was behind Amazon's own service.
A two-phase commit across them wasn't possible, and it would also couple them too tightly.
Local transactions plus an outbox, idempotent steps and reconciliation give you eventual consistency,
which was exactly what this business process needed.

</details>

### C4 · Follow-up: what if the outbox worker sends, but crashes before marking it as sent?
<details><summary>Show answer</summary>

Then the entry is sent again on the next run. That's at-least-once delivery.
That's why the receiving side must be idempotent: the job ID as the reference made the repeated request harmless.

</details>

---

### C1 · Functional core, imperative shell

### C1 · Situation
<details><summary>Show answer</summary>

The business rules were complex, and the specification kept changing.
So I separated the business logic from the communication as strictly as I could.

</details>

### C1 · The core and the shell
<details><summary>Show answer</summary>

The core was pure functions: data in, decision out. No calls to other systems inside.
All dependencies, like configuration, job data and services, were passed in explicitly.
The shell around it did the I/O: SOAP at first, later REST, JMS and the timers.

</details>

### C1 · Fail fast and fail slow
<details><summary>Show answer</summary>

Inside the core I used two kinds of validation.
- **Fail fast** where the next step depends on the previous one:
  if the job has no linked campaign, there's nothing more to check.
- **Fail slow** where the checks are independent: I collect all errors and return them together.
  So the user sees the full list at once, not one error per attempt.

</details>

### C1 · Result
<details><summary>Show answer</summary>

The core was covered by hundreds of fast unit tests, without any server.
When we migrated from SOAP to REST, only the shell changed. The business logic stayed untouched.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Keep decisions pure and push I/O to the edges.
Then changes in the outside world stay at the edges too.

</details>

### C1 · Follow-up: was functional programming hard?
<details><summary>Show answer</summary>

It was new for me at that level. Not writing functions, but designing with them:
composition, where to fail fast and where to collect errors. I learned a lot in parallel.
Today I use the same ideas in plain Java: records, immutable data and a clear core.

</details>

*Если спросят про Kotlin: многие синтаксические идеи Kotlin вы уже видели в Scala, поэтому они для вас не новые.*

---

### C2 · An API that was still changing

### C2 · Situation
<details><summary>Show answer</summary>

Job Manager was migrating from SOAP to REST at that time.
The REST API wasn't complete: features we used in SOAP were sometimes missing,
and fields changed between releases.

</details>

### C2 · Action
<details><summary>Show answer</summary>

I expected that from the start.
- **Ignore unknown fields.** A new field in a response never broke us.
- **Keep unknown fields on a round trip.** When we read an entity, changed it and sent it back,
  I used Jackson's `@JsonAnySetter` and `@JsonAnyGetter`.
  Fields we didn't know were collected into a map and sent back unchanged,
  so we never deleted data that another team had just added.
- **Own interfaces in the shell.** All calls went through our own interfaces,
  so a change in their API meant one adapter to fix, not the whole component.
- **Evidence for the product team.** When a release removed something we needed,
  I reported it with the exact raw request and response from our logging.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

When you depend on an API that is still evolving, isolate it and be tolerant of what you receive.

</details>

### C2 · Follow-up: why is ignoring unknown fields not enough?
<details><summary>Show answer</summary>

Because of the round trip. If you read an entity into a class that doesn't know a new field,
and then send the whole entity back, the new field is lost, and you overwrite someone else's data.
With `@JsonAnySetter` and `@JsonAnyGetter` the unknown fields survive the round trip.

</details>

---

### C3 · Feedback that business users can act on

### C3 · Situation
<details><summary>Show answer</summary>

I implemented a detailed business specification from Amazon.
A big part of it was: what happens when something is wrong.

</details>

### C3 · Four levels of feedback
<details><summary>Show answer</summary>

I designed four levels of feedback:
1. **Reject.** If the job could go back, we rejected it to the previous step with a clear comment.
   The user fixed the data and sent it again.
2. **Comment.** If it couldn't go back, for example after it was already in Coupa, we left a comment in the job.
3. **Email.** If the problem had no job to attach to, for example a purchase order from Coupa that matched no job,
   we sent an email to the responsible people.
4. **Log.** Everything was logged as a short description, not a stack trace.

</details>

### C3 · Texts in configuration
<details><summary>Show answer</summary>

The error texts lived in the configuration,
so the business could change the wording without a release.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

Every error needs an owner and a place where that owner will see it.

</details>

---

### C5 · Testing without access to the customer's system

### C5 · Mock endpoints
<details><summary>Show answer</summary>

We had no access to Coupa in our environments.
So I built mock endpoints inside the application:
one that returned realistic data, and one that always failed, to test our error handling.

</details>

### C5 · Integration tests instead of a debugger
<details><summary>Show answer</summary>

For production issues I didn't use a debugger. On a live system it was far too slow,
with about thirty seconds between steps.
Instead, my integration tests took the ID of the failing job
and ran the real logic against the live system from my machine.
I could reproduce the problem in minutes.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

If you can't debug production, make production reproducible.

</details>

---

## 6. Сами не поднимайте

- **Версии Java, Scala и библиотек.** Только «Java EE on WildFly», «Scala in a functional style».
- **Детали XCM и WFS purchase requests.** Говорите просто «purchase requests».
- **Что спецификацию писали не вы.** Говорите «I implemented a detailed business specification from Amazon».
