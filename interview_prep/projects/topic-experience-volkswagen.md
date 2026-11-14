# Project Experience — Volkswagen: Approved Assets to Adobe AEM, and Audit-Proof Approval Reports

*Как пользоваться: у VW несколько компонентов. Главные — AEM connector (передача одобренных материалов из нашей платформы в медиабазу VW на Adobe AEM Cloud) и approval report (PDF-отчёт о согласовании). Рассказ — около 3 минут. Главный челлендж — C1. Ответы — на английском, пояснения — по-русски.*

*Суть: маркетинг Volkswagen согласовывает материалы (изображения, документы) в Job Manager. Одобренные материалы должны попасть в центральную медиабазу VW, построенную на Adobe AEM Cloud, с правильными метаданными. А для каждого согласования нужен отчёт: кто, когда и что одобрил.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Approval in our platform → VW's media database on AEM → Connector → Approval report → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Approval** | Материалы согласовываются в jobs нашей платформы. | *"At Volkswagen, marketing assets were reviewed and approved in our Job Manager…"* |
| 2 | **AEM** | Центральная медиабаза VW — на Adobe AEM Cloud. | *"…but the central media database of the group runs on Adobe Experience Manager in the cloud."* |
| 3 | **Connector** | Из grid в job: файл + метаданные → AEM, статус по каждой строке. | *"My connector moved the approved assets there…"* |
| 4 | **Approval report** | PDF на двух языках, кто и когда одобрил, как новая версия asset'а. | *"And for every approval we generated an audit report…"* |
| 5 | **Challenges** | C1 connector — главный. | *"The hardest part was making the transfer reliable."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| integration with an enterprise system · reliability · large files · OAuth2 | **C1. Reliable transfer to Adobe AEM** |
| reporting · PDF · audit · multi-language | **C2. An audit-proof approval report** |
| debugging · Docker · concurrency · production issues | **C3. A bug that only happened in the container** |
| exports · Excel | **C4. Excel exports** |

---

## 2. Введение (около 40 секунд)

### Intro · The business
<details><summary>Show answer</summary>

At Volkswagen, marketing assets, like images and documents, were reviewed and approved in our Job Manager.
But the central media database of the group runs on Adobe Experience Manager in the cloud.
Approved assets had to get there, with the right metadata, so other VW teams and brands could find and use them.

</details>

### Intro · The components
<details><summary>Show answer</summary>

- An **AEM connector**: it transfers approved assets from a job into AEM, with their metadata.
- An **approval report**: for every approval it generates a PDF in English and German: who approved what and when.
- And two **Excel exporters**, for jobs and for the marketing plan.

</details>

---

## 3. Как это работало — этапы (около 60 секунд)

**Цепочка этапов:** Grid → Filter → Duplicate check → Upload → Metadata → Status

### Stage 1 · The grid
<details><summary>Show answer</summary>

In the job there is a grid: one row per asset.
In each row the user selects the asset from our Media Pool and fills in the metadata for AEM: title, categories and so on.
Many of the values are chosen from lists that we keep in our own custom structures.

</details>

### Stage 2 · Filter
<details><summary>Show answer</summary>

When the job reaches the transfer step, we take only the rows that are complete,
have exactly one asset, and are not transferred yet.

</details>

### Stage 3 · Duplicate check
<details><summary>Show answer</summary>

Before uploading, we ask AEM if a file with this name already exists.
If it does, we don't overwrite it. The row is marked as failed with a clear note.

</details>

### Stage 4 · Upload
<details><summary>Show answer</summary>

Then we upload the file using AEM's direct binary upload, in three steps:
initiate the upload, send the file in parts, and complete it.

</details>

### Stage 5 · Metadata
<details><summary>Show answer</summary>

Then we set the metadata in AEM. The values from our lists are translated into the labels AEM expects.
We also update the metadata of all transferred assets in our Media Pool, in a single call.

</details>

### Stage 6 · Status
<details><summary>Show answer</summary>

Every row gets a status, done or failed, and a note with the file name or the error.
When all rows are done, the job gets a flag that the transfer was successful.

</details>

---

## 4. Технологии (около 30 секунд)

### Tech · Stack
<details><summary>Show answer</summary>

