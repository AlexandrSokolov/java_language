# Project Experience — Porsche: Marketing Finance Components (Invoices, Budget Transfers, Dealer KPIs)

*Как пользоваться: у Porsche было несколько независимых компонентов, поэтому здесь не один процесс, а 2 главные истории и запасные. Рассказ — около 3 минут: введение, два главных компонента, технологии и один челлендж. Главный челлендж — C1 (webhooks, которые сделали неправильно, и как вы их переделали). Ответы — на английском, пояснения — по-русски.*

*Суть: маркетинг Porsche (headquarters и регионы — Central & Eastern Europe, Latin America, Middle East) ведёт бюджеты в Marketing Planner и workflow в Job Manager. Компоненты связывают счета, бюджеты и KPI дилеров.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Several components → Pending amounts → Budget transfers → Modernisation → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Several components** | Не один продукт, а несколько независимых компонентов для одного клиента. | *"For Porsche I owned several independent components around marketing finance."* |
| 2 | **Pending amounts** | Счёт в job → «pending» сумма на узлах плана → после approval — счёт в планировщике. | *"The most important one synchronised invoices…"* |
| 3 | **Budget transfers** | Перенос бюджета между узлами с approval по email, валютами и распределением по месяцам. | *"The second one was budget transfer requests…"* |
| 4 | **Modernisation** | Старые компоненты на Java 8 / WildFly → Spring Boot, Java 21, Docker. | *"And I moved these components to a modern stack…"* |
| 5 | **Challenges** | C1 webhooks — главный. | *"The interesting part: the webhooks were implemented wrong."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| webhooks · event-driven · concurrency · race conditions · taking over code | **C1. Webhooks done wrong, and how I fixed them** |
| reliability · dual write · saga · distributed transactions | **C2. Budget transfer: two nodes, no transaction** |
| business logic · money · rounding · currencies | **C3. Distributing money correctly** |
| legacy · modernisation · Docker · Spring Boot | **C4. From scripts and WildFly to one Spring Boot service** |
| data import · scheduled jobs · external systems | **C5. Dealer KPIs from SAP** |

---

## 2. Введение (около 40 секунд)

### Intro · Several components
<details><summary>Show answer</summary>

For Porsche I owned several independent components around marketing finance.
Their marketing teams, at the headquarters and in regions like Eastern Europe, Latin America and the Middle East,
planned budgets in our Marketing Planner, and ran their approvals as jobs in Job Manager.

</details>

### Intro · The main components
<details><summary>Show answer</summary>

The two most important ones:
- **Pending amounts sync**: when someone submits an invoice in a job, the planner immediately shows it as a pending amount,
  and after approval it becomes a real invoice in the plan.
- **Budget transfer requests**: moving budget from one plan node to another, with an approval chain by email.

And some smaller ones: an import of dealer campaign KPIs from SAP, a compressor for image attachments,
and a custom context menu in the planner.

</details>

---

## 3. Компонент 1 — Pending amounts sync (около 60 секунд)

**Цепочка:** Submit → Pending → Approve → Invoice → Reject

### PAS · Submit
<details><summary>Show answer</summary>

A user creates an invoice job, links it to a plan node and submits it.
Job Manager sends a webhook. We add the amount to the pending amount on the node and on all its parents up the tree,
in the node's currency.

</details>

### PAS · Approve
<details><summary>Show answer</summary>

When the invoice is approved, we create the real invoice in the planner,
and take the amount out of the pending sum.

</details>

### PAS · Reject or cancel
<details><summary>Show answer</summary>

If the job is rejected or cancelled, we take the pending amount back.
So at any time, controllers see the real budget, the invoiced amount, and what's still waiting for approval.

</details>

### PAS · Feedback
<details><summary>Show answer</summary>

Every step leaves a comment in the job: what was done, or why it failed.
If creating the invoice fails, the job goes back to the first step, so it's visible and can be submitted again.

