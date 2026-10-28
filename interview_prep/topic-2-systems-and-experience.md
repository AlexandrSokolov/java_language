# Topic 2 — The Systems I Worked On & My Experience

*Как пользоваться: HR-версия — для рекрутера и HR, без техники. Техническая версия — для команды. Учите цепочку ключевых слов, а не текст. Ответы — на английском, пояснения — по-русски.*

---

## 1. HR version (about 60 seconds)

**Цепочка:** Product → Customers → My job → Why it mattered

> **[Product]** Uptempo, formerly BrandMaker, makes marketing software for large companies. It has several modules: a calendar for marketing planning, a media pool for images and files, and a workflow tool where teams create, approve and track their marketing work.
>
> **[Customers]** Big companies never use such software alone. They have their own systems. My team connected our product with those systems, and built extensions for what the standard product didn't do. We worked for about twenty large enterprises, like IKEA, Amazon, Deutsche Bank and AT&T, with fifty to a hundred integrations over the years.
>
> **[My job]** My role was to design these integrations and make them reliable and easy to support.
>
> **[Why it mattered]** For example, when something went wrong, the business user saw a clear message right in the system and could often fix it alone, without calling support. That saved time for the customer and support costs for us.

---

## 2. Tech version (2–3 minutes)

**Цепочка:** Modules → Channels → Design → Reliability → Tooling → Modernization → My part in delivery

> **[Modules]** The product was a set of decoupled modules: Marketing Planner, a planning calendar; MediaPool, for digital assets; and Job Manager, a workflow engine. A job is a very flexible entity: it has configurable typed attributes, such as strings, dates, lists and grids, and it moves through a configurable workflow.
>
> **[Channels]** My team built integrations between these modules and the customers' systems. We called the modules through their APIs: first SOAP, and later we migrated everything to REST. Changes came to us as events: first JMS on WildFly, later Kafka, and webhooks for external systems. Bulk data came as CSV and Excel files over SFTP, and we produced PDF reports with JasperReports.
>
> **[Design]** The teams were small, so I designed the integrations end to end: the data model, the communication flow and the error handling.
>
> **[Reliability]** I designed for failure from the start. We validated incoming data. A job with bad data went back one step with a clear comment, and only the integration could move it forward, so it worked as a quality gate. For duplicates we used a mapping table with a unique constraint. And a scheduled reconciliation job found jobs stuck in a status and processed them again, because retries alone don't help when a service is down.
>
> **[Tooling]** When I see the same problem twice, I build a small reusable tool. I built a wrapper around the RESTEasy client with request logging per package, and a typed-configuration library for our Java EE services. Both were shared through Nexus and reused across projects.
>
> **[Modernization]** Over the years we moved our components from a JEE monolith on WildFly to Spring-based microservices in Docker. By the end we were on Java 24.
>
> **[My part in delivery]** I wrote the Dockerfiles and Docker Compose setups, kept my environment as code in Ansible, and built with Jenkins and Maven into Nexus. Production deployment was done by a separate infrastructure team.

---

## 3. "Walk me through one integration"

*Самый частый вопрос технической команды. Основной пример — SFTP-импорт: он показывает всё сразу. Пример B — запасной, если спросят про события.*

### Example A — File import over SFTP → jobs, calendar, files

**Шаги (их и держите в голове):**

1. **Trigger:** the customer's system puts a CSV or Excel file on SFTP.
2. **Structural validation first:** header, required columns, field formats. All errors are collected in one pass. A broken file is rejected as a whole, with a report by email or back on the SFTP.
3. **Streaming:** the file is read row by row, not loaded fully into memory.
4. **Task queue with a worker pool:** big imports are spread over time, so they don't overload the product.
5. **Row processing through REST:** check against Job Manager, Marketing Planner and the external system; create jobs, create calendar entries, upload files and link them.
6. **Row errors don't stop the import.** Status is tracked per import and per row, and duplicates are detected.
7. **Result report in business language:** what was created, what was uploaded, what failed and why. For an unexpected error: the exception message, the entity IDs and the time.

**Spoken version (about 1 minute):**

> A typical example: the customer's system puts a CSV or Excel file on our SFTP, usually hundreds to a few thousand rows. First I validate the structure: the header, the required columns, the formats. I collect all errors in one pass, and if the file is broken, we reject it and send a clear report back. Then I read the file as a stream and put the work into a task queue, so a big import doesn't overload the product. For each row, I check the data against Job Manager, Marketing Planner and the external system through their REST APIs. Then I create the jobs and the calendar entries, and upload and link the files. A bad row doesn't stop the whole import. At the end the customer gets a report in business language: what was created, what was uploaded, and what failed and why.

### Example B — Workflow event → Marketing Planner