- Spring Boot services, packaged as Docker images, configured with environment variables.
- WebClient and RestTemplate for the REST calls to Job Manager, Media Pool and AEM.
- OAuth2 against Volkswagen's identity provider for AEM, and JWT for our platform.
- JasperReports for the PDF, Apache POI for the Excel exports.
- Our shared libraries for file upload, comments and error handling.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Transfer to AEM** | status per row → safe restart → duplicate check → upload in parts → retries with back-off → token cache → feedback in the grid | *Keep the state where the user can see it, and make every row restartable on its own.* |
| **C2. Approval report** | history of the approval → PDF in two languages → new version, not a new file → errors in a support discussion | *An audit document must be complete, reproducible and in the reader's language.* |
| **C3. Container-only bug** | works locally, fails in Docker → remote debugging in the container → class loader and fonts | *If it fails only in production, reproduce production, not your laptop.* |
| **C4. Exports** | jobs and plan into Excel | *Give people their data in the tool they already use.* |

---

### C1 · Reliable transfer to Adobe AEM

*Главная история проекта.*

### C1 · Situation
<details><summary>Show answer</summary>

A job could have dozens of assets, some of them large files.
AEM is an external enterprise system with its own identity provider.
Any upload could fail in the middle, and the user must never end up with half a transfer and no idea what happened.

</details>

### C1 · Status per row
<details><summary>Show answer</summary>

The state of the transfer lived in the grid itself: every row had a status and a note.
We processed only rows that were not done yet.
So if the transfer stopped in the middle, the next run continued with the remaining rows,
and the rows that were done were never sent twice.

</details>

### C1 · Duplicate check
<details><summary>Show answer</summary>

Before every upload we checked if the file already existed in AEM.
That protected AEM from duplicates, and protected us from overwriting another team's asset with the same name.

</details>

### C1 · Upload in parts, with retries
<details><summary>Show answer</summary>

Large files were uploaded in parts, with AEM's direct binary upload.
Temporary errors, like timeouts or 5xx from AEM, were retried with exponential back-off.
Only after the last attempt was the row marked as failed.

</details>

### C1 · Token cache
<details><summary>Show answer</summary>

The access token from VW's identity provider was cached until shortly before it expired,
so we didn't ask the identity provider for a new token for every file.

</details>

### C1 · Feedback
<details><summary>Show answer</summary>

The user saw the result directly in the grid: done, with the file name, or failed, with the reason.
And when everything was done, the job was flagged as successfully transferred, so the workflow could continue.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Keep the state where the user can see it, and make every row restartable on its own.

</details>

### C1 · Follow-up: why not store the status in your own database?
<details><summary>Show answer</summary>

Because the user works in the job. If the status is in the grid, they see it without another tool,
and support sees the same thing. And the job is already the source of truth for the approval.

</details>

---

### C2 · An audit-proof approval report

### C2 · Situation
<details><summary>Show answer</summary>

For every approval, VW needed a document: which assets were reviewed, who approved or commented, and when.
In English and in German.

</details>

### C2 · Action
<details><summary>Show answer</summary>

- A button in the job triggered the report.
- We collected the history of the approval process: every decision and every comment, with the user and the time.
- JasperReports generated the PDF, once per language.
- If a report already existed, the new one was uploaded as a new version of the same asset,
  so the history of the report itself was kept. Otherwise a new asset was created and linked to the job.
- If anything failed, the error went into a dedicated support discussion in the job, created if it didn't exist yet.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

An audit document must be complete, reproducible and in the reader's language.

</details>

---

### C3 · A bug that only happened in the container

### C3 · Situation
<details><summary>Show answer</summary>

The report worked perfectly on my machine. In the Docker container it sometimes failed,
and the error didn't even appear in the job.

</details>

### C3 · Action
<details><summary>Show answer</summary>

I started the same image locally with remote debugging enabled, and attached the debugger to the container.
There were two causes.
First, resources like the logo were loaded in a way that worked from my IDE, but not reliably inside the packaged Spring Boot jar
when several reports were generated in parallel. I changed how resources were loaded, and tested it with fifty parallel loads.
Second, the container didn't have the fonts the report needed, so I packaged the fonts with the application.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

If it fails only in production, reproduce production, not your laptop.

</details>

---

### C4 · Excel exports

### C4 · Action
<details><summary>Show answer</summary>

Two small tools exported jobs and the marketing plan into Excel,
so the business could analyse them in the tool they already use.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

Give people their data in the tool they already use.

</details>

---

## 6. Сами не поднимайте

- **Внутренние имена VW** (MDB, P10). Говорите «the group's central media database on Adobe AEM».
- **Логины, пароли и детали входа в AEM.** Никогда.
- **Что часть connector'а начинала внешняя команда.** Если спросят — «I took it over and made it production-ready».
