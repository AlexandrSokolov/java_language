# Topic 3 — My Technology Stack & My Strengths

*Как пользоваться: стек — короткий ответ по областям. Уровни — для вас: честные формулировки по слабым местам. Сильные стороны — пять звеньев цепочкой, у каждого доказательство. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Цепочки

Сначала запомните цепочки. Если сбились: вспомните следующее ключевое слово, скажите его первую фразу, и рассказ продолжится.

### "What's your tech stack?" (about 40 seconds)

**Цепочка:** Core → APIs → Messaging → Data → Testing → Build → Before Spring

| # | Ключ | Что называете | Первая фраза |
|---|---|---|---|
| 1 | **Core** | Java и Spring: Spring Boot, Spring Data JPA с Hibernate. Java 21, потом 24. | *"My core is Java and Spring…"* |
| 2 | **APIs** | REST (JAX-RS, RESTEasy), SOAP (JAX-WS). | *"For APIs…"* |
| 3 | **Messaging** | JMS, Kafka, webhooks, Server-Sent Events. | *"For messaging…"* |
| 4 | **Data** | MySQL и SQL Server через JPA, Liquibase. | *"Databases…"* |
| 5 | **Testing** | JUnit 5, Mockito, Testcontainers. | *"For testing…"* |
| 6 | **Build** | Maven, Jenkins, Nexus, GitLab, Docker, Docker Compose, Ansible. | *"For build and environment…"* |
| 7 | **Before Spring** | Годы Java EE на WildFly. | *"And before Spring…"* |

### My strengths

**Цепочка:** Root cause → Prevention → Business sense → Reusable tools → Clean modern Java

| # | Ключ | Сильная сторона и доказательство | Мостик к следующему звену |
|---|---|---|---|
| 1 | **Root cause** | Нахожу и доказываю причину, через несколько систем. Показываю другой команде их ошибку в их логах. | *"Finding causes taught me to prevent them…"* |
| 2 | **Prevention** | Строю системы, которые легко поддерживать. Проверка контракта атрибутов; список улучшений из бюджета поддержки. | *"…and good prevention starts with understanding what the business really needs."* |
| 3 | **Business sense** | Нахожу реальную потребность за запросом. SOAP-вызов на гигабайт → инкрементальные обновления; явный шаг «processed». | *"When a problem comes back, I build a tool for the whole team…"* |
| 4 | **Reusable tools** | Увидел проблему дважды — делаю инструмент. Обёртка над RESTEasy, библиотека типизированной конфигурации. | *"…and underneath all of it is clean, modern Java."* |
| 5 | **Clean modern Java** | Records, immutable data, pattern matching, Java 24. | — |

### "Why did your company rate you so highly?"

**Цепочка ответа:** Find and prove → Stable → Business side

**Цепочка доказательств:** Reference letter → Several managers → Senior from the start → Kept until the end

| # | Ключ | О чём звено |
|---|---|---|
| 1 | **Find and prove** | Находил и доказывал причину проблем, которые шли через несколько систем, и ясно их описывал. |
| 2 | **Stable** | Интеграции стабильные и простые в поддержке; менеджер видел, что им нужно меньше поддержки. |
| 3 | **Business side** | Понимал бизнес: объяснял клиентам и проектным менеджерам, что возможно, предлагал лучшие варианты. |

---

## 2. Tech stack — cards

### Stack 1 · Core
<details><summary>Show answer</summary>

My core is Java and Spring: Spring Boot and Spring Data JPA with Hibernate, on Java 21 and later 24.

</details>

### Stack 2 · APIs
<details><summary>Show answer</summary>

For APIs, REST with JAX-RS and RESTEasy, and SOAP with JAX-WS.

</details>

### Stack 3 · Messaging
<details><summary>Show answer</summary>

For messaging, JMS and Kafka, webhooks, and Server-Sent Events.

</details>

### Stack 4 · Data
<details><summary>Show answer</summary>

Databases: MySQL and SQL Server through JPA, with Liquibase for schema changes.

</details>

### Stack 5 · Testing
<details><summary>Show answer</summary>

For testing, JUnit 5, Mockito and Testcontainers.

</details>

### Stack 6 · Build
<details><summary>Show answer</summary>

