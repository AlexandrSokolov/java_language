# When they ask about other projects

> I worked on many projects for customers like Volkswagen, IKEA, Deutsche Bank and Amazon, where our services had to work together with other systems: Google BigQuery, external customer APIs, or several BrandMaker modules. The business cases were different, but the core problems were always the same: reliability, consistency, and communication over the network. So I used the same set of solutions I just described.
>
> The differences were mostly in the details. For example, in the Amazon project I didn't get webhooks: I consumed events from Kafka. And instead of my own state machine, I used the Job Manager, where every job already has its own status.

**🔁 BRIDGE:** *"If you're interested, I can tell you more about the Amazon one."*

---

# Soft phrases: disagree without arguing

- *"In my experience…"*
- *"It depends a bit on the context."*
- *"I see it a little differently in some cases."*
- *"The rule is good as a default, but…"*
- *"I agree in most cases, but…"*
- *"One thing I'd add is…"*

---

# ⚠️ ЗАВТРА НАЧИНАЕМ С ЭТОГО (план на 16.11.2026)

## 1. Virtual threads — применимость и поиск кейса
- Комментарий пользователя: webhooks обрабатывались **асинхронно**, загрузка и concurrency cap были **полностью под контролем** → необходимости в virtual threads не было. Это нужно описать и объяснить.
- Найти use case, где virtual threads **действительно нужны**, и логику, как к этому решению пришли (два условия: blocking I/O **и** нехватка потоков в пуле).
- Как определить, что пул забит и потоки **ждут**, а не работают: метрики `executor.active` / `executor.queued` + низкий CPU; `cpu=` в thread dump (несколько dump'ов); JFR в production.

## 2. Отмена запущенной задачи
- Use case-кандидат: идёт обработка PUBLISHED, приходит DEPUBLISHED для того же asset'а → текущую задачу отменить.
- Отмена в Java кооперативная: `Future.cancel(true)` = interrupt, задача сама проверяет его между шагами.
- Придумать, как это вписать в опыт Deka.

## 3. Потом — снова прогон Deka как на собеседовании

## 4. Amazon (после Deka)
- Многие решения из Deka применимы и к Amazon, с другой спецификой (Job Manager, SOAP → REST, network/REST-коммуникация): outbox, retries + circuit breaker, транзакции отдельно от remote-вызовов, concurrency cap. Проверить и перенести.

---

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

## Opening

**Вопрос:** *"Tell us about the projects you worked on."*

> Sure. I'll take one of my recent projects as an example. The business side is compact, so it's easy to explain in a few minutes. But it has the problems you meet in every distributed system that depends on other services over the network: reliability, consistency, and failures you don't control. Let me first tell you briefly what it was about. Then, if that's okay with you, I'll explain how the system worked, which technologies I used, and the main challenges I solved.

## Project

> One of my last projects was for DekaBank, the investment company of the Sparkassen.
> Deka sends us PDF files and an Excel file with metadata. These are official documents for investors about Deka's funds, so they must be online on time and with correct data.
>
> It was a small distributed system:
> - **Deka's SFTP servers.** Deka puts the PDF files and the Excel file there.
> - **Media Pool**, our digital asset management. It stores the files, their versions and metadata, and publishes them to the **Web Cache**, so Deka's websites can show them.
> - **Our service** in between, with two functions:
>   - **importing**: it reads the Excel and the files from SFTP, uploads them into Media Pool, sets the metadata and publishes the right version;
>   - **webhooks**: Media Pool tells it when a file was published, unpublished or changed, and it makes sure the right file is on the Web Cache.

**🔁 BRIDGE:** *"That's the big picture. Any questions so far?"*

*or:* "Is the big picture clear?" · "Shall I go on with how it worked?"

---

## How it worked

> In short, our service worked in six steps.
>
> **Trigger.** A scheduler started the import and read the input from SFTP.
>
> **Snapshot.** We moved the Excel and its files into a separate folder, so they were never processed twice.
>
> **Upload.** We uploaded the files to Media Pool asynchronously: upload and move on.
>
> **Webhook.** Media Pool processed the files on its side and notified us when they were ready. We saved every notification first and processed it later.
>
> **Publish.** We updated the metadata and published the right version to the Web Cache.
>
> **Report.** At the end, the business got a report: what worked and what failed.

**🔁 BRIDGE:** *"So that's how it worked. Shall I go on with the technologies?"*

*or:* "Any questions about the flow?" · "Is that clear so far?"

---

## Tech

> Our service was one Spring Boot application, with both functions: importing and webhooks.
> For data, we used Spring Data JPA with MySQL, and Liquibase for the database changes.
> For Media Pool, we first used SOAP for uploads and REST for everything else, and later we moved everything to REST.
> Locally the whole system ran in Docker Compose, and the integration tests used Testcontainers.

**❓ ASK THE TEAM:** *"Which of these do you use in your team?"*

*or:* "Is your stack similar?" · "Do you also work with Testcontainers?"

**🔁 BRIDGE:** *"And now the most interesting part, the challenges."*

---

## Challenges

> The project was old and built by another team. Reliability and maintainability were weak, so I focused on these two areas.

### Reliability

| ID | Problem | Solution | How, in short |
|---|---|---|---|
| [CR1](#cr1--state-machine) | An import failed in the middle, and nobody knew which files were done | **State machine** | Status per import and per file, every step saved; the next run retries from that step |
| [CR2](#cr2--communication-with-external-systems) | **Communication with external systems** | **Retries, circuit breaker, concurrency cap** | Back-off for short failures, stop calls during outages, max 10 parallel uploads |
| [CR3](#cr3--dual-write) | **Dual write:** consistency between our database and an external system without transactions | **Outbox** | One local transaction, a worker sends, idempotent publish |
| [CR4](#cr4--network-calls-out-of-transactions) | **Long transactions around network calls** | **Network calls out of transactions** | Short local transaction per step; remote calls outside; error status saved in its own transaction |
| [CR5](#cr5--event-handling) | **Event handling** | **Local queue, duplicate check, reconciliation** | Answer 202 fast and store the event in our local queue; check for duplicated events by hash; reconciliation for lost events |

### Maintainability

| ID | Problem | Solution | How, in short |
|---|---|---|---|
| [CM1](#cm1--state-history) | When the client said "it didn't work yesterday", nobody could tell why | **State history** | The same state machine as in CR1: status and timestamp for every import and every line, so we could see what happened at any moment: SFTP closed, Media Pool down, our app undeployed |
| [CM2](#cm2--visible-and-actionable-errors) | Errors were invisible or understandable only for developers | **Visible and actionable errors** | The main principle: validation at every step, and every error says clearly what's wrong, so the business can fix it without contacting us |
| [CM3](#cm3--testability) | **Most errors came from communication with other systems** | **Integration tests per layer, in addition to unit tests** | Test each layer on its own, mock or replace any system, break things in a controlled way |

---

**🔁 BRIDGE:** *"Let me start with reliability."*

### CR1 · State machine

> **Problem.** The first problem: an import is a long process with many steps, and it could fail in the middle: SFTP not available, a timeout from Media Pool, or our service restarted. After that, nobody knew which files were done and which weren't.
>
> **Solution.** So I designed the import as a state machine, stored in the database.
>
> **How.** Every import and every line had its own status with a timestamp: uploaded, metadata updated, published, or a specific error. Every step was saved before the next one started. So when something failed, the next run simply continued from that step and retried only what wasn't done.

**🔁 BRIDGE:** *"So we always knew where every file was. Should I go deeper here, or shall I move on?"* → *"The next problem was communication with external systems."*

*or:* "Any questions about this part?" · "Is that enough detail?"

### CR2 · Communication with external systems

> **Problem.** Network calls to external systems can always fail: a timeout, a system that is down or overloaded. In our case, it was Media Pool.
>
> **Solution.** Retries with exponential back-off, a circuit breaker and a concurrency cap.
>
> **How.** When a call failed, we retried with exponential back-off: after one second, two, four. If the system was down completely or overloaded, the circuit breaker opened and stopped the next calls. And with the concurrency cap, we uploaded at most ten files in parallel, so we never overloaded Media Pool ourselves.

**❓ ASK THE TEAM:** *"Do you use Resilience4j, or something else?"*

*or:* "How do you handle failures of external systems in your services?"

**🔁 BRIDGE:** *"Retries make a single call reliable. But there was a bigger question: how do you keep your database consistent with an external system that doesn't support transactions? That's the classic dual-write problem."*

*or:* "Should I go deeper, or move on?"

### CR3 · Dual write

> **Problem.** We had to keep two things consistent: the status in our database and a request to Media Pool. Media Pool doesn't support transactions, so there is no transaction across both. And we didn't want to keep a database transaction and a connection open during a remote call.
>
> **Solution.** The outbox pattern.
>
> **How.** In one local transaction, we updated the status and saved the request into an outbox table. A separate worker picked up the pending requests and sent them to Media Pool, with retries. A request was marked as done only after Media Pool confirmed it. If anything crashed in between, the request was simply sent again, so publishing had to be idempotent: publishing an already published asset only set the official version.

**❓ ASK THE TEAM:** *"How do you handle this in your services? Do you use an outbox?"*

*or:* "Is this something you deal with in your system too?"

**🔁 BRIDGE:** *"And there was a related trap: long transactions around network calls."*

### CR4 · Network calls out of transactions

> **Problem.** A call to Media Pool could take a long time, especially an upload of a large file. If it runs inside a database transaction, the transaction holds locks and a connection all this time. And it can't roll back Media Pool anyway: what was uploaded stays uploaded.
>
> **Solution.** I kept network calls out of transactions.
>
> **How.** Each step had its own short transaction, only with our own database. The call to Media Pool happened outside of any transaction. If the call failed, nothing was rolled back: we saved the error status in its own short transaction, so the status was always correct.

**🔁 BRIDGE:** *"So our calls to Media Pool were reliable. The last problem was the other direction: events from Media Pool to us."*

*or:* "Any questions about this part?" · "Should I go deeper, or move on?"

### CR5 · Event handling

> **Problem.** The next problem was event handling. Originally, the webhooks were processed synchronously: all the work happened before we answered. It worked only sometimes. We had timeouts, lost and duplicated events, and when something broke, nobody could tell if Media Pool hadn't sent the event or we had lost it.
>
> **Solution.** A local queue, a duplicate check and reconciliation.
>
> **How.** We answered 202 Accepted right away and put the event into a local queue. Duplicated events were skipped with the help of a hash. For lost events, we used reconciliation: thanks to the state machine, we knew which assets had no notification for too long, so a job asked Media Pool for their current state and continued.
>
> **Why not Kafka?** A persistent queue only helps when an event reached us and we didn't process it in time. But an event might never arrive at all. Reconciliation covered both cases, so simpler infrastructure was the better choice.

**❓ ASK THE TEAM:** *"Do you work with events a lot in your system? Kafka, or something else?"*

*or:* "How do you deal with lost events?"

**🔁 BRIDGE:** *"That's reliability. The other side was maintainability."*

*or:* "Shall I go on with maintainability?" · "Any questions about reliability?"

> The original code was hard to read, and the logs were full of noise: duplicated errors, System.out messages you couldn't switch off, and the root cause was hard to find. So first I cleaned that up: proper log levels, configurable per package, and a correlation ID. But the real problems were these three.

### CM1 · State history

> **Problem.** When the client said "the import didn't work yesterday", nobody could tell why.
>
> **Solution.** State history: the same state machine as in reliability.
>
> **How.** Every status change had a timestamp, for every import and every line. So we could see what happened at any moment: SFTP was closed, Media Pool was down, or our app was undeployed. We exposed it through a simple REST API that returned JSON. Both our support and the administrators on the client's side used it.

**🔁 BRIDGE:** *"So one design decision helped with both reliability and maintainability. The next point was errors."*

### CM2 · Visible and actionable errors

> **Problem.** Errors were often invisible, or clear only to developers.
>
> **Solution.** Our main principle: every error must be visible and clear.
>
> **How.** We validated the data at every step. When something was wrong, the error said exactly what and where: which file, which line, which field. So the business could fix it themselves, without contacting us. That was the principle we always aimed for.

**🔁 BRIDGE:** *"And the last point was testing."*

### CM3 · Testability

> **Problem.** Our service didn't have much computation, and unit tests covered that logic. But most of the work was communication with other systems, and that's where most errors came from.
>
> **Solution.** So in addition to unit tests, we put a strong focus on integration tests per layer.
>
> **How.** Each layer could be tested on its own: the SFTP client, the Media Pool client, or the processing of one asset by its ID. Any system could be mocked or replaced. So I could break things in a controlled way, for example a timeout or a system that is down, and see how our service reacts.

**❓ ASK THE TEAM:** *"How do you test integrations with external systems in your team?"*

*or:* "Do you also test failures on purpose?"

**🔁 BRIDGE:** *"So that's the project in short. I'm happy to go deeper into any part."*

---

## Технические вопросы по проекту

*Короткие вопросы, которые могут задать по проекту. Сверху — самые важные для любого программиста, ниже — специфика проекта.*

### Must-have

#### Did you have race conditions?

Yes, in a few places, and I designed against them:
- **Two imports at the same time.** Only one import could run; a new trigger just returned the state of the running one.
- **Deka uploading new files during an import.** We moved the Excel and its files into a separate folder first, so the running import never saw new files.
- **Two events for the same asset at the same time.** A queue per asset, so events for one asset were processed one after another.
- **Two assets writing the same public file name.** A unique constraint in the database prevented that.

*Race condition — результат зависит от порядка параллельных операций. Типичные решения в базе: unique constraint, optimistic locking (`@Version`), `SELECT ... FOR UPDATE` (или `SKIP LOCKED` для очередей).*

#### How did the outbox work?

In one local transaction, we updated the line status and inserted a request into an outbox table.
A scheduled worker picked up the pending requests every few seconds and sent them to Media Pool.
It selected by status, not by time, so nothing was lost when the application was down.
Every request had the number of attempts and the time of the next attempt. After the last attempt it was marked as failed and reported.
A request was marked as done only after Media Pool confirmed it.

*Детали: индекс на `(status, next_attempt_at)`; `@Scheduled(fixedDelay = 5000)`; при нескольких экземплярах — `SELECT ... FOR UPDATE SKIP LOCKED`; отправленные записи периодически удаляются.*

#### What if the worker sends, but crashes before marking it as done?

Then it sends again on the next run. That's at-least-once delivery.
So the operation must be idempotent: publishing an already published asset just sets the official version.

#### How did retries and the circuit breaker work together?

Resilience4j on the calls to Media Pool.
Retry with exponential back-off and jitter handles short failures. The circuit breaker handles long ones: after many failures in a row it opens, and the next calls fail at once.
Retry is the outer layer. When the circuit breaker is open, we don't retry: we stop, and the next run continues from where we stopped.
Before every run we also checked the connections to Media Pool and each SFTP server.

*Детали: `resilience4j-spring-boot3` + `spring-boot-starter-aop`, `@Retry(name = "mediaPool")` и `@CircuitBreaker(name = "mediaPool")`, настройки в `application.yml`. Ловушки: self-invocation; retry не должен повторять `CallNotPermittedException` (`ignoreExceptions`).*

#### Why not one long transaction over the whole import?

It holds locks and a database connection for minutes.
And it can't roll back Media Pool anyway: what was uploaded stays uploaded.
So each step had its own short transaction, and the remote call happened outside of any transaction.

*В Spring: `@Transactional` на маленьких методах «сохранить статус + outbox» и «отметить как выполненный», но не на методе с вызовом Media Pool. Отдельный бин или `TransactionTemplate` — из-за self-invocation.*

#### How did you limit concurrency?

A fixed thread pool of ten threads for the uploads. When all ten were busy, the next file waited in the queue.

*Варианты: `ThreadPoolTaskExecutor` с `corePoolSize = maxPoolSize = 10`; `Semaphore(10)`; `@Bulkhead` из Resilience4j; лимит соединений в HTTP-клиенте. «Почему не parallel streams» — общий ForkJoinPool, размер не контролируется, не для блокирующего I/O.*

#### How did you keep the order of events?

Order matters only per asset: "published" and then "unpublished" is not the same as the other way round.
So we kept a queue per asset: while one event was in progress, the next ones for this asset waited.
Events for different assets ran in parallel. One thread for everything would let one slow asset block all the others. It's the same idea as partitions in Kafka.

#### Why did you re-read the asset instead of trusting the event?

An event tells you what happened some time ago. By the time we process it, things may have changed.
So we read the current state of the asset from Media Pool and acted on that.

### Project-specific

#### How did you track the progress? Why a state machine, and not just statuses?

Every import and every line had a status for every step, stored in the database with a timestamp.
A line can only move from one status to the next one, like uploaded, then metadata updated, then published, or to an error. That's why it's a state machine.
Errors went into their own table, linked to the line, with the operation and the reason, and we kept the history over several attempts.

#### What could you see in the status API?

The connection state of the SFTP servers and Media Pool, all imports with their status history, and every line of an import with its status and the Media Pool asset.
Plain JSON. Our support and the administrators on the client's side used it.

#### How did you find lost webhooks?

A lost webhook means a line stays too long in the same status, for example "uploaded, waiting for the webhook".
A scheduled job found these lines and asked Media Pool for the current state. If the asset was ready, we continued; if not, we tried again later or marked the line as failed.
For published assets, the job also checked with a HEAD request that the file really was on the Web Cache.
And there was a REST endpoint to process one asset again on demand.

#### How did Media Pool and the web stay consistent?

An asset could be published in Media Pool but missing on the web, or the other way round.
So we checked both sides and reported any mismatch.
And public file names had to be unique: two assets could never write to the same public name.

#### What if the same file was uploaded twice?

We upload a file, Media Pool stores it, but our call times out. For us it's a failure.
On the next run we try again, and Media Pool says: this media already exists.
So for every file we calculated the MD5 hash. On a duplicate, we looked up the asset by that hash.
If our previous attempt for this line ended with a timeout, it was our own upload, and we continued.
If it was a real duplicate, the file went to the error folder and the line was reported.

#### Why MD5 and not just the file name?

The file name tells you which asset it belongs to, not if the content is the same.
Deka sends new versions of a prospectus under the same name.
The hash tells you if this exact content is already in Media Pool.

#### What was wrong when you took it over?

The project was built by another team and handed over to me.
When something failed, the logs showed an error, but not the cause: which file, which asset or which line in the Excel.
And there were no guarantees that the data in all systems was consistent.

#### How did you handle logging?

Info only for business events, like an import started or finished. Details on debug, configurable per package.
A correlation ID in the logging context, so you could follow one import or one event through the system.
Full requests and responses only on debug, and on errors only the failing call.

#### What did the business get when something failed?

Every Excel line was validated: required columns, file name, folder, categories, dates. One bad line didn't stop the import.
Files that couldn't be imported went to an error folder on SFTP, not deleted.
After each run, an email with reports, including a non-technical one in German for the business team.
For webhooks, the owner of the asset got an email with the error details.

#### How did you write the REST clients?

I prefer declarative clients: you describe the API as a Java interface, and the framework creates the client.
In this project we used Jakarta REST interfaces with RESTEasy, as a small separate module.
Spring has the same idea with HTTP Interfaces.

#### How did you test it?

Testcontainers started a real MySQL and a real SFTP server for the integration tests.
Docker Compose ran the whole system locally.
Integration tests per layer, against a test Media Pool or with any system mocked.

---

## Сами не поднимайте

- **Версии Spring и Java.** Только «Spring Boot».
- **SOAP.** Если спросят — «we first used SOAP for uploads, and then we moved everything to REST».
- **Детали Excel-колонок и названия CSV-отчётов.**
