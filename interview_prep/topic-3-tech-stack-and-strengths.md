# Topic 3 — My Technology Stack & My Strengths

*Как пользоваться: раздел 1 — короткий ответ про стек. Раздел 2 — для вас: уровни и честные формулировки по слабым местам. Раздел 3 — пять сильных сторон цепочкой, у каждой доказательство. Ответы — на английском, пояснения — по-русски.*

---

## 1. "What's your tech stack?" (about 40 seconds)

> My core is Java and Spring: Spring Boot and Spring Data JPA with Hibernate, on Java 21 and later 24. For APIs, REST with JAX-RS and RESTEasy, and SOAP with JAX-WS. For messaging, JMS and Kafka, webhooks, and Server-Sent Events. Databases: MySQL and SQL Server through JPA, with Liquibase for schema changes. For testing, JUnit 5, Mockito and Testcontainers. For build and environment: Maven, Jenkins, Nexus, GitLab, Docker, Docker Compose and Ansible. And before Spring, I worked for years with Java EE on WildFly.

---

## 2. Уровни и честные формулировки (для вас)

| Уровень | Технологии |
|---|---|
| **1 — глубоко** | Java · Spring Boot · Spring Data JPA и Hibernate · JAX-RS и RESTEasy · SOAP и JAX-WS · webhooks и REST · JUnit 5, Mockito, Testcontainers · Docker и Docker Compose · Ansible · Jenkins, Maven, Nexus, GitLab · Liquibase |
| **2 — уверенно** | Jakarta EE, CDI, WildFly · JMS и Kafka (как consumer) · Server-Sent Events · MySQL и SQL Server (в основном через ORM) · OAuth2 и JWT (как клиент) |
| **3 — знаком** | JasperReports · JavaScript · React |

**Если спросят про уровень 2 или 3:**

- **Kafka:**
  > I worked with Kafka as a consumer: subscribing to platform events and processing them reliably. Topics and the cluster were owned by the platform team.
- **OAuth2:**
  > I integrated with OAuth2-protected APIs as a client, for example to read data from Google BigQuery. I haven't built an authorization server myself.
- **SQL:**
  > Day to day through JPA and Hibernate. Plain SQL for investigations and support.
- **Server-Sent Events:**
  > I used them in two projects, as a lighter alternative to WebSockets for pushing updates to the client. At that time they weren't widely used yet.
- **JasperReports:**
  > I built a complex report with nested tables and images in about two weeks, under a tight deadline. I haven't used it since, so I'd need a refresh.
- **Frontend:**
  > JavaScript and React at maintenance level. I can fix and extend things, but I'm a backend engineer.

*Не заявляйте: virtual threads в продакшне, настройку Kafka-кластера, серверную часть OAuth2, Flyway (вы работали с Liquibase).*

---

## 3. My strengths — the chain

**Цепочка:** Root cause → Prevention → Business sense → Reusable tools → Clean modern Java

**Связки между звеньями:**
- *"Finding causes taught me to prevent them…"*
- *"…and good prevention starts with understanding what the business really needs."*
- *"When a problem comes back, I build a tool for the whole team…"*
- *"…and underneath all of it is clean, modern Java."*

### 1. Root cause: I find and prove the cause, across systems

> When an integration failed, many systems were involved, and each team looked only at its own code. I followed the logic from start to end, across modules, and found where it really broke. Often it was a bug in the product. And I didn't just say "it doesn't work". I showed the other team their own error in their logs, right before mine, and described it clearly. That's what got problems fixed.

### 2. Prevention: I build systems that are easy to support

> Job attributes in our product were identified by technical names, and those names could change in the configuration. Other teams hard-coded them. When a name changed, their component silently broke, and finding out why took a long time. I validated the contract before processing: every attribute I need exists and has the right type, and the incoming values are there. If not, the job got a clear message saying which attribute was missing or wrong, so the person configuring it could fix it alone. It made the first delivery a bit slower, but it saved a lot of time later.
>
> And for every project I kept a list of robustness improvements: validations, clearer feedback. I added them step by step from the regular maintenance budget, never as a separate paid extra.