> A user creates a project job in Job Manager, enters a budget and moves it forward. A manager approves it. Our integration gets an event, first through JMS and later through Kafka, validates the data and creates the matching entry in the Marketing Planner calendar, with start and end dates. If the data is wrong, we send the job back one step with a clear comment, and the owner and the admins get notified. Only our integration can move that job forward, so bad data can't flow downstream. Each created object carries a comment with links to the job that created it, so business users can always trace where it came from.

---

## 4. Likely follow-up questions

**Why did you move from JMS to Kafka?**
> It was a platform decision by the company. For us it meant we could subscribe to events from our own containers, independent of the product's server.

**How did you handle duplicates?**
> Where it made sense, a mapping table in our own database with a unique constraint on the source ID, using JPA and Hibernate. So the same event couldn't create the same object twice. Where that wasn't possible, every created object got a clear comment with links to its source, so it was always traceable.

**What happened when a service was down?**
> Retries alone don't help when a service is down. So we had a scheduled reconciliation job, running a few times a day, that asked the system which jobs were stuck in a status and processed them again. Support could also trigger it through a REST endpoint.

**How did you handle webhooks?**
> External teams under time pressure had built the webhook handlers synchronously: the whole logic ran before the response, so the sender timed out, and messages were lost or duplicated. I redesigned it. We acknowledge fast, collapse several events for the same entity into one, and process them from a queue.

*Подробная версия этой истории — в теме 4 (сложные задачи).*

**Your queue was in memory. What happens on restart?**
> We lose the queue, but nothing is lost for good. The job status in the source system is the source of truth. Reconciliation, plus the platform's audit service with the history of changes, finds everything that was missed and processes it again. For a few external systems the state lived only in memory. If I built that today, I'd keep it in a database table or in Kafka.

**The product changed with every release. How did you deal with that?**
> New releases often brought breaking changes to the APIs we used. So I built a thin wrapper around our RESTEasy client: I could switch on readable request and response logging for one package. Then I could send the product team the exact raw request within minutes. We shared it as a library through Nexus.

**How did you debug problems across several systems?**
> I read the logs, not only of our components but also of the product, in Grafana with Loki, or from WildFly logs when there was no web access. I never told another team just "it doesn't work". I showed them their error in their logs, right before ours. That's what got problems fixed.

**How did your code get to production?**
> Code in GitLab, builds in Jenkins with Maven, artifacts published to Nexus. Production deployment was done by a separate infrastructure team; we handed over the artifact through a Jira ticket. I also helped IT when they moved to infrastructure as code with Ansible; they needed the developer view.

**What happened if a configuration was wrong?**
> The configuration was validated at startup, so the service failed fast with a clear error. Some things can go wrong later, like expired credentials for an external system. Those errors were caught too, and the user got clear feedback: an email, or a comment right in the job, like "external system not available".

**How did you build the configuration library?**
> [Заполнить одним предложением: например, Java dynamic proxy, где InvocationHandler читает аннотацию и возвращает значение.]

**What have you worked on most recently?**
> In the last year the focus moved from new projects to maintaining and stabilising our existing integrations, including the move to Java 24.

**Do you have Prometheus and Grafana experience?** *(сами не поднимайте)*
> For logs I used Grafana with Loki every day to troubleshoot production. Metrics and dashboards in Prometheus were owned by our infrastructure team. I know how it works and I've set it up myself to learn it, but I haven't owned it in production. My strength in observability is a different layer: making problems visible to business users.

**What's your AWS experience?** *(сами не поднимайте)*
> Limited. In the last year, after the merger with Allocadia, a few projects deployed build output to AWS S3. I know the basics, but it's not my core experience.

---

## 5. Сами не поднимайте

- **Prometheus и метрики.** Ответ выше — только если спросят.
- **AWS S3 и Workato.** Только если спросят.
- **Что Jenkins запускали вручную.** Если спросят про полную автоматизацию: «For small integration projects, on-demand builds were enough; production deployment was a controlled hand-over to the infra team.»
- **Внешние команды по странам и «слабые разработчики».** Только «external teams under time pressure».
- **Что новых проектов последний год не было.** Говорите формулировкой из ответа «most recently».

---

## 6. Key phrases

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

---

## 7. Цифры, которые нужно помнить

| Что | Цифра |
|---|---|
| Крупные клиенты | about 20: IKEA, Amazon, Deutsche Bank, Commerzbank, AT&T, Volvo, Volkswagen, Porsche, UBS, Energizer, Thalia |
| Интеграции за все годы | 50 to 100 |
| Срок разработки одной интеграции | a few weeks to a few months, 2–3 developers |
| Поддержка | several years per integration |
| Команда в Германии | 3–6 developers, 2 testers |
| Размер файлов импорта | hundreds to a few thousand rows |
| Модернизация | JEE monolith on WildFly → Spring microservices in Docker → Java 24 |