For build and environment: Maven, Jenkins, Nexus, GitLab, Docker, Docker Compose and Ansible.

</details>

### Stack 7 · Before Spring
<details><summary>Show answer</summary>

And before Spring, I worked for years with Java EE on WildFly.

</details>

---

## 3. Уровни и честные формулировки

### Уровни (для вас)

| Уровень | Технологии |
|---|---|
| **1 — глубоко** | Java · Spring Boot · Spring Data JPA и Hibernate · JAX-RS и RESTEasy · SOAP и JAX-WS · webhooks и REST · JUnit 5, Mockito, Testcontainers · Docker и Docker Compose · Ansible · Jenkins, Maven, Nexus, GitLab · Liquibase |
| **2 — уверенно** | Jakarta EE, CDI, WildFly · JMS и Kafka (как consumer) · Server-Sent Events · MySQL и SQL Server (в основном через ORM) · OAuth2 и JWT (как клиент) |
| **3 — знаком** | JasperReports · JavaScript · React |

### Если спросят про уровень 2 или 3

### How much Kafka experience do you have?
<details><summary>Show answer</summary>

I worked with Kafka as a consumer: subscribing to platform events and processing them reliably.
Topics and the cluster were owned by the platform team.

</details>

### Have you worked with OAuth2?
<details><summary>Show answer</summary>

I integrated with OAuth2-protected APIs as a client, for example to read data from Google BigQuery.
I haven't built an authorization server myself.

</details>

### How good is your SQL?
<details><summary>Show answer</summary>

Day to day through JPA and Hibernate. Plain SQL for investigations and support.

</details>

### Have you used Server-Sent Events?
<details><summary>Show answer</summary>

I used them in two projects, as a lighter alternative to WebSockets for pushing updates to the client.
At that time they weren't widely used yet.

</details>

### Do you know JasperReports?
<details><summary>Show answer</summary>

I built a complex report with nested tables and images, under a tight deadline.
The whole project took about a month, and the first two weeks were very intensive learning.
I haven't used it since, so I'd need a refresh.

</details>

### Do you do frontend work?
<details><summary>Show answer</summary>

JavaScript and React at maintenance level. I can fix and extend things, but I'm a backend engineer.

</details>

*Не заявляйте: virtual threads в продакшне, настройку Kafka-кластера, серверную часть OAuth2, Flyway (вы работали с Liquibase).*

---

## 4. My strengths — cards

### S1 · Root cause
<details><summary>Show answer</summary>

When an integration failed, many systems were involved, and each team looked only at its own code.
I followed the logic from start to end, across modules, and found where it really broke.
Often it was a bug in the product. And I didn't just say "it doesn't work".
I showed the other team their own error in their logs, right before mine, and described it clearly.
That's what got problems fixed.

</details>

### S2a · Prevention: the contract check
<details><summary>Show answer</summary>

Job attributes in our product were identified by technical names,
and those names could change in the configuration.
Other teams hard-coded them. When a name changed, their component silently broke,
and finding out why took a long time.
I validated the contract before processing: every attribute I need exists and has the right type,
and the incoming values are there.
If not, the job got a clear message saying which attribute was missing or wrong,
so the person configuring it could fix it alone.
It made the first delivery a bit slower, but it saved a lot of time later.

</details>

### S2b · Prevention: the robustness list
<details><summary>Show answer</summary>

And for every project I kept a list of robustness improvements: validations, clearer feedback.
I added them step by step from the regular maintenance budget, never as a separate paid extra.

</details>

### S3a · Business sense: the gigabyte SOAP call
<details><summary>Show answer</summary>

Once a project manager wanted to give a customer a SOAP call that returned almost the whole database:
30 to 40 minutes, around a gigabyte.
I pushed back and proposed regular incremental updates with only the fields the customer actually needed.

</details>

### S3b · Business sense: the "processed" step
<details><summary>Show answer</summary>

Another example: whether we had processed a job was hidden in flags.
I introduced an explicit "processed" step in the workflow.
The state became visible to everyone, reconciliation became simple,
and the customer only saw jobs that were ready for the next step.

</details>

*Запасной пример:* отдельная техническая дискуссия в каждой job, чтобы бизнес-пользователи не путались в технических деталях, а разработчики сразу их находили.

