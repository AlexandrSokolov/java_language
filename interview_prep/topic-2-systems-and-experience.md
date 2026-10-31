# Topic 2 — The Systems I Worked On & My Experience

*Как пользоваться: HR-версия — для рекрутера и HR, без техники. Техническая версия — для команды. Учите цепочку ключевых слов, а не текст. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочки

Сначала запомните цепочки. Если сбились: вспомните следующее ключевое слово, скажите его первую фразу, и рассказ продолжится.

### HR version (about 60 seconds)

**Цепочка:** Product → Customers → My job → Why it mattered

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Product** | Что за продукт: маркетинговый софт для крупных компаний, три модуля. | *"Uptempo, formerly BrandMaker, makes marketing software for large companies."* |
| 2 | **Customers** | Зачем нужны интеграции: у клиентов свои системы. ~20 клиентов, 50–100 интеграций. | *"Big companies never use such software alone."* |
| 3 | **My job** | Ваша роль одной фразой: проектировал, делал надёжными и простыми в поддержке. | *"My role was to design these integrations…"* |
| 4 | **Why it mattered** | Польза для бизнеса: пользователь сам видит ошибку и часто сам её чинит. | *"For example, when something went wrong…"* |

### Tech version (2–3 minutes)

**Цепочка:** Modules → Channels → Design → Reliability → Tooling → Modernization → My part in delivery

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Modules** | С чем интегрировали: Marketing Planner, MediaPool, Job Manager. Гибкая job. | *"The product was a set of decoupled modules…"* |
| 2 | **Channels** | Как ходили данные: SOAP → REST, JMS → Kafka, webhooks, файлы по SFTP, PDF-отчёты. | *"My team built integrations between these modules and the customers' systems."* |
| 3 | **Design** | Вы проектировали целиком: модель данных, поток, обработка ошибок. | *"The teams were small, so I designed the integrations end to end…"* |
| 4 | **Reliability** | Design for failure: валидация, quality gate, unique constraint, reconciliation. | *"I designed for failure from the start."* |
| 5 | **Tooling** | Ваши две библиотеки в Nexus. | *"When I see the same problem twice, I build a small reusable tool."* |
| 6 | **Modernization** | Монолит на WildFly → Spring-микросервисы в Docker → Java 24. | *"Over the years we moved our components…"* |
| 7 | **My part in delivery** | Docker, Ansible, Jenkins, Maven, Nexus. Прод — отдельная команда. | *"I wrote the Dockerfiles…"* |

### "Walk me through one integration" — Example A: file import over SFTP

*Самый частый вопрос технической команды. Основной пример — SFTP-импорт: он показывает всё сразу.*

**Цепочка:** Trigger → Structure first → Stream & queue → Rows → Bad rows → Report

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Trigger** | Система клиента кладёт CSV или Excel на SFTP. | *"A typical example: the customer's system puts a CSV or Excel file on our SFTP…"* |
| 2 | **Structure first** | Сначала структура: заголовок, колонки, форматы. Все ошибки за один проход. Битый файл — отказ целиком, с отчётом. | *"First I validate the structure…"* |
| 3 | **Stream & queue** | Читаем построчно, а не целиком в память. Очередь задач с пулом воркеров, чтобы не перегрузить продукт. | *"Then I read the file as a stream…"* |
| 4 | **Rows** | Каждая строка через REST: проверка, создание jobs и записей календаря, загрузка и привязка файлов. | *"For each row, I check the data…"* |
| 5 | **Bad rows** | Ошибка в строке не останавливает импорт. Статус по импорту и по строке, дубликаты распознаются. | *"A bad row doesn't stop the whole import."* |
| 6 | **Report** | Отчёт бизнес-языком. При неожиданной ошибке — текст исключения, ID сущностей и время. | *"At the end the customer gets a report in business language…"* |

### Example B: workflow event → Marketing Planner

*Запасной пример, если спросят про события.*

