# План: закрыть пробелы в навыках

Составлено 06.11.2026. Цены и акции указаны по памяти, без поиска — **перед покупкой проверить**.

## Зачем

По разбору вакансий (октябрь–ноябрь 2026) чаще всего не хватало:

| # | Навык | Где встречался |
|---|---|---|
| 1 | **Немецкий B2–C1** | DB infraView, DCON, Schmitt + Sohn, Rentenversicherung, meiyu, TecFox, FindYou и др. — самый частый стоп |
| 2 | **Kubernetes** (Helm, ArgoCD) | Schmitt + Sohn, Tipico, Lobster, iSAX, TecFox, SIXT, GLS, Mobimeo |
| 3 | **Kafka** (уверенно, а не «работал») | INTERSPORT, Schmitt + Sohn, meiyu, TecFox, SIXT, GLS, Tide |
| 4 | **AWS / облако** (ECS, Lambda, SQS, Terraform) | INTERSPORT, Dedalus, GLS, Tide, Pleo, Mobimeo |
| 5 | **Kotlin** | GLS, dmTECH, x-tention, Pleo, INTERSPORT, voize |
| 6 | **NoSQL** (MongoDB, Redis, Cassandra) | DB, Bookwire, argumentum, SIXT |
| 7 | TypeScript + Angular/React | фуллстек-вакансии (Dedalus, INTERSPORT, dmTECH) — пока не приоритет |

Quarkus — **не учим**: тот же Jakarta EE / MicroProfile (JAX-RS, CDI), что и так знаю; осваивается на месте.

## Приоритеты

1. **Немецкий** — каждый день, главный приоритет.
2. **Kubernetes** — курс + сертификат CKAD + свой проект. Цель: **2–3 недели**.
3. **Kafka** — в том же проекте + сертификат CCDAK.
4. **Kotlin** — часть проекта пишем на Kotlin.
5. **AWS** — после Kubernetes и Kafka.
6. **NoSQL** — по ходу: Redis и/или MongoDB в проекте.

Поиск работы на это время — пассивный: подаю только на сильные совпадения. Уже назначенные интервью не отменяю (Tipico — пн 09.11, 15:00).

---

## 1. Немецкий

- Цель: быстро пройти B1 → B2, дальше C1.
- **Сдать официальный экзамен B2** (telc или Goethe) — это «корочка», которая весит больше любой самооценки в CV и снимает вопрос «B1 или нет» на первом звонке.
- Когда будет сертификат — обновить CV и абзац про язык в письмах.

## 2. Kubernetes

**Бесплатно, чтобы разобраться:**
- Linux Foundation «Introduction to Kubernetes (LFS158)» на edX — смотреть бесплатно, сертификат курса платный (не нужен).
- Официальные туториалы на kubernetes.io.
- Killercoda — бесплатные интерактивные задания в браузере.
- Дома: **kind**, k3d или minikube.

**Сертификат: CKAD** (Certified Kubernetes Application Developer, Linux Foundation / CNCF)
- Именно для разработчиков; практический экзамен в терминале, не тест с вариантами.
- Стоит несколько сотен долларов. На **Black Friday / Cyber Monday (конец ноября)** Linux Foundation обычно даёт большие скидки — купить ваучер тогда; экзамен можно сдать позже (обычно в течение года после покупки).
- Обычно в комплекте 2 попытки и тренировочный симулятор экзамена.
- CKA (для администраторов) — **не нужен**.

**Дешёвые курсы (Udemy и т. п.)** — только для подготовки. Их сертификаты о прохождении работодатель почти не ценит: в CV пишем CKAD, а не Udemy.

**Темы для CKAD / работы:** Pods, Deployments, Services, Ingress, ConfigMaps и Secrets, probes (liveness/readiness), ресурсы и лимиты, Jobs/CronJobs, Helm, логи и отладка (`kubectl logs`, `describe`, `exec`).

**План на 2–3 недели** (по 2–3 часа в день):
- Неделя 1: основы на kind + бесплатные курсы, разворачиваю первый сервис проекта.
- Неделя 2: Helm, Ingress, конфиги и секреты, мониторинг; задачи на Killercoda.
- Неделя 3: тренировочные экзамены → сдача CKAD.