</details>

---

## 4. Компонент 2 — Budget transfer requests (около 45 секунд)

### BTR · The flow
<details><summary>Show answer</summary>

A user asks to move budget from a source node to a recipient node.
- The request goes through an approval chain. Each approver gets an email with approve and reject buttons.
- On final approval we execute the transfer: we convert the amount into each node's local currency,
  take it from the source and add it to the recipient: yearly, per month and per quarter, and on the parent nodes.
- Every party gets an email with the result.

There was also a second variant of the same flow for customer goodwill budgets.

</details>

---

## 5. Технологии (около 30 секунд)

### Tech · Stack
<details><summary>Show answer</summary>

- Spring Boot services on Java 21, running in Docker, configured from outside the container.
- REST clients for Job Manager, Marketing Planner and the spending module, with OAuth2 tokens.
- Webhooks from the platform's event bus.
- Thymeleaf for the approval emails and pages.
- WireMock for the integration tests, and a central observability platform for the logs.

</details>

---

## 6. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Webhooks** | old: synchronous, errors swallowed → acknowledge fast → filter early → one queue, one consumer → inbox → comments and a failure step → retry endpoint → graceful shutdown | *Acknowledge fast, process in order, and make every failure visible where the user works.* |
| **C2. Budget transfer** | source and recipient, no transaction → operation ID → journal → steps in order → compensation | *Without a transaction, every step must be repeatable or reversible.* |
| **C3. Money** | currency conversion → distribute by month capacity → leftovers → quarters and parents | *With money, the sum must always match, to the cent.* |
| **C4. Modernisation** | three scripts → one service → Java 8 and WildFly → Spring Boot in Docker | *Merge what does the same thing, and make it boring to deploy.* |
| **C5. Dealer KPIs** | nightly import from SAP → match dealers → create or update KPIs → retries | *An import must be safe to run every night.* |

---

### C1 · Webhooks done wrong, and how I fixed them

*Главная история. Это тот самый случай: другой разработчик сделал webhooks неправильно, вы переделали.*

### C1 · Situation
<details><summary>Show answer</summary>

The first version was written by another team, and there were three separate scripts for three regions.
The invoice sync was a synchronous REST call: all the work happened inside the request.
Errors were caught and written to the log at info level, and the caller just got a 500.
So invoices were sometimes missing in the plan, and nobody knew why.

</details>

### C1 · Acknowledge fast, filter early
<details><summary>Show answer</summary>

I rebuilt it as one service that receives the platform's webhooks.
The endpoint only checks the event and puts it in a queue, and answers right away.
We filter early: only our job types, and only the transitions we care about.
Everything else is ignored, with a debug line saying why.

</details>

### C1 · One queue, one consumer
<details><summary>Show answer</summary>

This was the key decision.
To update a pending amount, we read the current value on a node and add to it.
Two events for nodes under the same parent, processed in parallel, would both read the same old value,
and one update would be lost. A classic race condition.
So all events went into one bounded queue, with a single consumer, processing them strictly one after another.

</details>

### C1 · Inbox: no event processed twice
<details><summary>Show answer</summary>

Webhooks can come twice. Every event had a UUID, and I stored it in an inbox table with a unique constraint.
A duplicate was skipped.
And on top of that, before creating an invoice, we checked if this job already had its invoice comment on the node.
So even a replayed event couldn't create a second invoice.

</details>

### C1 · Failures visible in the job
<details><summary>Show answer</summary>

Every transition left a comment in the job: done, or failed and why.
If the approval step failed, the job was moved back to the first step.
That step was our indicator: the user and support could see it immediately, fix it and submit again.

</details>

### C1 · Retries and replay
<details><summary>Show answer</summary>

Temporary errors, like timeouts or 5xx, were retried with exponential back-off.
And if our service wasn't running during a deployment, the events from that time were lost.
So I added a REST endpoint to process a job again on demand,
and a reconciliation job that found jobs whose state in Job Manager didn't match the planner.