**Цепочка:** Event → calendar → Bad data goes back → Traceability

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Event → calendar** | Одобренная job → событие (JMS, потом Kafka) → запись в календаре. | *"A user creates a project job in Job Manager…"* |
| 2 | **Bad data goes back** | Плохие данные: job на шаг назад с комментарием. Дальше её двигает только интеграция. | *"If the data is wrong, we send the job back one step…"* |
| 3 | **Traceability** | Каждый созданный объект ссылается на job-источник. | *"Each created object carries a comment with links…"* |

---

## 2. HR version — cards

### HR 1 · Product
<details><summary>Show answer</summary>

Uptempo, formerly BrandMaker, makes marketing software for large companies.
It has several modules: a calendar for marketing planning, a media pool for images and files,
and a workflow tool where teams create, approve and track their marketing work.

</details>

### HR 2 · Customers
<details><summary>Show answer</summary>

Big companies never use such software alone. They have their own systems.
My team connected our product with those systems, and built extensions for what the standard product didn't do.
We worked for about twenty large enterprises, like IKEA, Amazon, Deutsche Bank and AT&T,
with fifty to a hundred integrations over the years.

</details>

### HR 3 · My job
<details><summary>Show answer</summary>

My role was to design these integrations and make them reliable and easy to support.

</details>

### HR 4 · Why it mattered
<details><summary>Show answer</summary>

For example, when something went wrong, the business user saw a clear message right in the system
and could often fix it alone, without calling support.
That saved time for the customer and support costs for us.

</details>

---

## 3. Tech version — cards

### Tech 1 · Modules
<details><summary>Show answer</summary>

The product was a set of decoupled modules: Marketing Planner, a planning calendar;
MediaPool, for digital assets; and Job Manager, a workflow engine.
A job is a very flexible entity: it has configurable typed attributes, such as strings, dates, lists and grids,
and it moves through a configurable workflow.

</details>

### Tech 2 · Channels
<details><summary>Show answer</summary>

My team built integrations between these modules and the customers' systems.
We called the modules through their APIs: first SOAP, and later we migrated everything to REST.
Changes came to us as events: first JMS on WildFly, later Kafka, and webhooks for external systems.
Bulk data came as CSV and Excel files over SFTP, and we produced PDF reports with JasperReports.

</details>

### Tech 3 · Design
<details><summary>Show answer</summary>

The teams were small, so I designed the integrations end to end:
the data model, the communication flow and the error handling.

</details>

### Tech 4 · Reliability
<details><summary>Show answer</summary>

I designed for failure from the start. We validated incoming data.
A job with bad data went back one step with a clear comment, and only the integration could move it forward,
so it worked as a quality gate.
For duplicates we used a mapping table with a unique constraint.
And a scheduled reconciliation job found jobs stuck in a status and processed them again,
because retries alone don't help when a service is down.

</details>

### Tech 5 · Tooling
<details><summary>Show answer</summary>

When I see the same problem twice, I build a small reusable tool.
I built a wrapper around the RESTEasy client with request logging per package,
and a typed-configuration library for our Java EE services.
Both were shared through Nexus and reused across projects.

</details>

### Tech 6 · Modernization
<details><summary>Show answer</summary>

Over the years we moved our components from a JEE monolith on WildFly to Spring-based microservices in Docker.
By the end we were on Java 24.

</details>

### Tech 7 · My part in delivery
<details><summary>Show answer</summary>

I wrote the Dockerfiles and Docker Compose setups, kept my environment as code in Ansible,
and built with Jenkins and Maven into Nexus.
Production deployment was done by a separate infrastructure team.

</details>

---

## 4. "Walk me through one integration" — cards

### Example A — file import over SFTP (about 1 minute)

### A1 · Trigger
<details><summary>Show answer</summary>

A typical example: the customer's system puts a CSV or Excel file on our SFTP,
usually hundreds to a few thousand rows.