## 3. Kafka

Сейчас «плаваю» — нужна уверенность, не только строчка в CV.

**Сертификат: CCDAK** (Confluent Certified Developer for Apache Kafka)
- Сертификат именно для разработчиков на Kafka (тест; стоимость порядка $150 — проверить).
- Бесплатная подготовка: курсы на сайте Confluent Developer (Kafka 101, Kafka Streams, Schema Registry и др.).

**Темы:** топики и партиции, consumer groups и ребалансировка, offsets и коммиты, гарантии доставки (at-least-once, exactly-once, идемпотентный producer), ретраи и dead letter topic, порядок сообщений, Kafka Streams (основы), Schema Registry / Avro, мониторинг consumer lag.

## 4. Свой проект (главное — показать на интервью и на GitHub)

Идея: маленькая событийная система, где видны мои сильные стороны — надёжность, идемпотентность, сверка, мониторинг.

**Например:** «заказы → оплата → уведомления»
- 2–3 сервиса на **Spring Boot**; один из них — на **Kotlin**.
- Обмен событиями через **Kafka**: идемпотентный consumer, ретраи, dead letter topic, сверка (reconciliation).
- **PostgreSQL**; **Redis** как кэш (закрывает NoSQL); по желанию MongoDB.
- Тесты: JUnit 5, **Testcontainers** (Kafka, PostgreSQL).
- **Docker** → **Kubernetes** (kind локально), деплой через **Helm**.
- Мониторинг: **Prometheus + Grafana** (метрики Micrometer, дашборд с consumer lag и ошибками).
- CI/CD: **GitHub Actions** — сборка, тесты, образ в GitHub Container Registry, деплой (`helm upgrade` или **ArgoCD**).
- Позже — часть в **AWS** (см. ниже), инфраструктура через **Terraform**.
- README на английском: архитектурная схема, как запустить, какие решения приняты и почему.
- Писать в режиме **agent-first** (ИИ-агенты пишут код, я проверяю) — заодно практика того, что ищут в вакансиях.

**В CV после проекта:** ссылка на GitHub + честные формулировки, например «Kubernetes (hands-on: Helm, deployments; CKAD 2026)», «Kafka (CCDAK 2026)» — без намёка на продакшен-опыт, которого не было.

## 5. Kotlin

- Бесплатно: Kotlin Koans и документация на kotlinlang.org; курс JetBrains «Kotlin for Java Developers» (Coursera, смотреть бесплатно).
- Для Java-разработчика вход за 1–2 недели. Закрепить — написать сервис проекта на Kotlin.

## 6. AWS

**Можно учить без лицензии и без работодателя:** создать свой аккаунт AWS — для новых аккаунтов есть бесплатный уровень / стартовые кредиты (условия менялись — проверить при регистрации).
- **Обязательно сразу:** настроить бюджет и оповещения о расходах (AWS Budgets), удалять ресурсы после экспериментов. Kubernetes в AWS (EKS) — платный, его не трогаем.
- Бесплатная альтернатива для локальной игры: **LocalStack** (эмулирует сервисы AWS на своём компьютере).
- Бесплатные курсы: AWS Skill Builder.

**Что это за сервисы:**
- **Lambda** — функция, которую AWS запускает сам по событию (запрос, сообщение, файл); серверов нет, платишь за вызовы.
- **SQS** — очередь сообщений (проще Kafka: без партиций и истории, сообщение забирают и удаляют).
- **SNS** — рассылка событий подписчикам (pub/sub).
- **ECS** — запуск Docker-контейнеров в AWS (проще, чем Kubernetes).
- **S3** — хранилище файлов. **RDS** — управляемая PostgreSQL/MySQL. **DynamoDB** — NoSQL-база AWS.
- **Terraform** — описание инфраструктуры (сервисы, сети, базы) в файлах-коде: `terraform apply` создаёт всё в облаке, `destroy` удаляет. Аналоги: AWS CDK, Pulumi.

**Сертификат (позже):** AWS Certified Developer – Associate (порядка $150 — проверить). Начальный Cloud Practitioner — проще, но весит меньше.