### 3. Business sense: I find the real need behind a request

> Once a project manager wanted to give a customer a SOAP call that returned almost the whole database: 30 to 40 minutes, around a gigabyte. I pushed back and proposed regular incremental updates with only the fields the customer actually needed.
>
> Another example: whether we had processed a job was hidden in flags. I introduced an explicit "processed" step in the workflow. The state became visible to everyone, reconciliation became simple, and the customer only saw jobs that were ready for the next step.

*Запасной пример:* отдельная техническая дискуссия в каждой job, чтобы бизнес-пользователи не путались в технических деталях, а разработчики сразу их находили.

### 4. Reusable tools: when I see the same problem twice, I build a tool

> I built a thin wrapper around our RESTEasy client: you could switch on readable JSON logging of requests and responses for just one package. When a product release broke an API, I could send the product team the exact raw request in minutes. And before Spring, I wrote a small typed-configuration library for our Java EE services: you declare a Java interface, and the library loads and validates the configuration. Both were shared through Nexus and reused across projects.

### 5. Clean modern Java

> I use records for all DTOs. Our REST and webhook payloads were all records, and I use immutable data wherever I can. I used pattern matching to choose the right handler based on the job's status in our event processing. And we moved our projects up to Java 24.

---

## 4. "Why did your company rate you so highly?"

> I think there are three reasons. First, I could find and prove the cause of problems that crossed several systems, not only in my code but in other modules, and describe them clearly. Second, my integrations were stable and easy to maintain. My manager saw that my solutions needed less support than others. Third, I understood the business side: I could explain to customers and project managers what was possible, and propose better alternatives.

**Доказательства, которые можно назвать:**

- **The reference letter:**
  > My reference says my expertise is excellent and exceptionally in-depth, and that I work at the highest professional level. It has the top grade.

  *Немецкие интервьюеры знают код: «stets zu unserer vollsten Zufriedenheit» — это высшая оценка.*
- **Several managers:**
  > I have references from several managers over the years, all with the same top marks.
- **Senior from the start:**
  > From my first year in Germany, management saw me as a senior developer.
- **Kept until the end:**
  > When the teams were closed, I was the last developer they kept.

---

## 5. Likely questions

**What's your strongest technical skill?**
> Finding the real cause of a problem that crosses several systems, and proving it with evidence.

**What makes you different from other senior developers?**
> I always think about the person who will support the system after me, and about the business user who will see the error. I build for them, not only for the happy path.

**What would your manager say about you?**
> That my solutions were stable and easy to maintain, and that I was the person to call when nobody knew why something broke.

**On a scale of 1 to 10, how good is your Java?**
> I'd say eight. I've used it every day for many years, but there's always more to learn.

*Никогда не говорите 10.*

**What did you use from the newer Java versions?**
> Records for all DTOs and immutable data in general. Pattern matching, for example to choose a handler based on a job's status. Text blocks. And we kept our projects on current Java, up to version 24.

**Have you used virtual threads?**
> Not in production yet, but I've studied them well. They are lightweight threads managed by the JVM, so you can run thousands of blocking tasks, like calls to external REST APIs, without a big thread pool. They're very easy to switch on. In Spring Boot it's one property. But they only help with I/O-bound work, not with CPU-heavy work. And since Java 24, `synchronized` no longer pins a virtual thread to its carrier thread, which removed one of the main pitfalls.

**What's the difference between JMS and Kafka?**
> JMS is a classic message broker: a message goes into a queue, a consumer takes it, and it's gone. Kafka is a distributed log: messages stay for a retention period, and each consumer group keeps its own position, its offset. So you can replay events, and partitions give you scale with ordering inside each partition. In our case, Kafka also let us subscribe from our own containers, independently of the product's server.

---

## 6. Не говорить

- **Цифры зарплаты.** Ни прошлую, ни текущую. Названная цифра становится якорем: компания вроде Databricks может предложить меньше, чем готова заплатить. Рост зарплаты за 10 лет — аргумент для вас, не для интервью.
- **Что вы «используете все новые возможности Java».** Говорите только о том, что использовали, с конкретным примером.