### S4a · Reusable tools: the RESTEasy wrapper
<details><summary>Show answer</summary>

I built a thin wrapper around our RESTEasy client:
you could switch on readable JSON logging of requests and responses for just one package.
When a product release broke an API, I could send the product team the exact raw request in minutes.

</details>

### S4b · Reusable tools: the configuration library
<details><summary>Show answer</summary>

And before Spring, I wrote a small typed-configuration library for our Java EE services:
you declare a Java interface, and the library loads and validates the configuration.
Both were shared through Nexus and reused across projects.

</details>

### S5 · Clean modern Java
<details><summary>Show answer</summary>

I use records for all DTOs. Our REST and webhook payloads were all records,
and I use immutable data wherever I can.
I used pattern matching to choose the right handler based on the job's status in our event processing.
And we moved our projects up to Java 24.

</details>

---

## 5. "Why did your company rate you so highly?" — cards

### Why did your company rate you so highly?
<details><summary>Show answer</summary>

I think there are three reasons.
First, I could find and prove the cause of problems that crossed several systems,
not only in my code but in other modules, and describe them clearly.
Second, my integrations were stable and easy to maintain.
My manager saw that my solutions needed less support than others.
Third, I understood the business side: I could explain to customers and project managers what was possible,
and propose better alternatives.

</details>

### Proof · Reference letter
<details><summary>Show answer</summary>

My reference says my expertise is excellent and exceptionally in-depth,
and that I work at the highest professional level. It has the top grade.

</details>

*Немецкие интервьюеры знают код: «stets zu unserer vollsten Zufriedenheit» — это высшая оценка.*

### Proof · Several managers
<details><summary>Show answer</summary>

I have references from several managers over the years, all with the same top marks.

</details>

### Proof · Senior from the start
<details><summary>Show answer</summary>

From my first year in Germany, management saw me as a senior developer.

</details>

### Proof · Kept until the end
<details><summary>Show answer</summary>

When the teams were closed, I was the last developer they kept.

</details>

---

## 6. Likely questions

### What's your strongest technical skill?
<details><summary>Show answer</summary>

Finding the real cause of a problem that crosses several systems, and proving it with evidence.

</details>

### What makes you different from other seniors?
<details><summary>Show answer</summary>

I always think about the person who will support the system after me,
and about the business user who will see the error.
I build for them, not only for the happy path.

</details>

### What would your manager say about you?
<details><summary>Show answer</summary>

That my solutions were stable and easy to maintain,
and that I was the person to call when nobody knew why something broke.

</details>

### How good is your Java, from 1 to 10?
<details><summary>Show answer</summary>

I'd say eight. I've used it every day for many years, but there's always more to learn.

</details>

*Никогда не говорите 10.*

### What did you use from newer Java versions?
<details><summary>Show answer</summary>

Records for all DTOs and immutable data in general.
Pattern matching, for example to choose a handler based on a job's status. Text blocks.
And we kept our projects on current Java, up to version 24.

</details>

### Have you used virtual threads?
<details><summary>Show answer</summary>

Not in production yet, but I've studied them well.
They are lightweight threads managed by the JVM, so you can run thousands of blocking tasks,
like calls to external REST APIs, without a big thread pool.
They're very easy to switch on. In Spring Boot it's one property.
But they only help with I/O-bound work, not with CPU-heavy work.
And since Java 24, `synchronized` no longer pins a virtual thread to its carrier thread,
which removed one of the main pitfalls.

</details>

### What's the difference between JMS and Kafka?
<details><summary>Show answer</summary>

JMS is a classic message broker: a message goes into a queue, a consumer takes it, and it's gone.
Kafka is a distributed log: messages stay for a retention period, and each consumer group keeps its own position,
its offset.
So you can replay events, and partitions give you scale with ordering inside each partition.
In our case, Kafka also let us subscribe from our own containers, independently of the product's server.

</details>

---

## 7. Не говорить

- **Цифры зарплаты.** Ни прошлую, ни текущую. Названная цифра становится якорем: компания вроде Databricks может предложить меньше, чем готова заплатить. Рост зарплаты за 10 лет — аргумент для вас, не для интервью.
- **Что вы «используете все новые возможности Java».** Говорите только о том, что использовали, с конкретным примером.