</details>

### C1 · Graceful shutdown
<details><summary>Show answer</summary>

On shutdown, the service stopped accepting new events, let the current one finish,
and the events still in the queue were recovered by the reconciliation after the restart.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Acknowledge fast, process in order, and make every failure visible where the user works.

</details>

### C1 · Follow-up: doesn't one consumer make it slow?
<details><summary>Show answer</summary>

Not here. It's a few hundred invoices a day, and each one takes a second or two.
Correct sums matter much more than speed.
If the volume grew, I would partition by the top of the tree, so independent branches run in parallel,
and events in the same branch stay in order. Like partitions in Kafka.

</details>

### C1 · Follow-up: why not lock the node instead?
<details><summary>Show answer</summary>

The value lives in the planner, not in our database. We can't lock it there.
And the update goes up the whole tree, so you'd need to lock all parents.
A single ordered queue is simpler and impossible to get wrong.

</details>

---

### C2 · Budget transfer: two nodes, no transaction

### C2 · Situation
<details><summary>Show answer</summary>

A transfer changes two nodes: take from the source, add to the recipient,
and update months, quarters, the year and the parent nodes on both sides.
That's many REST calls, and no transaction across them.
If it stops in the middle, money disappears from one node and never arrives at the other.

</details>

### C2 · Action
<details><summary>Show answer</summary>

- **Operation ID.** Every transfer got its own ID. It was in every log line and every call, so one transfer could be traced end to end.
- **Journal.** I stored each step of the transfer with its status: source updated, recipient updated, parents updated.
- **Idempotent steps.** A step that was already done was skipped on a retry.
- **Compensation.** If the recipient couldn't be updated, we reversed the change on the source and informed the requester and the approvers.
- **Validation first.** Before executing, we checked that the source had enough available budget in its currency.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Without a transaction, every step must be repeatable or reversible. That's a saga.

</details>

---

### C3 · Distributing money correctly

### C3 · Action
<details><summary>Show answer</summary>

- The amount was converted into the local currency of each node.
- It was distributed over the remaining months of the year, based on each month's available capacity.
- What couldn't fit into one month moved to the next.
- Rounding leftovers were redistributed, so the sum of the months always matched the total exactly.
- Quarters and yearly values were then derived from the months.

This logic had its own unit tests for every edge case.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

With money, the sum must always match, to the cent.

</details>

---

### C4 · From scripts and WildFly to one Spring Boot service

### C4 · Action
<details><summary>Show answer</summary>

- Three separate scripts for three regions became one configurable service.
- The old components on Java 8 and WildFly moved to Spring Boot on Java 21, in Docker.
- Configuration came from a file mounted by the operations team, so the same image ran on test and production.
- Logs went to a central observability platform.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

Merge what does the same thing, and make it boring to deploy.

</details>

---

### C5 · Dealer KPIs from SAP

### C5 · Action
<details><summary>Show answer</summary>

Every night we read dealer campaign results from an SAP database: target group, participants, leads, orders and test drives.
We matched each record to its plan node by the dealer ID, and created or updated the KPIs on that node.
Calls to the planner were retried with back-off, and the import was safe to run again.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

An import must be safe to run every night.

</details>

---

## 7. Запасное: маленькие компоненты

### Extra · Attachment compressor and context menu
<details><summary>Show answer</summary>

- An image compressor that reduced the size of attachments on plan nodes, so the planner stayed fast.
- A custom context menu in the planner, with a custom form for a new job type.

</details>

---

## 8. Сами не поднимайте

- **Сокращения Porsche:** PCEE, PLA, PME, CAP, MBI. Говорите «three regions», «the platform's event bus».
- **Что часть компонентов писала внешняя команда.** Говорите «the first version was written by another team».
- **Версии Java в старых компонентах.**
