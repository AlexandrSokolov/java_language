# Project Experience — DekaBank: Fund Documents from SFTP to Media Pool and the Web

*Как пользоваться: сначала выучите цепочку проекта. Рассказ — около 3 минут: введение, компоненты, этапы, технологии и один-два челленджа. Главные челленджи — C1 (legacy → new importer), C2 (reliability: lost response, retries, dual write) и C3 (webhooks). Остальные — на уточняющие вопросы. Ответы — на английском, пояснения — по-русски.*

*DekaBank — Wertpapierhaus der Sparkassen, управляет фондами группы Sparkassen. Документы по фондам (проспекты, обязательные публикации) — это регуляторные документы: их нужно опубликовать вовремя и правильно.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Regulated documents → Four components → I took it over → Stages → Tech → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Regulated documents** | Deka публикует документы по фондам: проспекты, обязательные публикации. Ошибка или опоздание — регуляторный риск. | *"The biggest customer project I owned in the last years was for DekaBank…"* |
| 2 | **Four components** | Importer, webhook consumer, Planner extension, плюс старый importer. | *"It wasn't one application, but several components…"* |
| 3 | **I took it over** | Часть начинал другой разработчик: синхронные webhooks, логи невозможно читать. | *"Part of it was started by another developer…"* |
| 4 | **Stages** | SFTP → Excel → snapshot → upload → metadata → publishing → web → reports. | *"The flow looks like this…"* |
| 5 | **Tech** | Spring Boot, Java EE, MySQL, Liquibase, SFTP, Excel, SOAP и REST, webhooks, Docker, Testcontainers. | *"The new importer is a Spring Boot application…"* |
| 6 | **Challenges** | C1 rewrite, C2 reliability, C3 webhooks — главные. | *"The hardest part was reliability."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| legacy code · rewrite · taking over a project · state management | **C1. From a legacy importer to a state machine** |
| reliability · dual write · lost responses · retries · idempotency | **C2. Reliability: lost responses, retries and dual writes** |
| webhooks · event-driven · async processing · ordering | **C3. Webhooks done right** |
| logging · observability · production support | **C4. Logs you can actually read** |
| consistency between systems · data integrity | **C5. Consistency between Media Pool and the web** |
| validation · business feedback · reports | **C6. Feedback the business can act on** |
| testing · Docker · Testcontainers | **C7. Testing with real infrastructure** |

---

## 2. Введение (около 40 секунд)

### Intro · The business
<details><summary>Show answer</summary>

The biggest customer project I owned in the last years was for DekaBank, the asset manager of the German savings banks.

Deka publishes documents for its funds: sales prospectuses, mandatory publications, fact sheets.
Many of them are regulated. They must be online on time, with the right content and the right metadata.

</details>

### Intro · The components
<details><summary>Show answer</summary>

It wasn't one application, but several components:
- an **importer**: it takes documents and an Excel file with metadata from SFTP servers, and imports them into Media Pool, our digital asset management;
- a **webhook consumer**: it reacts to publishing events from Media Pool, and puts the files on the web with stable public names;
- and a small **extension for Marketing Planner**: custom actions in the context menu of a planning node.

</details>

### Intro · I took it over
<details><summary>Show answer</summary>

Part of it was started by another developer.
The webhooks were handled synchronously, and the logs were full of info messages.
When something went wrong, it was almost impossible to understand what had happened.
I took over the project, rebuilt the importer from scratch, and reworked the webhook handling.

</details>

---

## 3. Как это работало — этапы (около 60–90 секунд)

**Цепочка этапов:** SFTP → Excel → Snapshot → Upload → Metadata → Publishing → Web → Reports

### Stage 1 · SFTP and Excel
<details><summary>Show answer</summary>

Deka's systems put PDF files and an Excel file with metadata into folders on several SFTP servers.
Each line in the Excel describes one document: file name, title, folder, categories, ISIN codes, publishing date.
The importer finds the Excel, parses it and validates every line.

</details>

### Stage 2 · Snapshot
<details><summary>Show answer</summary>

Then it takes a snapshot: it moves the Excel and all listed files into a new folder with a timestamp.
So new uploads from Deka don't mix with the import that is running.
Files that are listed in the Excel but don't exist are marked right away.

