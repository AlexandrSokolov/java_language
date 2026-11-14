# Project Experience — Deutsche Bank: Marketing Budget Control in Marketing Planner

*Как пользоваться: сначала выучите цепочку проекта. Рассказ — около 3 минут: введение, этапы, технологии и один-два челленджа. Главные челленджи — C1 (invoice import) и C2 (reliability: dual write без транзакции). Остальные — на уточняющие вопросы. Ответы — на английском, пояснения — по-русски.*

*Суть: маркетинг Deutsche Bank планирует бюджет в Marketing Planner. Плагины добавляют туда финансовый контроль: заказы (estimates), счета (invoices), справочники (поставщики, cost centers, cost types), импорт счетов из финансовой системы и отчёты.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Budget vs. reality → Plugins inside the planner → Stages → Tech → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Budget vs. reality** | План бюджета есть в Planner, а реальные заказы и счета — в финансах. Их нужно связать. | *"For Deutsche Bank, the marketing teams planned their budgets in our Marketing Planner…"* |
| 2 | **Plugins inside the planner** | Набор плагинов: estimates, invoices, master data, CSV import, export, history. | *"We built a set of plugins that run inside the planner…"* |
| 3 | **Stages** | Master data → Estimates → Invoice import → Matching and closing → Reports. | *"It starts with master data…"* |
| 4 | **Tech** | Java EE на WildFly, REST, свой extended REST API, два источника данных, Liquibase, React. | *"The backend was a Java EE application on WildFly…"* |
| 5 | **Challenges** | C1 import и C2 reliability — главные. | *"The hardest part was the invoice import."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| data import · validation · design patterns · batch processing | **C1. Invoice import as a chain of steps** |
| reliability · dual write · transactions · idempotency · retries | **C2. Several REST calls, no transaction** |
| missing API · performance · working around a platform | **C3. When the platform API isn't enough** |
| data migration · database · startup | **C4. Data migration without downtime** |
| reporting · design patterns · business logic | **C5. Reports for finance** |
| security · permissions · multi-language | **C6. Permissions and two languages** |
| testing | **C7. Testing against a mocked platform** |

---

## 2. Введение (около 40 секунд)

### Intro · The business
<details><summary>Show answer</summary>

For Deutsche Bank, the marketing teams planned their budgets in our Marketing Planner, as a tree of plan nodes.
But the real money lived somewhere else: orders to agencies, and invoices from the finance system.
Controllers needed to see, for every plan node, what was ordered, what was already invoiced, and what was left.

</details>

### Intro · The plugins
<details><summary>Show answer</summary>

We built a set of plugins that run inside the planner:
- **estimates**: orders to suppliers, attached to plan nodes;
- **invoices**, linked to those orders;
- **master data**: suppliers, cost centers and cost types;
- an **invoice import** from the finance system;
- **reports** for controlling, and a **change history** for every order.

</details>

---

## 3. Как это работало — этапы (около 60 секунд)

**Цепочка этапов:** Master data → Estimates → Invoice import → Matching and closing → Reports

### Stage 1 · Master data
<details><summary>Show answer</summary>

Administrators maintain suppliers, cost centers and cost types in our own screens.
Every order uses them, so the reports are consistent.

</details>

### Stage 2 · Estimates
<details><summary>Show answer</summary>

A marketing manager creates an order on a plan node: supplier, amount, cost center, cost type.
The order gets a generated order number, which the finance system also uses.

</details>

### Stage 3 · Invoice import
<details><summary>Show answer</summary>

Finance exports the invoices as a CSV file. An administrator uploads it in our plugin.
For every line we find the order by its order number, create the invoice, and link it to the order.

</details>

### Stage 4 · Matching and closing
<details><summary>Show answer</summary>

After linking, we recalculate the rest value of the order: the amount minus all linked invoices.
If less than a thousand euros is left, the order is closed automatically.

</details>

### Stage 5 · Reports
<details><summary>Show answer</summary>

Controllers export an Excel report for a node and its subtree, for a year, a quarter or a month:
only orders, only invoices, or both together, with the invoiced sum, the rest value and the projected value.