</details>

### A2 · Structure first
<details><summary>Show answer</summary>

First I validate the structure: the header, the required columns, the formats.
I collect all errors in one pass, and if the file is broken, we reject it and send a clear report back.

</details>

### A3 · Stream & queue
<details><summary>Show answer</summary>

Then I read the file as a stream and put the work into a task queue,
so a big import doesn't overload the product.

</details>

### A4 · Rows
<details><summary>Show answer</summary>

For each row, I check the data against Job Manager, Marketing Planner and the external system
through their REST APIs. Then I create the jobs and the calendar entries, and upload and link the files.

</details>

### A5 · Bad rows
<details><summary>Show answer</summary>

A bad row doesn't stop the whole import.
We track the status of each import and each row, and we detect duplicates.

</details>

### A6 · Report
<details><summary>Show answer</summary>

At the end the customer gets a report in business language:
what was created, what was uploaded, and what failed and why.

</details>

### Example B — workflow event → Marketing Planner

### B1 · Event → calendar
<details><summary>Show answer</summary>

A user creates a project job in Job Manager, enters a budget and moves it forward. A manager approves it.
Our integration gets an event, first through JMS and later through Kafka, validates the data
and creates the matching entry in the Marketing Planner calendar, with start and end dates.

</details>

### B2 · Bad data goes back
<details><summary>Show answer</summary>

If the data is wrong, we send the job back one step with a clear comment, and the owner and the admins get notified.
Only our integration can move that job forward, so bad data can't flow downstream.

</details>

### B3 · Traceability
<details><summary>Show answer</summary>

Each created object carries a comment with links to the job that created it,
so business users can always trace where it came from.

</details>

---

## 5. Likely follow-up questions

### Why did you move from JMS to Kafka?
<details><summary>Show answer</summary>

It was a platform decision by the company.
For us it meant we could subscribe to events from our own containers, independent of the product's server.

</details>

### How did you handle duplicates?
<details><summary>Show answer</summary>

Where it made sense, a mapping table in our own database with a unique constraint on the source ID,
using JPA and Hibernate. So the same event couldn't create the same object twice.
Where that wasn't possible, every created object got a clear comment with links to its source,
so it was always traceable.

</details>

### What happened when a service was down?
<details><summary>Show answer</summary>

Retries alone don't help when a service is down.
So we had a scheduled reconciliation job, running a few times a day,
that asked the system which jobs were stuck in a status and processed them again.
Support could also trigger it through a REST endpoint.

</details>

### How did you handle webhooks?
<details><summary>Show answer</summary>

External teams under time pressure had built the webhook handlers synchronously:
the whole logic ran before the response, so the sender timed out, and messages were lost or duplicated.
I redesigned it. We acknowledge fast, collapse several events for the same entity into one,
and process them from a queue.

</details>

*Подробная версия этой истории — в теме 4 (сложные задачи).*

### Your queue was in memory. What happens on restart?
<details><summary>Show answer</summary>

We lose the queue, but nothing is lost for good. The job status in the source system is the source of truth.
Reconciliation, plus the platform's audit service with the history of changes,
finds everything that was missed and processes it again.
For a few external systems the state lived only in memory.
If I built that today, I'd keep it in a database table or in Kafka.

</details>

### The product changed with every release. How did you cope?
<details><summary>Show answer</summary>

New releases often brought breaking changes to the APIs we used.
So I built a thin wrapper around our RESTEasy client:
I could switch on readable request and response logging for one package.
Then I could send the product team the exact raw request within minutes.
We shared it as a library through Nexus.

</details>

### How did you debug across several systems?
<details><summary>Show answer</summary>

I read the logs, not only of our components but also of the product, in Grafana with Loki,
or from WildFly logs when there was no web access.
I never told another team just "it doesn't work". I showed them their error in their logs, right before ours.
That's what got problems fixed.

</details>

### How did your code get to production?
<details><summary>Show answer</summary>

