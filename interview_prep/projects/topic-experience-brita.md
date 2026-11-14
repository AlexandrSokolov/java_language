# Project Experience — BRITA: Print Assets from Job Manager to Media Pool

*Как пользоваться: сначала выучите цепочку проекта. Рассказ — около 3 минут: введение, этапы, технологии и один-два челленджа. Главные челленджи — C1 (configuration) и C5 (reliability), их поднимайте сами. Остальные — на уточняющие вопросы. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Print workflow → Manual work → Stages → Tech → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Print workflow** | BRITA делает печатные материалы в Job Manager, финальные файлы прикреплены к job. | *"Another project was for BRITA, the water filter company."* |
| 2 | **Manual work** | Media Pool ничего не знает о job: метаданные копировали руками, письма слали руками. | *"But the digital asset management system didn't know anything about the job."* |
| 3 | **Stages** | Configuration → Event → Validation → Update → Notifications. | *"First, configuration…"* |
| 4 | **Tech** | Java EE на WildFly, MySQL, JPA, Liquibase, React UI, JMS, REST. | *"A Java EE backend on WildFly…"* |
| 5 | **Challenges** | C1 configuration и C5 reliability — главные. | *"The interesting part was making it robust."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| configuration · self-service · admin UI · changing requirements | **C1. Configuration that doesn't break silently** |
| idempotency · partial failures · batch processing | **C2. Idempotent, isolated updates** |
| observability · support · troubleshooting · logging | **C3. Explainable decisions** |
| performance · load · caching · event filtering | **C4. Too many events** |
| reliability · duplicates · retries · external system down | **C5. Reliability: inbox, retries and back-off** |
| testing · debugging production | **C6. Testing against real jobs** |

---

## 2. Введение (около 40 секунд)

### Intro · The problem
<details><summary>Show answer</summary>

Another project was for BRITA, the water filter company.

Their marketing team produced print materials, like brochures and packaging, in Job Manager.
The final print files were attached to the job.
But the digital asset management system, our Media Pool, didn't know anything about the job.
So people copied the metadata by hand, and sent emails to approvers and printers manually.

</details>

### Intro · My component
<details><summary>Show answer</summary>

My component automated that.
- It listened to Job Manager.
- It synchronised the job data into the asset metadata in Media Pool.
- And it informed the right people at the right workflow step.

</details>

---

## 3. Как это работало — этапы (около 60 секунд)

**Цепочка этапов:** Configuration → Event → Validation → Update → Notifications

### Stage 1 · Configuration
<details><summary>Show answer</summary>

An administrator opens our configuration UI and:
- chooses a job type and an asset selector;
- maps job variables to Media Pool fields: title, folder, categories, keywords, tags and custom attributes;
- chooses at which workflow step the import happens, and when the approver and printer emails go out.

A configuration can be saved as a draft and published later.

</details>

### Stage 2 · Event
<details><summary>Show answer</summary>

When a job changes its workflow step, or when one of the mapped variables changes,
Job Manager sends a JMS event.
We find the published configurations for that job type,
and check if the job has reached the configured step.

</details>

### Stage 3 · Validation
<details><summary>Show answer</summary>

We check that all required variables are filled.
If not, the user gets a comment in the job with the exact fields,
and the tab of the form where to find them.

</details>

### Stage 4 · Update
<details><summary>Show answer</summary>

For each selected asset, we send a partial update to Media Pool, with only the mapped fields.

</details>

### Stage 5 · Notifications
<details><summary>Show answer</summary>

At the configured steps we send emails from templates:
- to the approver;
- to the printer;
- and to the proof printer.

If the job says the asset is no longer needed, we move it to the recycle bin.

</details>

---

## 4. Технологии (около 30 секунд)

**Цепочка:** Backend → UI → Integration

### Tech 1 · Backend
<details><summary>Show answer</summary>

A Java EE backend on WildFly, with a MySQL database through JPA and Hibernate,
and Liquibase for schema changes.

</details>

### Tech 2 · UI
<details><summary>Show answer</summary>

A separate configuration UI in React with Material UI, talking to our own REST API.

</details>

### Tech 3 · Integration
<details><summary>Show answer</summary>

JMS for events from Job Manager, and REST clients for Job Manager and Media Pool.
We supported both classic workflows and the newer BPMN processes.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Configuration** | config refers to variables and steps → job types change → validation page in UI → step names stored with IDs → migration at startup | *If users configure the system, the system must tell them when the configuration is no longer valid.* |
| **C2. Idempotent updates** | events can repeat → partial update with absolute values → one asset fails, others continue → error per asset | *Make every update safe to repeat, and keep failures small.* |
| **C3. Explainable decisions** | "why did nothing happen?" → every filter logs why → support checklist | *A system should explain not only its errors, but also why it did nothing.* |
| **C4. Too many events** | event for every change → process only mapped variables → cache reference data | *Filter early, and don't ask for the same data twice.* |
| **C5. Reliability** | duplicates → inbox table → transient errors → retries with back-off → give up visibly | *Retry what is temporary, report what is permanent, and never process the same event twice.* |
| **C6. Testing** | services without the server → integration tests by real job ID | *If you can't debug production, make production reproducible.* |

---

### C1 · Configuration that doesn't break silently

### C1 · Situation
<details><summary>Show answer</summary>

The whole behaviour was driven by configuration, created by administrators.
The configuration referred to job variables and workflow steps.
But job types kept changing: someone renamed a variable or changed the workflow,
and the configuration silently stopped working.