</details>

---

## 4. Технологии (около 30 секунд)

**Цепочка:** Backend → Extended API → Data → UI

### Tech 1 · Backend
<details><summary>Show answer</summary>

The backend was a Java EE application on WildFly, with JAX-RS and RESTEasy.
It talked to the Marketing Planner through its REST API, and to the audit service for the history.

</details>

### Tech 2 · Extended API
<details><summary>Show answer</summary>

Where the planner's REST API wasn't enough, I added our own read-only REST API on top of the planner's database.

</details>

### Tech 3 · Data
<details><summary>Show answer</summary>

Two data sources through JPA and Hibernate: the planner's database for reading, and our own database for master data,
with Liquibase for schema changes.
Apache Commons CSV for the import, Apache POI for the Excel reports.

</details>

### Tech 4 · UI
<details><summary>Show answer</summary>

The screens were React plugins embedded in the planner, in English and German.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Invoice import** | CSV from finance → header check → group rows → duplicate check → chain of steps per row → error log back to the user | *Validate everything, stop per row, not per file, and give the user the bad rows back.* |
| **C2. No transaction** | create invoice → link → update order → any call can fail → import journal → idempotent steps → compensation → retries with back-off | *Without a transaction, every step must be repeatable or reversible.* |
| **C3. API not enough** | queries the API didn't support → own read-only REST API on the database → stateless sessions | *If the platform can't answer your question, add a small, read-only door, not a back door.* |
| **C4. Migration** | master data moved to own database → at startup → only once → order number history | *A migration must be safe to run twice.* |
| **C5. Reports** | three report types → strategy pattern → history from audit with fallback | *Put the variation in one place.* |
| **C6. Permissions** | admin-only import → roles from the planner → two languages | *Check permissions on the server, not only in the UI.* |
| **C7. Testing** | planner REST API mocked with WireMock → unit tests for each step | *Test the hard logic without the platform.* |

---

### C1 · Invoice import as a chain of steps

*Главная история проекта.*

### C1 · Situation
<details><summary>Show answer</summary>

Finance sent the invoices as a CSV file, and the data wasn't always clean:
wrong order numbers, invoices that were already imported, the same invoice split over several lines.
One bad line must not break the whole import.

</details>

### C1 · Before the rows
<details><summary>Show answer</summary>

- **Header check.** If the header is wrong, it's the wrong file. We stop right away with a clear message.
- **Grouping.** Lines with the same invoice and the same order were summed into one.
- **Duplicates.** If an invoice with this number already existed in the planner, the line was marked as a duplicate.

</details>

### C1 · Chain of responsibility
<details><summary>Show answer</summary>

Then every row went through a chain of small handlers:
invoice number, invoice date, amount, internal order ID, order number, and finally the order step,
which finds the order, creates the invoice and links it.
If a handler found a problem, it added an error to the row, and the next handlers skipped it.
Each step was small and had its own unit tests.

</details>

### C1 · Errors back to the user
<details><summary>Show answer</summary>

At the end the user got a result: either "everything imported",
or a CSV log with only the failed rows and the reason for each, in the user's language.
They could fix those rows and upload just them again.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

Validate everything, stop per row, not per file, and give the user the bad rows back.

</details>

### C1 · Follow-up: why a chain and not one big method?
<details><summary>Show answer</summary>

Every check is independent and easy to test alone.
When finance added a new rule, I added one handler, and the others didn't change.

</details>

---

### C2 · Several REST calls, no transaction

*Вторая главная история. Поднимайте её сами.*

### C2 · Situation
<details><summary>Show answer</summary>

Importing one invoice was three calls to the planner's REST API:
create the invoice, link it to the order, then update the order, its reference number and maybe close it.
There was no transaction across these calls.
If the link failed, we had an invoice that belonged to no order. A classic dual-write problem.

</details>

### C2 · Import journal
<details><summary>Show answer</summary>

I kept an import journal in our own database: one record per row and per step,
with the status: invoice created, linked, order updated.
The status was written after each successful call.
So after a failure we always knew exactly which step was done and which wasn't.

