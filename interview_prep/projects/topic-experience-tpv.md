# Project Experience — TPV: Purchase Requests and Orders from SAP into Marketing Planner

*Как пользоваться: проект небольшой — это хороший короткий пример «импорт из SAP + валюты». Рассказ — около 2 минут. Главный челлендж — C1 (ежедневный импорт, безопасный для повторного запуска). Ответы — на английском, пояснения — по-русски.*

*Суть: TPV — производитель телевизоров и мониторов под брендом Philips. Маркетинговые фонды для партнёров-ритейлеров (MDF, Market Development Funds) планируются в Marketing Planner, а заявки на закупку (purchase requests) и заказы (purchase orders) живут в SAP. Компонент каждый день переносит их из SAP в план, в одной валюте.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочка проекта

**Цепочка:** Marketing funds → SAP exports on SFTP → Import → Currency → Challenges

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Marketing funds** | Бюджеты для партнёров в планировщике, реальные закупки — в SAP. | *"For TPV, the company behind Philips TVs and monitors…"* |
| 2 | **SAP exports** | SAP каждый день кладёт CSV на SFTP: заявки и отчёт по заказам. | *"Every day SAP exported two files…"* |
| 3 | **Import** | Каждая заявка становится заказом на нужном узле плана. | *"For every line we find the plan node…"* |
| 4 | **Currency** | Все суммы в долларах, по курсу месяца. | *"All amounts had to be in US dollars…"* |
| 5 | **Challenges** | C1 — безопасный повторный импорт. | *"The main challenge: the import had to be safe to run again."* |

### Какой челлендж на какой вопрос

| Если спрашивают про… | Челлендж |
|---|---|
| data import · idempotency · batch jobs · SAP | **C1. A daily import that is safe to run again** |
| money · currencies · business rules | **C2. Currency conversion by month** |
| performance · large result sets · APIs | **C3. Searching through large result sets** |
| configuration · reuse | **C4. Configurable mapping and a shared foundation** |

---

## 2. Введение (около 30 секунд)

### Intro · The business
<details><summary>Show answer</summary>

For TPV, the company behind Philips TVs and monitors, the marketing teams planned funds for their retail partners
in our Marketing Planner. But the actual purchase requests and purchase orders were created in SAP.
Controllers needed to see them in the plan, next to the budget, in one currency.

</details>

---

## 3. Как это работало — этапы (около 45 секунд)

**Цепочка этапов:** Files → Validation → Node → Order → Dimension → PO details

### Stage 1 · Files
<details><summary>Show answer</summary>

Every day SAP exported two CSV files to SFTP: the list of purchase requests and the purchase order report.
A monthly exchange rate file was also there.

</details>

### Stage 2 · Validation and mapping
<details><summary>Show answer</summary>

For every line we checked the required columns, the status, open or closed,
and that we had an exchange rate for its currency.
Then we found the plan node by its internal order number, which was stored on the node as an external reference.

</details>

### Stage 3 · Order and dimension
<details><summary>Show answer</summary>

On that node we created or updated an order for the purchase request, with the amount in US dollars,
and set the G/L account as a dimension on the node.

</details>

### Stage 4 · PO details
<details><summary>Show answer</summary>

The purchase order report added the details from SAP to the existing orders:
the text, the accrual to be booked, and the total PO value.

</details>

---

## 4. Технологии (около 20 секунд)

### Tech · Stack
<details><summary>Show answer</summary>

A Java EE application on WildFly, with the same module structure we used for other customers:
authentication, a REST client, configuration, CSV handling, the planner client and the REST API.
SFTP for the files, the planner's REST API for nodes, orders and dimensions.
Integration tests ran against a test SFTP server with real, anonymised SAP files.

</details>

---

## 5. Челленджи

| Челлендж | Цепочка | Вывод (Lesson) |
|---|---|---|
| **C1. Safe to run again** | upsert by PR number → processed files table → archive → idempotent PO details → one bad line doesn't stop the file → report | *A daily import will run twice one day. Make sure that's boring.* |
| **C2. Currency** | monthly rates → rate by the month of the required date → no rate, no import → report | *Convert with the right rate for the right month, or don't convert at all.* |
| **C3. Large result sets** | paged API → lazy stream over pages → stop at the first match | *Don't load what you don't need.* |
| **C4. Configuration** | column mapping in configuration → shared modules | *Keep the customer's format out of the code.* |

---

### C1 · A daily import that is safe to run again

### C1 · Situation
<details><summary>Show answer</summary>

The import ran every day, and SAP sometimes sent the same file again, or an updated file for the same day.
And the operations team sometimes triggered it manually after a problem.
A second run must never create duplicate orders or duplicate comments.

</details>

### C1 · Upsert by PR number
<details><summary>Show answer</summary>

Every order was an upsert. On the node, we looked for an order with the same purchase request number:
found, we updated it; not found, we created it.

</details>

### C1 · Processed files
<details><summary>Show answer</summary>

I kept a table of processed files: the file name, its checksum and when it was imported.
The same file with the same content was skipped. A new version of the file was imported again,
which was safe because of the upserts.
After a successful import the file was moved to an archive folder on SFTP.

</details>

### C1 · Idempotent PO details
<details><summary>Show answer</summary>

The PO details were appended to the order's notes. Appending is not idempotent by nature.
So every block of details carried the date of the report it came from,
and before appending we checked if a block for that report was already there.

</details>

### C1 · Errors per line
<details><summary>Show answer</summary>

One bad line didn't stop the file. Lines without a node, without a rate or with missing required columns were skipped and collected,
and after each run the responsible people got an email with the skipped lines and the reason for each.

</details>

### C1 · Lesson
<details><summary>Show answer</summary>

A daily import will run twice one day. Make sure that's boring.

</details>

---

### C2 · Currency conversion by month

### C2 · Action
<details><summary>Show answer</summary>

All amounts in the plan had to be in US dollars.
The rate file had a rate per currency and per month.
We converted every amount with the rate for the month of its required date, not today's rate,
so the numbers in the plan matched finance.
If there was no rate for a currency or a month, the line was not imported, and it was reported.
A wrong conversion is worse than a missing line.

</details>

### C2 · Lesson
<details><summary>Show answer</summary>

Convert with the right rate for the right month, or don't convert at all.

</details>

---

### C3 · Searching through large result sets

### C3 · Action
<details><summary>Show answer</summary>

The planner's search API returned results page by page.
I wrapped it in a lazy stream: the next page was loaded only when the stream needed it,
and the search stopped at the first match.
So finding one order didn't mean loading thousands.

</details>

### C3 · Lesson
<details><summary>Show answer</summary>

Don't load what you don't need.

</details>

---

### C4 · Configurable mapping and a shared foundation

### C4 · Action
<details><summary>Show answer</summary>

The mapping from SAP columns to order fields lived in the configuration, not in the code.
When SAP renamed a column or a field stopped being required, it was a configuration change, not a release.
The project was built on the same module structure as our other integrations, so it was quick to start and easy to support.

</details>

### C4 · Lesson
<details><summary>Show answer</summary>

Keep the customer's format out of the code.

</details>

---

## 6. Сами не поднимайте

- **Названия файлов и колонок SAP.**
- **Что это маленький проект.** Подавайте его как «a focused integration», если нужен короткий пример.