</details>

### Stage 3 · Upload
<details><summary>Show answer</summary>

For each line we look for the asset in Media Pool by file name.
- No asset: we upload it as a new asset.
- One asset: we upload the file as a new version.
- Several assets: we can't decide, so the file goes to an error folder and the line is reported.

After a successful upload the file is removed from SFTP.

</details>

### Stage 4 · Metadata
<details><summary>Show answer</summary>

For new assets we set the metadata from the Excel: title, folder, categories, ISINs and custom attributes.
That makes the asset searchable.

</details>

### Stage 5 · Publishing
<details><summary>Show answer</summary>

Then publishing. Documents without a date are published right after the upload.
Documents with a future date are published by a nightly run on that date.

</details>

### Stage 6 · Web
<details><summary>Show answer</summary>

When an asset is published to the public channel, Media Pool sends a webhook.
Our consumer generates the right rendition of the file, and saves it on the web cache under a stable public name.
So the savings banks' websites can link to it.
When the publication ends, the file is removed.

</details>

### Stage 7 · Reports
<details><summary>Show answer</summary>

At the end of every run, the business gets an email with CSV reports:
what was imported, what was published, and what failed and why.
There is a separate, non-technical report in German for the business team.

</details>

---

## 4. Технологии (около 30 секунд)

**Цепочка:** Importer → Webhooks → Integration → Infrastructure

### Tech 1 · The new importer
<details><summary>Show answer</summary>

The new importer is a Spring Boot application, with Spring Data JPA, MySQL and Liquibase.
Apache POI for Excel, JSch for SFTP.

</details>

### Tech 2 · The webhook consumer
<details><summary>Show answer</summary>

The webhook consumer is a Java EE application on WildFly, split into Maven modules:
authentication, REST client, webhook DTOs, configuration, data source, domain and the REST API.

</details>

### Tech 3 · Integration with Media Pool
<details><summary>Show answer</summary>

We talked to Media Pool over SOAP for the file uploads, and over REST for search, metadata and publishing.
Webhooks were signed with RSA, and we verified the signature on every request.

</details>

### Tech 4 · Infrastructure
<details><summary>Show answer</summary>

Locally the whole system ran in Docker Compose: the application, MySQL and an SFTP server.
The integration tests used Testcontainers.
In production, scheduled runs triggered the import, upload and publishing through REST endpoints.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Legacy → state machine** | old importer, state in files → new importer, state in database → statuses per import and per line → errors in their own table → status REST API | *Make the state of a long process explicit, persistent and visible.* |
| **C2. Reliability** | lost response → MD5 to recognise our own upload → every step saved → retries with back-off → outbox for publishing → single run at a time | *Assume every call can fail after it succeeded, and design so that the next run can tell.* |
| **C3. Webhooks** | synchronous handler → acknowledge fast → per-asset order → inbox → re-read current state → signature → replay endpoint and reconciliation | *Acknowledge fast, keep order per entity, and never trust that you received every event.* |
| **C4. Logs** | info spam → right levels → correlation ID per message → request logging only on errors | *A log is for the person who reads it at 3 a.m.* |
| **C5. Consistency** | published in Media Pool ≠ file on the web → check both sides → unique public names | *When two systems must agree, check both of them.* |
| **C6. Feedback** | validation per line → error folder on SFTP → CSV reports by email | *The business should never need to read a log.* |
| **C7. Testing** | Testcontainers for MySQL and SFTP → Docker Compose → integration tests from my machine | *Test against real infrastructure, not against your assumptions.* |

---

### C1 · From a legacy importer to a state machine

*Главная история «я взял чужой проект и переделал».*

### C1 · Situation
<details><summary>Show answer</summary>

The first version of the importer was an old Spring application.
It kept its state in local files, and an import was basically one long run.
If it stopped in the middle, nobody knew which files were done and which weren't.

</details>

### C1 · Action: a new importer
<details><summary>Show answer</summary>

I rebuilt it as a new Spring Boot application, and I designed it as a state machine stored in the database.
- **An import has statuses:** Excel found, Excel parsed, SFTP prepared, upload started, all uploaded, all published.
- **Every line has its own status:** moved, uploaded, metadata updated, published, or a specific error, like duplicated assets or file missing.
- **Every status change has a timestamp.** So we always know where an import is, and since when.