</details>

### C2 · Idempotent steps
<details><summary>Show answer</summary>

Every step was safe to repeat.
- The invoice number was the idempotency key. Before creating an invoice, we checked if it already existed.
- Linking checked if the link was already there.
- Closing an already closed order did nothing.

So the same file could be uploaded again, and only the missing steps were done.

</details>

### C2 · Compensation
<details><summary>Show answer</summary>

If a row couldn't be completed, for example the order was closed in the meantime,
we compensated: we deleted the invoice we had just created, so no orphan stayed in the planner,
and reported the row.

</details>

### C2 · Retries with back-off
<details><summary>Show answer</summary>

Temporary errors, like timeouts or 5xx responses, were retried with exponential back-off and a limit.
Business errors, like an unknown order number, were not retried. They went straight into the error log for the user.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Without a transaction, every step must be repeatable or reversible.

</details>

### C2 · Follow-up: why not a distributed transaction?
<details><summary>Show answer</summary>

The planner only offered a REST API. There's no transaction you can join over REST.
So the answer is a saga: a sequence of local steps, each with a known state,
and a compensating action when you can't finish.

</details>

---

### C3 · When the platform API isn't enough

### C3 · Situation
<details><summary>Show answer</summary>

The planner's REST API couldn't answer some of our questions:
find invoices by number or by year, find orders by order number across the whole tree.
Loading everything and filtering in memory was far too slow.

</details>

### C3 · Action
<details><summary>Show answer</summary>

I added a small, read-only REST API, deployed next to the planner, that queried its database directly.
Read-only on purpose: all writes still went through the official API, so we never bypassed its business rules.
For large reads I used stateless Hibernate sessions and cursors, so we didn't load everything into memory.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

If the platform can't answer your question, add a small, read-only door, not a back door.

</details>

---

### C4 · Data migration without downtime

### C4 · Situation and action
<details><summary>Show answer</summary>

Master data first lived in the planner's database. We moved it into our own database, so we could own its structure.
The migration ran at startup:
- it copied suppliers, cost centers and cost types;
- it converted the history of generated order numbers into our own format;
- and it ran only once: a flag in the configuration table marked it as finished.

If one part failed, the flag wasn't set, the error was logged, and the next start tried again.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

A migration must be safe to run twice.

</details>

---

### C5 · Reports for finance

### C5 · Action
<details><summary>Show answer</summary>

There were three kinds of reports: only orders, only invoices, and both together.
I used the strategy pattern: one strategy per report type, and the rest of the export was shared:
walking the node tree, filtering by year, quarter or month, and writing the Excel file.

For every order we calculated the invoiced sum, the rest value and the projected value.
The "created by" and "last changed by" came from the audit service,
with a fallback to the order's own data when the audit log was incomplete.

</details>

### C5 · Lesson
<details><summary>Show answer</summary>

Put the variation in one place.

</details>

---

### C6 · Permissions and two languages

### C6 · Action
<details><summary>Show answer</summary>

- **Permissions on the server.** Only administrators could import invoices.
  The other actions used the user's roles and groups from the planner. Every check was done on the server, not only in the UI.
- **Two languages.** All messages, error logs and report headers were in English or German, based on the user's locale.

</details>

### C6 · Lesson
<details><summary>Show answer</summary>

Check permissions on the server, not only in the UI.

</details>

---

### C7 · Testing against a mocked platform

### C7 · Action
<details><summary>Show answer</summary>

The hard logic was in the import chain, the export strategies and the history.
I mocked the planner's REST API with WireMock, so these tests ran without a planner,
and every handler of the chain had its own unit tests.

</details>

### C7 · Lesson
<details><summary>Show answer</summary>

Test the hard logic without the platform.

</details>

---

## 6. Сами не поднимайте

- **Версии Java, WildFly, React.** Только «Java EE on WildFly», «React».
- **Порог закрытия заказа в 1000 евро.** Говорите «below a configured threshold», если спросят.
- **Прямой доступ к базе планировщика.** Только как «read-only REST API next to the planner»; запись — только через официальный API.
