# Project Experience — IKEA: Campaign Planning Platform for Stores Worldwide

*Как пользоваться: сначала выучите цепочку проекта. Рассказ — около 3 минут: введение, компоненты, технологии и один-два челленджа. Главные челленджи — C1 (PIM import at scale) и C3 (Excel into a job, with retry). Остальные — на уточняющие вопросы. Ответы — на английском, пояснения — по-русски.*

*Суть: IKEA (Ingka Group) планирует кампании для своих магазинов по всему миру на нашей платформе (внутреннее имя у IKEA — CAMP). Вокруг платформы — набор компонентов: магазины, артикулы из Google BigQuery, Excel-сетки в jobs, данные о продажах на таймлайне, отчёты в Google Cloud, GDPR-анонимизация.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Global retail → Platform + Google Cloud → Components → Shared foundation → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Global retail** | IKEA планирует кампании для сотен магазинов в десятках стран. | *"IKEA was one of our biggest customers…"* |
| 2 | **Platform + Google Cloud** | Наша платформа в центре, данные IKEA — в Google Cloud: BigQuery на вход, Cloud Storage на выход. | *"Their data lived in Google Cloud…"* |
| 3 | **Components** | Store setup, PIM import, Excel grids, timeline insights, report transfer, GDPR anonymizer. | *"Over the years we built about eight components…"* |
| 4 | **Shared foundation** | Общий шаблон проекта: модули auth, REST client, cron, config; quality gates. | *"All of them were built on the same foundation…"* |
| 5 | **Challenges** | C1 PIM import, C3 Excel into job — главные. | *"The hardest one was the product import."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| large data · performance · concurrency · back-pressure · cloud | **C1. Importing the product catalogue from BigQuery** |
| cloud integration · security · credentials | **C2. Two-way integration with Google Cloud** |
| validation · user feedback · retry · workflow | **C3. Excel into a job, with a way back** |
| synchronisation · diff · efficiency · business rules | **C4. Sales insights on the timeline** |
| idempotency · data integrity · audit | **C5. Store setup and deletion protection** |
| GDPR · privacy · batch jobs | **C6. GDPR: anonymising inactive users** |
| code quality · standards · reuse · architecture | **C7. One foundation for all components** |

---

## 2. Введение (около 40 секунд)

### Intro · The business
<details><summary>Show answer</summary>

IKEA was one of our biggest customers.
They planned their marketing campaigns for hundreds of stores in many countries on our platform:
the campaign calendar, the budgets, the offers with their articles and prices.

</details>

### Intro · Platform and Google Cloud
<details><summary>Show answer</summary>

Our platform was in the middle, but IKEA's own data lived in Google Cloud.
The product catalogue and the sales data came from BigQuery,
and our reports had to go back into their Cloud Storage.
So most of my work for IKEA was connecting the two worlds.

</details>

### Intro · The components
<details><summary>Show answer</summary>

Over the years we built about eight components for IKEA. The main ones:
- **Store setup**: creates new stores in the planning structure from a file;
- **Product import**: loads IKEA's article catalogue from BigQuery into our product information system;
- **Excel grid import**: lets users upload an Excel with articles and prices into a job;
- **Timeline insights**: shows sales performance from BigQuery directly on the campaign timeline;
- **Report transfer**: moves our CSV reports into IKEA's Google Cloud Storage;
- **User anonymizer**: anonymises inactive users for GDPR.

</details>

---

## 3. Как это работало — компоненты как один процесс (около 60 секунд)

**Цепочка:** Stores → Products → Offers → Insights → Reports → Privacy

### Flow 1 · Stores
<details><summary>Show answer</summary>

When IKEA opens a store, an administrator uploads a file.
We create the store in the right country in the planning tree,
with its standard structure below it, its dimensions and its user groups.

</details>

### Flow 2 · Products
<details><summary>Show answer</summary>

Every night we load the changed articles from BigQuery into the product information system,
so the planners always work with the current catalogue.

</details>

### Flow 3 · Offers
<details><summary>Show answer</summary>

For an offer, a user uploads an Excel with articles and prices into a job.
We validate it, link every article to its product, and fill the job's grid.

</details>

### Flow 4 · Insights
<details><summary>Show answer</summary>

On the campaign timeline, a hover shows the right information for the phase of the campaign:
before it starts, the sales action status; just before the start, the goals;
while it runs, the performance; after it ends, the follow-up summary.
The data comes from BigQuery.

</details>

### Flow 5 · Reports and privacy
<details><summary>Show answer</summary>

Our CSV reports are transferred to IKEA's Cloud Storage every two hours.
And users who haven't logged in for two months are anonymised automatically.

</details>

---

## 4. Технологии (около 30 секунд)

**Цепочка:** Backend → Google Cloud → UI → Quality

### Tech 1 · Backend
<details><summary>Show answer</summary>

Java EE applications on WildFly, with JAX-RS and RESTEasy, JPA and MySQL where we needed our own state.
We talked to the platform over REST, and over SOAP for some grid operations.