**В проекте:** например, сервис уведомлений как Lambda, читающая из SQS; инфраструктура — Terraform.

## 7. NoSQL

- Redis — в проекте как кэш. Бесплатные курсы: Redis University.
- MongoDB — бесплатные курсы MongoDB University (есть сертификат MongoDB Associate Developer, платный).
- Cassandra — только по необходимости (DataStax Academy, бесплатно).

---

## Порядок по времени (черновик)

| Период | Немецкий | Технически |
|---|---|---|
| Недели 1–3 | каждый день | Kubernetes + каркас проекта → **CKAD** |
| Недели 3–5 | каждый день | Kafka в проекте → **CCDAK** |
| Недели 5–6 | каждый день | Kotlin (сервис проекта), Redis |
| Дальше | каждый день → экзамен **B2** | AWS (LocalStack → свой аккаунт), Terraform → по желанию сертификат AWS |

Конец ноября: купить ваучеры со скидками Black Friday (CKAD, при желании CCDAK).

---

## 8. Paying for CKAD / CCDAK without a card (SEPA only)

*Added 07.11.2026, without web search — verify partner status, prices and terms before paying.*

**My constraint:** no working Visa/Mastercard, no PayPal, no virtual cards. Available: **SEPA transfer** and **Google Play gift cards**.

**What does NOT work:**
- **Google Play gift cards** — neither Linux Foundation (CKAD) nor Confluent (CCDAK) sells exams through an app; only on their websites with card checkout.
- **Direct SEPA on the official sites** — checkout expects a card.

**Validity (no rush after buying):**
- **CKAD:** 12 months from purchase. The free retake must also fit into these 12 months. Includes 2 sessions of the **killer.sh** exam simulator.
- **CCDAK:** usually 12 months from purchase — confirm at purchase.
- → Buy at Black Friday, learn in December–February, take the exam when ready.

### Option A — German authorized partner, invoice + SEPA

The partner sells an exam voucher, issues an invoice (Rechnung), I pay by SEPA, they email the voucher code, I redeem it in **my own** Linux Foundation / Confluent account.

| Partner | Covers | Contact |
|---|---|---|
| **Fast Lane** (flane.de) | Linux Foundation, Confluent (per Gemini — verify) | info@flane.de |
| **NobleProg** (nobleprog.de) | Linux Foundation (verify) | support form on site |