</details>

### C1 · Action: errors in their own table
<details><summary>Show answer</summary>

Errors went into their own table, linked to the line, with the operation and the reason.
A line could have several errors over several attempts, and we kept the history.

</details>

### C1 · Action: status API
<details><summary>Show answer</summary>

And I added a REST API to see everything:
the connection state of the SFTP servers and Media Pool, all imports with their status history,
and every line of an import with its status and the Media Pool asset.
Support could answer most questions without a developer.

</details>

### C1 · Result
<details><summary>Show answer</summary>

An interrupted import simply continued on the next run, from where it stopped.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Make the state of a long process explicit, persistent and visible.

</details>

---

### C2 · Reliability: lost responses, retries and dual writes

*Вторая главная история. Поднимайте её сами.*

### C2 · Situation
<details><summary>Show answer</summary>

Uploads were large PDF files over SOAP, and the Media Pool was not always fast.
The most dangerous failure is not an error. It's a timeout after the server has already done the work.

</details>

### C2 · The lost response
<details><summary>Show answer</summary>

We upload a file, Media Pool stores it, but our call times out. For us it's a failure.
On the next run we try again, and Media Pool says: this media already exists.

So for every file we calculate the MD5 hash.
When Media Pool reports a duplicate, we look up the asset by that hash.
If our previous attempt for this line ended with a timeout, we know it was our own upload.
We take that asset and continue, instead of reporting a false duplicate.
If it was a real duplicate, the file goes to the error folder and the line is reported.

</details>

### C2 · Every step saved
<details><summary>Show answer</summary>

Every step of a line was saved to the database before we went to the next one:
uploaded, removed from SFTP, metadata updated, published.
So after a crash, the next run knew exactly which step to repeat, and which were done.

</details>

### C2 · Retries with back-off
<details><summary>Show answer</summary>

Failed uploads were retried on the next runs, with exponential back-off and a maximum number of attempts.
Before every run we checked the connections to Media Pool and to each SFTP server,
so we didn't start an import when a system was down.
After the last attempt the line was marked as failed and reported. Nothing failed silently.

</details>

### C2 · The dual-write problem and the outbox
<details><summary>Show answer</summary>

Publishing had a classic dual-write problem: we update our database and call Media Pool, and one of the two can fail.
So I used an outbox.
- In one transaction we saved the line status and a publishing request into an outbox table.
- A scheduled worker sent the pending requests to Media Pool.
- A request was marked as done only after Media Pool confirmed it.

If anything crashed in between, the request was simply sent again.
Publishing was idempotent: if the asset was already published, we only set the official version.

</details>

### C2 · One run at a time
<details><summary>Show answer</summary>

The runs were triggered by a scheduler through REST endpoints, and the work ran asynchronously.
If a run was still in progress, a new trigger didn't start a second one. It just returned the state of the running one.
So two runs never worked on the same files.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Assume every call can fail after it succeeded, and design so that the next run can tell.

</details>

### C2 · Follow-up: why MD5 and not just the file name?
<details><summary>Show answer</summary>

The file name tells you which asset it belongs to. It doesn't tell you if the content is the same.
Deka sends new versions of a prospectus under the same name.
The hash tells you if this exact content is already in Media Pool.

</details>

### C2 · Follow-up: what if the worker sends, but crashes before marking it as done?
<details><summary>Show answer</summary>

Then it sends again on the next run. That's at-least-once delivery.
That's why the operation must be idempotent: publishing an already published asset just sets the official version.

</details>

---

### C3 · Webhooks done right

### C3 · Situation
<details><summary>Show answer</summary>

When I took over, the webhook consumer did all the work synchronously, before answering Media Pool:
loading the asset, generating the file, downloading it, saving it to the web.
That took long, Media Pool timed out, and events were lost or sent again.

</details>

### C3 · Acknowledge fast, process asynchronously
<details><summary>Show answer</summary>

I changed it to answer right away with 202 Accepted, after checking the signature,
and to process the event asynchronously.

</details>

### C3 · Order per asset
<details><summary>Show answer</summary>

