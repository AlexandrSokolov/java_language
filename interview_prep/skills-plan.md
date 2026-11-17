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

Сейчас «плаваю» — нужна уверенность, не только строчка в CV. Цель — сертификат **CCDAK**.

*Раздел обновлён 17.11.2026, цены и условия проверены.*

**Экзамен CCDAK** (Confluent Certified Developer for Apache Kafka) — [страница экзамена](https://training.confluent.io/learn/courses/1102/confluent-certified-developer-for-apache-kafka-r)
- $150, 90 минут, ~60 вопросов, онлайн под наблюдением; сертификат действует 2 года.
- Пересдача в цену не входит: платится заново, не раньше чем через 7 дней.
- Скидка 10% при оплате картой — код `CFLTTAKE10`.
- **[Exam guide (PDF)](https://assets.confluent.io/m/26d0f06b432e68e9/original/20250414-DS-Certified_Developer_Apache_Kafka.pdf)** — темы, веса разделов и список рекомендованных курсов: это пункты 2–8 плана ниже.

**Веса разделов экзамена:** разработка приложений 28% · основы Kafka 23% · Kafka Connect 15% · observability 13% · Kafka Streams 12% · тестирование 8%.
**Не входит в экзамен:** администрирование и развёртывание Kafka, ksqlDB, RBAC, CFK, сети, инфраструктура.

**План учёбы — по порядку:**

| # | Что | Зачем | Ссылка / цена |
|---|---|---|---|
| 1 | Книга *Kafka: The Definitive Guide*, 2nd ed. — **есть** | Фундамент; читать параллельно с курсами, главы по теме курса | — |
| 2 | Apache Kafka Fundamentals *(из exam guide)* | Основы: топики, партиции, репликация, producers, consumers | [курс](https://developer.confluent.io/courses/apache-kafka/events/) |
| 3 | Fundamentals Accreditation *(из exam guide)* | Короткий тест: база усвоена | [тест](https://training.confluent.io/learningpath/confluent-fundamentals-accreditation) |
| 4 | Designing Events and Event Streams *(из exam guide)* | Как проектировать события и ключи | [курс](https://developer.confluent.io/courses/event-design/intro/) |
| 5 | Practical Event Modeling *(из exam guide)* | Моделирование данных в событиях | [курс](https://developer.confluent.io/courses/event-modeling/intro/) |
| 6 | Kafka Connect 101 *(из exam guide)* | 15% экзамена: source, sink, CDC | [курс](https://developer.confluent.io/courses/kafka-connect/intro/) |
| 7 | Kafka Streams 101 *(из exam guide)* | 12% экзамена | [курс](https://developer.confluent.io/courses/kafka-streams/get-started/) |
| 8 | Designing Event Driven Microservices *(из exam guide)* | Всё вместе на уровне архитектуры — и для собеседований | [курс](https://developer.confluent.io/courses/microservices/overview/) |
| 9 | Udemy, Stéphane Maarek — *Learn Apache Kafka for Beginners v3* — по желанию | Код producers и consumers на Java | [курс](https://www.udemy.com/course/apache-kafka/), ~€10–20 |
| 10 | Практические тесты CCDAK — *Practice Exams: Confluent Certified Apache Kafka Developer* (Udemy, 4 теста по 60 вопросов, обновлены 01/2026) | Пробный экзамен: проверить готовность, не тратить $150 вслепую | [тесты](https://www.udemy.com/course/confluent-certified-apache-kafka-developer-practice-exams/), ~€10–20 |
| 11 | Экзамен CCDAK | Сертификат в CV | [экзамен](https://training.confluent.io/learn/courses/1102/confluent-certified-developer-for-apache-kafka-r), $150 −10% |

*Про тесты:* старые тесты Maarek на Udemy охватывают ksqlDB и REST Proxy — по exam guide 2025 они больше не входят в экзамен. Поэтому выбраны более свежие тесты.

Курсы 2–8 — бесплатные, без инструктора, все перечислены в exam guide (раздел «Deepen your knowledge»). Платный 3-дневный курс с инструктором из того же PDF — не берём.

**Дыра:** для разработки приложений (28%), тестирования (8%) и observability (13%) отдельного курса в гайде нет. Закрываем книгой и практикой: писать producers и consumers, смотреть метрики — учебный проект Kafka (раздел 4) и эксперименты на cp-all-in-one (раздел 10).

**Темы:** топики и партиции, consumer groups и ребалансировка, offsets и коммиты, гарантии доставки (at-least-once, exactly-once, идемпотентный producer), ретраи и dead letter topic, порядок сообщений, Kafka Streams (основы), Schema Registry / Avro, мониторинг consumer lag.

## 4. Учебный проект: Kafka

**Цель:** научиться проектировать и настраивать Kafka и писать надёжные producers и consumers. Каждое решение проверять экспериментом и видеть результат в метриках, трейсах и логах. Kafka — главное, всё остальное — вспомогательное.

**Принцип — walking skeleton:** сначала минимальное приложение, но сразу со всеми слоями (кластер, producer, consumer, полная observability). Потом наращиваются домен и сложность.

**Домен:** заказы и платежи. Заказ создан → платёж авторизован или отклонён → баланс счёта изменён → заказ подтверждён или отменён → уведомление.

### Требования

**A. Кластер Kafka**
- 3 брокера в KRaft, каждому задан `broker.rack`: имитация трёх машин или зон.
- Replication factor 3, `min.insync.replicas = 2`.
- Решить и обосновать: какие топики, сколько партиций, какие ключи, retention, где нужен compaction.
- Записать решения кратко, в формате ADR: решение и почему.

**B. Producers**
- Несколько producers, в том числе несколько экземпляров одного сервиса.
- Идемпотентный producer, `acks=all`. Отдельно — опыты с `acks=1` и `acks=0`.
- Настройки batching, `linger.ms` и compression — измерить влияние на latency и throughput.
- Outbox на стороне заказов: состояние в БД и событие консистентны.

**C. Consumers**
- Несколько consumer groups на одних топиках (fan-out) и несколько экземпляров в группе (масштабирование).
- Ручной commit offset'а после обработки. Порядок сообщений по ключу.
- Идемпотентная обработка: дубликаты не меняют баланс дважды.
- Retry-топики с задержкой и DLT; повторная обработка из DLT.
- Rebalancing: сравнить eager и cooperative sticky.

**D. Контракты данных**
- Avro + Schema Registry, правила совместимости, эволюция схемы.
- Заголовки сообщений: тип события, trace context.

**E. Надёжность**
- Ни одно событие о платеже не теряется.
- Обновление баланса — exactly-once (транзакции Kafka) или идемпотентно. Выбрать и обосновать.
- Reconciliation: периодическая сверка заказов и платежей.

**F. Observability — с первого дня, всё по отдельности**
- OpenTelemetry Java agent в каждом сервисе → отдельный OTel Collector.
- **Трейсы** (Tempo или Jaeger): один заказ виден через все топики и сервисы.
- **Логи** в JSON с `trace_id` → Loki. Из трейса переход к логам.
- **Метрики** → Prometheus: метрики приложений (Micrometer), брокеров (JMX Exporter), consumer lag.
- Дашборды в Grafana; алерты.

**G. SLO**
- Определить SLI и SLO, например:
  - end-to-end latency «заказ → результат платежа» p99 < 2 с;
  - доля ошибок < 0.1%;
  - consumer lag < N сообщений.
- Error budget и алерт по скорости его расхода (burn rate).

### Эксперименты

Каждый — по схеме: гипотеза → эксперимент → что видно в Grafana и Tempo → вывод письменно.

1. **Уронить брокер-лидера:** выбор нового лидера, сжатие ISR, повторы producer'а, всплеск latency в трейсах.
2. **`acks=1` и падение лидера до репликации:** воспроизвести потерю подтверждённого сообщения. То же с `unclean.leader.election.enable=true`.
3. **Два брокера из трёх недоступны:** `NotEnoughReplicas` при `min.insync.replicas=2`.
4. **Чтение с follower'а** (`client.rack` + rack-aware replica selector): сравнить latency. Проверить гипотезу: consumer никогда не видит нереплицированные данные, потому что читает только до high watermark. Отставший follower отдаёт данные позже, но не «грязные».
5. **Медленный follower** (сетевая задержка или пауза контейнера): выпадение из ISR, метрики отставания реплики.
6. **Убить consumer посреди обработки:** повторная доставка, дубликаты, идемпотентность.
7. **Замедлить consumer:** рост lag → срабатывает алерт → добавить экземпляры → rebalance.
8. **«Ядовитое» сообщение:** retry → DLT → повторная обработка.
9. **Дубликат сообщения:** баланс не меняется дважды.
10. **Увеличить число партиций:** нарушение порядка по ключу.
11. **Несовместимая схема:** Schema Registry отклоняет.
12. **Сетевой раздел между брокерами:** поведение кластера и клиентов.

*Важно:* «асинхронного follower'а» в Kafka нет — все follower'ы сами забирают данные у лидера. Ситуация «подтверждено, но не реплицировано» воспроизводится через `acks=1` (эксперимент 2).

### Этапы
0. **Скелет:** кластер из 3 брокеров + 1 producer + 1 consumer + вся observability.
1. Полный поток заказов и платежей.
2. Надёжность: idempotency, retry/DLT, outbox, транзакции.
3. Эксперименты 1–12.
4. Kafka Streams (агрегаты по платежам) и Kafka Connect — outbox через CDC (Debezium). Это закрывает Streams и Connect для экзамена.

**Стек:** Java, Spring Boot, Spring Kafka. Один сервис на «чистом» Kafka-клиенте, чтобы видеть API без обёрток. Docker Compose, PostgreSQL, Kafka UI, инструмент для сетевых сбоев (например, Toxiproxy).

**Результат:** схема архитектуры, ADR по топикам, дашборды Grafana (экспорт JSON), отчёт по каждому эксперименту.

## 5. Свой проект (главное — показать на интервью и на GitHub)

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

## 6. Kotlin

- Бесплатно: Kotlin Koans и документация на kotlinlang.org; курс JetBrains «Kotlin for Java Developers» (Coursera, смотреть бесплатно).
- Для Java-разработчика вход за 1–2 недели. Закрепить — написать сервис проекта на Kotlin.

## 7. AWS

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

## 8. NoSQL

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

## 9. Paying for CKAD / CCDAK without a card (SEPA only)

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

## 10. Learning resources: structured courses + practice

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