**Before paying, check:**
1. The partner is listed on the **official partner list** of Linux Foundation / Confluent.
2. What exactly I get: CKAD exam **incl. retake + killer.sh**? Valid from purchase or from redemption?
3. Price in EUR incl. VAT, invoice to a private person.
4. Whether they pass on the Black Friday discount (usually **not** — partners sell at list price; that's the main downside).

**Skip:** non-EU sellers (e.g. Merit Global Training) — "bank wire" there is an international SWIFT transfer, not SEPA: fees, and my bank may block or question it.

**Request text (German):**
```
Guten Tag,

ich möchte als Privatperson nur einen Prüfungsgutschein (Exam Voucher) für die Zertifizierung CKAD (Linux Foundation) und ggf. CCDAK (Confluent) erwerben – ohne Schulung.

Ist das möglich, und kann ich per Rechnung / SEPA-Überweisung bezahlen?
Bitte teilen Sie mir den Preis inkl. MwSt. mit und ob die Wiederholungsprüfung (Retake) und der Simulator killer.sh enthalten sind. Ab wann gilt die Gültigkeit von 12 Monaten?

Vielen Dank und freundliche Grüße
Aleksandr Sokolov
```

### Option B — a friend pays by card, I repay by SEPA (only way to get the Black Friday price)

- Cleanest variant: the friend pays **while logged into MY Linux Foundation account** (my name, matching my ID). The exam lands directly in my account — no voucher transfer needed. Individual purchases may not be transferable, so a "buy it yourself and send me the code" approach is riskier.
- The friend needs his phone for 3-D Secure confirmation → easiest sitting together.
- I repay by normal SEPA transfer.

### Option C — Bundesagentur für Arbeit (BA pays the provider directly — no payment problem)

I'm registered as arbeitsuchend → ask my Arbeitsvermittler **before buying anything** (no reimbursement afterwards):
- **Bildungsgutschein** for an AZAV-certified course (Kubernetes / DevOps / cloud courses often include the certification exam). Pros: free, structured, with a teacher. Cons: usually in German (also trains German), often full-time for weeks — conflicts with job search and intensive German.
- **Vermittlungsbudget (§ 44 SGB III):** can cover individual job-search costs — ask whether exam fees for CKAD/CCDAK qualify.

### Exam-day note

The name in the Linux Foundation / Confluent account must match the ID shown to the online proctor **exactly** (Latin letters). Check the accepted ID list before scheduling (passport vs. residence permit).

---

## 9. Learning resources: structured courses + practice

*Added 07.11.2026, without web search. Goal: courses where someone **explains** concepts and operations, combined with hands-on practice — not just exercises.*

### Payable with Google Play gift cards

- **Udemy via the Android app** — courses can be bought as in-app purchases through Google Play (check the price shown in the app; it can differ from web sale prices). After purchase the course is also available on the web.
  - **CKAD:** Mumshad Mannambeth — *Kubernetes Certified Application Developer (CKAD) with Tests*. Lectures + browser labs (KodeKloud) + mock exams. The most popular CKAD prep.
  - **Kafka:** Stéphane Maarek — *Apache Kafka Series – Learn Apache Kafka for Beginners*; same author has courses on Kafka Streams, Schema Registry and **CCDAK practice tests**.
- **Google Play Books (ebooks):**
  - Benjamin Muschko — *Certified Kubernetes Application Developer (CKAD) Study Guide* (O'Reilly): follows the exam curriculum, with exercises.
  - *Kafka: The Definitive Guide*, 2nd ed. (O'Reilly) — Confluent usually gives it away **free** after registration, so check there first.
- **LinkedIn Learning** — included in LinkedIn Premium (Career); on Android, Premium can usually be bought via Google Play. Good Kubernetes and Kafka courses. Some German city libraries give free LinkedIn Learning access with a library card (paid in cash) — ask the local Stadtbibliothek.

### Free structured courses

**Kubernetes**
- **LFS158 Introduction to Kubernetes** (Linux Foundation, edX) — free to audit.
- **TechWorld with Nana** (YouTube) — full Kubernetes course, very clear explanations of concepts.
- **freeCodeCamp** (YouTube) — several full-length Kubernetes courses.
- **kubernetes.io** — "Learn Kubernetes Basics" tutorials + docs (the docs are allowed during the CKAD exam — get used to navigating them).

**Kafka**
- **Confluent Developer** (developer.confluent.io) — free video courses with hands-on exercises, by the people behind Kafka and the exam: *Kafka 101*, *Kafka Internal Architecture* (replication, ISR, leader election), *Kafka Streams 101*, *Schema Registry 101*, *Kafka Connect 101*.
- **Confluent training portal** — some free self-paced courses; check what's available.

### Practice (after the concept lessons)

**CKAD**
- **dgkanatsios/CKAD-exercises** (GitHub) — the classic exercise set by exam topic; solve without peeking.
- **Killercoda** — free CKAD scenarios in the browser.
- **killer.sh** — 2 sessions included with the exam; harder than the real exam. Do it at the end.

**Kafka**
- **Confluent cp-all-in-one** (GitHub, Docker Compose) — brokers, Schema Registry, Connect, ksqlDB locally. Kill brokers and observe: `acks=1` vs `acks=all`, `min.insync.replicas`, leader failover, consumer rebalancing, lag.
- Practice tests (e.g. Maarek's CCDAK tests on Udemy) — CCDAK is multiple choice and tests tricky details.

*Removed from Gemini's list:* "k8s.naveedkumbhar.com" — couldn't confirm it exists; don't rely on it.

### Suggested combination

| Exam | Learn (explanations) | Practice | Final check |
|---|---|---|---|
| **CKAD** | Udemy Mumshad (or free LFS158 + Nana) | CKAD-exercises + Killercoda daily, project on kind | killer.sh ×2 → exam |
| **CCDAK** | Confluent Developer courses + *Definitive Guide* | experiments on cp-all-in-one, Kafka in own project | practice tests → exam |