</details>

### Tech 2 · Google Cloud
<details><summary>Show answer</summary>

The Google Cloud client libraries for BigQuery and Cloud Storage, with service account credentials.
For the platform's newer APIs, OAuth2 client credentials and JWT.

</details>

### Tech 3 · UI
<details><summary>Show answer</summary>

React for the upload screens and the timeline extension inside the planner.

</details>

### Tech 4 · Quality
<details><summary>Show answer</summary>

FindBugs, PMD and JaCoCo ran on every build, with a 90 percent line coverage gate.
REST APIs were documented with Swagger.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. PIM import** | catalogue in BigQuery → full and delta → stream rows → bounded parallelism → upsert by article number → watermark → retries → cancellable → statistics | *Stream, limit the parallelism, and make every write an upsert.* |
| **C2. Google Cloud** | BigQuery in, Cloud Storage out → service accounts → credentials outside the package | *Treat credentials as configuration, never as code.* |
| **C3. Excel into a job** | validate everything first → reject with all errors → all-or-nothing write → failure step in the workflow → retry endpoint | *Either the whole file goes in, or nothing does, and the user always knows why.* |
| **C4. Timeline insights** | rules by campaign phase → nightly sync from BigQuery → update only what changed → clean up the rest | *Compare before you write.* |
| **C5. Store setup** | find or create → safe to run twice → deletion lock → audit log watcher | *Protect what is expensive to rebuild.* |
| **C6. GDPR** | inactive users → anonymise → exclusions → one failure doesn't stop the run → audit file | *Privacy jobs must be safe, selective and traceable.* |
| **C7. Foundation** | same modules for every component → quality gates → documented APIs | *Build the second component faster than the first.* |

---

### C1 · Importing the product catalogue from BigQuery

*Главная история проекта.*

### C1 · Situation
<details><summary>Show answer</summary>

IKEA's article catalogue lived in BigQuery: tens of thousands of articles, with names, types, states and dates.
We had to keep our product information system in sync with it.
Our side was a REST API that created and updated one product at a time.

</details>

### C1 · Full and delta loads
<details><summary>Show answer</summary>

There were two modes:
- an **initial load** of all active, saleable articles;
- a **daily delta**: only the articles that were inserted or updated since the last run, based on IKEA's ETL timestamps.

</details>

### C1 · Watermark instead of a fixed window
<details><summary>Show answer</summary>

The delta didn't use a fixed "last 24 hours".
We stored a watermark: the timestamp of the last successful run.
The next run asked for everything changed since then.
So if a run was skipped or failed, nothing fell into a gap.

</details>

### C1 · Streaming and bounded parallelism
<details><summary>Show answer</summary>

We didn't load the whole result into memory. We streamed the rows from BigQuery.
Every row became a task, and at most five tasks ran in parallel, controlled by semaphores.
If all five were busy, reading the next row waited.
That was our back-pressure: BigQuery could deliver rows much faster than the platform could accept them.

</details>

### C1 · Upsert by article number
<details><summary>Show answer</summary>

Every write was an upsert. We searched the product by article number:
not found, we created it; found once, we updated it; found more than once, it was an error for that article.
So the import was safe to run again at any time.

</details>

### C1 · Retries and errors
<details><summary>Show answer</summary>

Temporary errors from the platform, like timeouts, 5xx or 429, were retried with exponential back-off.
Data errors, like a missing required column, were not retried.
Every article was handled on its own, so one bad article never stopped the import.

</details>

### C1 · Cancellable and visible
<details><summary>Show answer</summary>

A full load took a long time, so an administrator could cancel it from the UI,
and the running tasks finished cleanly.
At the end we sent an email with the statistics: processed, created or updated, and every error with the article number.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Stream, limit the parallelism, and make every write an upsert.

</details>

### C1 · Follow-up: why only five in parallel?
<details><summary>Show answer</summary>

The limit was the platform, not BigQuery.
More parallel calls didn't make it faster, they just made the platform slower for the planners working at the same time.
Five was the point where throughput was good, and the users didn't notice the import.

</details>

### C1 · Follow-up: what about virtual threads today?
<details><summary>Show answer</summary>

Virtual threads would make the waiting cheaper, but they don't remove the need for a limit.
The bottleneck is the downstream system, so you still need a semaphore or a rate limit.

</details>

---

### C2 · Two-way integration with Google Cloud

### C2 · Action
<details><summary>Show answer</summary>

- **In:** BigQuery for the catalogue and for the timeline data.
- **Out:** every two hours, the newest base and delta CSV reports from our SFTP were uploaded into IKEA's Cloud Storage bucket.
- **Credentials:** each component used a Google service account. The key file lived in the server's configuration folder,
  outside the deployed package, so it was never in the code or in the artifact.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Treat credentials as configuration, never as code.

</details>

---

### C3 · Excel into a job, with a way back