</details>

### C1 · Action: validation in the UI
<details><summary>Show answer</summary>

So I built validation into the product.
A separate page in the UI checked every configuration against the current job type:
- variables that no longer exist;
- required Media Pool fields that are not mapped;
- email steps without a template or a recipient.

The administrator saw the problem before a user did.

</details>

### C1 · Action: step names and migration
<details><summary>Show answer</summary>

Workflow step IDs and numbers could change, so I stored the step names together with the IDs.
A migration at startup filled in the names for old configurations.

</details>

### C1 · Action: draft and publish
<details><summary>Show answer</summary>

And a configuration could be saved as a draft and published later.
So an administrator could prepare a change without affecting running jobs.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

If users configure the system, the system must tell them when the configuration is no longer valid.

</details>

---

### C2 · Idempotent, isolated updates

### C2 · Idempotent update
<details><summary>Show answer</summary>

JMS can deliver the same event twice, and the same job can trigger several events in a row.
So I made the update idempotent.
We didn't send "add" or "increment" operations.
We sent a partial update that set the mapped fields to the values from the job.
Running it twice gave the same result.

</details>

### C2 · Isolated failures
<details><summary>Show answer</summary>

A job could have many assets. If one asset failed, the others were still updated.
The failed one got its own email, and a comment in the job with the reason from Media Pool's response.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Make every update safe to repeat, and keep failures small.

</details>

### C2 · Follow-up: why a partial update and not a full one?
<details><summary>Show answer</summary>

A full update means you read the whole asset and send it back.
Then you can overwrite fields that someone else changed in the meantime, or fields we don't manage at all.
With a partial update we touched only the fields from our mapping.

</details>

---

### C3 · Explainable decisions

### C3 · Situation
<details><summary>Show answer</summary>

The most common support question was not "why did it fail", but "why did nothing happen".

</details>

### C3 · Action
<details><summary>Show answer</summary>

So I wrote a small stream wrapper: every filter step logged which configurations were dropped, and why.
For example: the job is on step 7, but the import is configured for step 8.
With that, support could answer most questions from the log alone.

I also wrote a checklist for the support team: seven questions to answer before they opened a ticket.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

A system should explain not only its errors, but also why it did nothing.

</details>

---

### C4 · Too many events

### C4 · Situation
<details><summary>Show answer</summary>

Job Manager sent an event for every change of any variable. Most of them were irrelevant to us.

</details>

### C4 · Action
<details><summary>Show answer</summary>

- We processed a variable change only if the changed variable was part of a published mapping.
- Reference data from Media Pool, like folders, categories and custom attributes, was cached for a few minutes.

So a burst of events didn't turn into a burst of identical requests.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

Filter early, and don't ask for the same data twice.

</details>

---

### C5 · Reliability: inbox, retries and back-off

*Вторая главная история проекта. Поднимайте её сами, особенно если вакансия про distributed systems.*

### C5 · Situation
<details><summary>Show answer</summary>

Two things could go wrong.
First, JMS delivers at least once, so the same event could arrive twice.
Second, Media Pool was a separate system. Sometimes it was slow, restarting during a release, or overloaded.

</details>

### C5 · Inbox: no event processed twice
<details><summary>Show answer</summary>

I added an inbox table in our MySQL database.
For every event we stored a key: the job instance, the event type and the workflow step or the changed variables,
with a unique constraint.
Before processing, we inserted the key. If the insert failed on the constraint,
the event was a duplicate, and we skipped it.

</details>

### C5 · Retries with back-off
<details><summary>Show answer</summary>

For calls to Media Pool I separated temporary errors from permanent ones.
- **Temporary:** timeouts, connection errors and 5xx responses. We retried them with exponential back-off and some random jitter,
  so many failed calls didn't all come back at the same moment.
- **Permanent:** 4xx responses, like a category that doesn't exist. No retry. The user got a clear comment in the job.

Retries were safe because the update itself was idempotent.

</details>

### C5 · Giving up visibly
<details><summary>Show answer</summary>

After the maximum number of attempts we stopped,
marked the event as failed in the inbox, and sent an email with the job and the asset.
So nothing failed silently, and a failed event could be processed again after the cause was fixed.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

Retry what is temporary, report what is permanent, and never process the same event twice.

</details>

### C5 · Follow-up: why jitter?
<details><summary>Show answer</summary>

Without jitter, all clients that failed at the same moment retry at the same moment again.
That's a thundering herd: the system that is just recovering gets the whole load at once.
Random jitter spreads the retries over time.

</details>

### C5 · Follow-up: if the update is idempotent, why do you need an inbox?
<details><summary>Show answer</summary>

Idempotency protects the data. The inbox protects everything around it:
the emails to approvers and printers, and the load on Media Pool.
Nobody wants the same approval email twice.

</details>

---

### C6 · Testing against real jobs

### C6 · Integration tests by job ID
<details><summary>Show answer</summary>

The services could be created without the application server.
My integration tests took the ID of a real job and ran the full logic against a test system,
for both classic jobs and BPMN processes.
So I could reproduce a production problem on my machine in minutes, without a debugger.

</details>

### C6 · Lesson
<details><summary>Show answer</summary>

If you can't debug production, make production reproducible.

</details>

---

## 6. Сами не поднимайте

- **Версии Java, WildFly, React.** Только «Java EE on WildFly», «React with Material UI».
- **Детали email-шаблонов и ролей.** Только «emails from templates».