Code in GitLab, builds in Jenkins with Maven, artifacts published to Nexus.
Production deployment was done by a separate infrastructure team;
we handed over the artifact through a Jira ticket.
I also helped IT when they moved to infrastructure as code with Ansible; they needed the developer view.

</details>

### Was your pipeline fully automated? *(сами не поднимайте)*
<details><summary>Show answer</summary>

For small integration projects, on-demand builds were enough.
Production deployment was a controlled hand-over to the infra team.

</details>

### What happened if a configuration was wrong?
<details><summary>Show answer</summary>

The configuration was validated at startup, so the service failed fast with a clear error.
Some things can go wrong later, like expired credentials for an external system.
Those errors were caught too, and the user got clear feedback:
an email, or a comment right in the job, like "external system not available".

</details>

### How did you build the configuration library?
<details><summary>Show answer</summary>

*[Заполнить одним предложением: например, Java dynamic proxy, где InvocationHandler читает аннотацию
и возвращает значение.]*

</details>

### What have you worked on most recently?
<details><summary>Show answer</summary>

In the last year the focus moved from new projects to maintaining and stabilising our existing integrations,
including the move to Java 24.

</details>

### Do you have Prometheus and Grafana experience? *(сами не поднимайте)*
<details><summary>Show answer</summary>

For logs I used Grafana with Loki every day to troubleshoot production.
Metrics and dashboards in Prometheus were owned by our infrastructure team.
I know how it works and I've set it up myself to learn it, but I haven't owned it in production.
My strength in observability is a different layer: making problems visible to business users.

</details>

### What's your AWS experience? *(сами не поднимайте)*
<details><summary>Show answer</summary>

Limited. In the last year, after the merger with Allocadia, a few projects deployed build output to AWS S3.
I know the basics, but it's not my core experience.

</details>

---

## 6. Numbers to remember

### How many customers did you work for?
<details><summary>Show answer</summary>

About twenty large enterprises, for example IKEA, Amazon, Deutsche Bank, Commerzbank, AT&T, Volvo,
Volkswagen, Porsche, UBS, Energizer and Thalia.

</details>

### How many integrations did you build?
<details><summary>Show answer</summary>

Fifty to a hundred over the years.

</details>

### How long did one integration take?
<details><summary>Show answer</summary>

A few weeks to a few months, with two or three developers.
After that we supported it for several years.

</details>

### How big was your team?
<details><summary>Show answer</summary>

In Germany, three to six developers and two testers.

</details>

### How big were the import files?
<details><summary>Show answer</summary>

Hundreds to a few thousand rows.

</details>

---

## 7. Сами не поднимайте

- **Prometheus и метрики.** Карточка выше — только если спросят.
- **AWS S3 и Workato.** Только если спросят.
- **Что Jenkins запускали вручную.** Если спросят про полную автоматизацию — карточка «Was your pipeline fully automated?».
- **Внешние команды по странам и «слабые разработчики».** Только «external teams under time pressure».
- **Что новых проектов последний год не было.** Говорите формулировкой из карточки «What have you worked on most recently?».

---

## 8. Key phrases

| Фраза | Когда использовать |
|---|---|
| **design for failure** | общий принцип ваших интеграций |
| **validate incoming data** | что вы делали первым делом |
| **a quality gate: bad data can't move downstream** | блокировка job через права |
| **idempotency with a unique constraint** | защита от дубликатов |
| **reconciliation against the source of truth** | поиск «застрявших» jobs |
| **retries alone don't help when a service is down** | почему нужна reconciliation |
| **acknowledge fast, process asynchronously** | история с webhooks |
| **collect all errors in one pass instead of failing fast** | валидация файлов |
| **a business-readable report** | отчёты для пользователей |
| **traceability: every object links back to its source** | комментарии со ссылками |
| **when I see the same problem twice, I build a reusable tool** | ваши две библиотеки |