*Вторая главная история: здесь ваш паттерн со специальным шагом workflow и REST endpoint для ретрая.*

### C3 · Situation
<details><summary>Show answer</summary>

For an offer, users prepared the articles and prices in Excel and uploaded the file into a job.
The data went into the job's grid: one row per article, linked to the product.
A half-imported grid would be worse than no grid, because prices would be wrong.

</details>

### C3 · Validate everything first
<details><summary>Show answer</summary>

We validated the whole file before writing anything:
required columns, number formats, and that every article exists in the product system.
We collected all errors, not only the first one.

</details>

### C3 · Reject with all errors
<details><summary>Show answer</summary>

If anything was wrong, the job was rejected to the previous step,
with a comment that listed every problem: the row, the column and what was wrong.
The user fixed the file and uploaded it again.

</details>

### C3 · All or nothing
<details><summary>Show answer</summary>

Only when the whole file was valid did we write the grid, and link each row to its product.

</details>

### C3 · A step for failures and a retry endpoint
<details><summary>Show answer</summary>

But a technical failure is different from bad data.
If the platform was down in the middle, or our component wasn't deployed,
the job moved to a special workflow step that meant "import failed, retry".
That step was our indicator: support could see all such jobs in one place.
And I added a REST endpoint to retry the import for a job,
so after the problem was fixed, nobody had to upload the file again.

</details>

### C3 · Export
<details><summary>Show answer</summary>

And it worked the other way too: users could download the grid of a job as Excel,
change it and upload it again.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

Either the whole file goes in, or nothing does, and the user always knows why.

</details>

---

### C4 · Sales insights on the timeline

### C4 · Situation
<details><summary>Show answer</summary>

Planners wanted to see sales information directly on the campaign timeline, without opening other tools.
What to show depended on the phase of the campaign, and the data came from three sources in BigQuery.

</details>

### C4 · Rules by phase
<details><summary>Show answer</summary>

- **Far in the future:** only the campaign ID.
- **Within six months:** the sales action status, if data exists.
- **Within the next month:** goals and range.
- **Running:** the performance summary.
- **Finished:** the follow-up summary.

Some links were shown only to certain roles, like store employees.
All these rules were in one service with its own unit tests.

</details>

### C4 · Nightly sync, only what changed
<details><summary>Show answer</summary>

Every night a job read the timelines from BigQuery, grouped them per planning node,
and compared them with what we had stored.
Only if a node's state had changed did we update its dimension and marker in the planner, and our table.
Nodes that disappeared from BigQuery were cleaned up.
So the planner wasn't flooded with thousands of identical updates every night.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

Compare before you write.

</details>

---

### C5 · Store setup and deletion protection

### C5 · Store setup
<details><summary>Show answer</summary>

From a file with store code, name and country, we created the store under the right country node,
found by its country code dimension, with its standard subtree, dimensions and groups.
Every step was "find or create", so the same file could be processed again safely.

</details>

### C5 · Deletion protection
<details><summary>Show answer</summary>

Store nodes with orders were expensive to lose, so they had a deletion lock.
A job read the platform's audit log for recent changes to the protection dimensions.
If someone removed a lock where it was still needed, we set it again.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

Protect what is expensive to rebuild.

</details>

---

### C6 · GDPR: anonymising inactive users

### C6 · Action
<details><summary>Show answer</summary>

IKEA had thousands of store users. Users who hadn't logged in for two months had to be anonymised.
- A scheduled job found the inactive users and replaced their personal data through the platform's API.
- Technical and excluded accounts were skipped.
- One failed user didn't stop the run. It was logged and tried again next time.
- Every run wrote an audit file: which accounts were anonymised and when.

</details>

### C6 · Lesson
<details><summary>Show answer</summary>

Privacy jobs must be safe, selective and traceable.

</details>

---

### C7 · One foundation for all components

### C7 · Action
<details><summary>Show answer</summary>

All IKEA components were built on the same multi-module structure:
authentication, a REST client with request logging, scheduled jobs, configuration, the REST API, and the web layer,
plus one module that managed all dependency versions.
Every build ran FindBugs, PMD and JaCoCo with a 90 percent coverage gate,
and every REST API had Swagger documentation.
A new component started from that template, so it was production-ready from the first day.

</details>

### C7 · Lesson
<details><summary>Show answer</summary>

Build the second component faster than the first.

</details>

---

## 6. Запасное: маленькие компоненты

### Extra · Context menu and timeline backend
<details><summary>Show answer</summary>

- A context menu extension in the planner with custom actions for IKEA.
- A small backend inside the planner that served the timeline data to the hover UI.
- An Excel importer that created custom objects from a configured sheet.

</details>

---

## 7. Сами не поднимайте

- **Внутренние имена IKEA:** CAMP, TAKT, SCIN, SDM, PACKA. Говорите «three data sources in BigQuery».
- **Версии Java, WildFly, React.**
- **Точные интервалы cron.** Только «every night», «every two hours».