Events for the same asset must be processed in order. "Published" and then "unpublished" is not the same as the other way round.
So I kept a queue per asset: while one event for an asset was in progress, the next ones waited.
Events for different assets ran in parallel.

</details>

### C3 · Inbox: no event processed twice
<details><summary>Show answer</summary>

Media Pool could send the same event again.
I stored every event ID in an inbox table with a unique constraint.
If the insert failed, the event was a duplicate, and we skipped it.

</details>

### C3 · Re-read the current state
<details><summary>Show answer</summary>

An event tells you what happened some time ago. By the time we process it, things may have changed.
So we never trusted the event payload alone.
We read the current state of the asset from Media Pool, and acted on that.
For example: if the asset was no longer published to our channel, we did nothing.

</details>

### C3 · Replay and reconciliation
<details><summary>Show answer</summary>

And if our component wasn't running, for example during a deployment, the events from that time were lost.
So I added a REST endpoint to process an asset again on demand,
and a scheduled reconciliation job that compared the published assets in Media Pool with the files on the web,
and fixed the differences.

</details>

### C3 · Feedback
<details><summary>Show answer</summary>

If processing failed, the owner of the asset got an email with the error details,
and if we couldn't find the owner, a fallback address got it.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

Acknowledge fast, keep order per entity, and never trust that you received every event.

</details>

### C3 · Follow-up: why not just one thread for everything?
<details><summary>Show answer</summary>

Then one slow asset blocks all the others. Order only matters per asset, so we keep order per asset,
and run different assets in parallel. It's the same idea as partitions in Kafka.

</details>

---

### C4 · Logs you can actually read

### C4 · Situation
<details><summary>Show answer</summary>

The old code wrote thousands of messages at info level, sometimes even with System.out.
When something went wrong, you couldn't find the one line that mattered.

</details>

### C4 · Action
<details><summary>Show answer</summary>

- **Levels.** Info only for business events: an import started, an import finished. Details on debug.
- **Correlation ID.** Every webhook message got its own ID in the logging context,
  so you could filter one message's whole path through the system.
- **Request logging only where it helps.** Full requests and responses on debug, and on errors only the failing call.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

A log is for the person who reads it at three in the morning.

</details>

---

### C5 · Consistency between Media Pool and the web

### C5 · Situation and action
<details><summary>Show answer</summary>

An asset could be published in Media Pool, but the file was missing on the web, or the other way round.
So before publishing we checked both sides, and reported any mismatch.
And public file names had to be unique: two assets could never write to the same public name.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

When two systems must agree, check both of them.

</details>

---

### C6 · Feedback the business can act on

### C6 · Action
<details><summary>Show answer</summary>

- **Validation per line.** Every Excel line was validated: required columns, file name, folder, categories, dates.
  One bad line didn't stop the import.
- **Error folder.** Files that couldn't be imported were moved to an error folder on SFTP, not deleted.
- **Reports.** After each run, an email with CSV reports: processed, problems, published, publishing problems,
  and a non-technical report in German for the business team.

</details>

### C6 · Lesson
<details><summary>Show answer</summary>

The business should never need to read a log.

</details>

---

### C7 · Testing with real infrastructure

### C7 · Action
<details><summary>Show answer</summary>

- **Testcontainers** started a real MySQL and a real SFTP server for the integration tests.
- **Docker Compose** ran the whole system locally: the application, the database, SFTP and the scheduler.
- **Integration tests against a test Media Pool**, which I could run from my machine to reproduce a production problem.

</details>

### C7 · Lesson
<details><summary>Show answer</summary>

Test against real infrastructure, not against your assumptions.

</details>

---

## 6. Запасное: Marketing Planner extension

### Extra · Context menu extension
<details><summary>Show answer</summary>

A small component, also inherited: custom actions in the context menu of a node in Marketing Planner.
The menu items and their links were configurable, and the extension checked the user's login before showing them.
I mostly maintained it and cleaned up the logging.

</details>

---

## 7. Сами не поднимайте

- **Версии Spring, Java, WildFly.** Только «Spring Boot», «Java EE on WildFly».
- **SOAP для загрузки файлов.** Если спросят — «uploads went over SOAP, the rest over REST; we planned to move everything to REST».
- **Детали Excel-колонок и названия CSV-отчётов.**
