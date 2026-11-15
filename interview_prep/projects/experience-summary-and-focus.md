# Мой опыт: сильные стороны, зоны роста и на чём фокусироваться

*Сводка по 8 проектам (Amazon, BRITA, Deka, Deutsche Bank, IKEA, Porsche, TPV, Volkswagen) — на основе кода. Для подготовки к техническому интервью: что подчёркивать, какие 3 проекта учить глубоко, и что отвечать на вопросы о слабых местах.*

---

## 1. Общая картина

За десять лет вы прошли через то, что большинство разработчиков видит только частями:

- **Все уровни одной системы**: от требований бизнеса и спецификации клиента — через дизайн модели данных и потоков — до кода, тестов, Docker-образа и поддержки в проде.
- **Интеграции с крупными внешними системами**: SAP, Coupa (Amazon), Adobe AEM (VW), Google BigQuery и Cloud Storage (IKEA), SFTP-серверы банков, собственная платформа из независимых модулей.
- **Все виды коммуникации**: REST, SOAP, JMS, webhooks, файлы через SFTP, облачные API, OAuth2, JWT, RSA-подписи.
- **Короткий цикл обратной связи**: вы видели, как ваше решение работает у бизнес-пользователя, и сами чинили, когда не работало.
- **Legacy и модернизация**: взяли чужие проекты (Deka, Porsche, VW), переписали их, перевели с Java 8/WildFly на Spring Boot, Java 21, Docker.

### 30 секунд — «Summarise your experience»
<details><summary>Show answer</summary>

For ten years I built integrations between our marketing platform and the systems of large enterprises:
SAP, Coupa, Adobe AEM, Google BigQuery, bank SFTP servers.
I designed them end to end, from the business requirement to production.
What I'm best at is reliability across systems without a shared transaction:
idempotent steps, retries, sagas, race conditions, and feedback that business users can act on.

</details>

---

## 2. Сильные технические стороны (то, чего не хватает многим)

*Не «я молодец», а конкретные навыки, которые у среднего разработчика есть в теории, но редко на практике. У каждого — доказательство из ваших проектов.*

### S1 · Думаете об отказах до того, как они случились
Многие пишут happy path и добавляют обработку ошибок потом. Вы проектируете с вопроса «что будет, если это упадёт посередине?».
- **Deka:** таймаут после того, как файл уже загрузился. При повторе Media Pool говорит «дубликат» — вы находите asset по MD5 и понимаете, что это ваша же загрузка. Это редкий уровень понимания.
- **Deka DIM v2:** импорт как state machine в базе, статус каждой строки, продолжение с места остановки.
- **VW:** статус в каждой строке grid — перезапуск продолжает с необработанных.

### S2 · Консистентность без распределённой транзакции
Dual write, saga, компенсация, outbox, reconciliation — для многих это слова из статей. Вы применяли их в реальных потоках денег и документов.
- **Deutsche Bank:** счёт = три REST-вызова без транзакции → журнал, идемпотентные шаги, компенсация.
- **Porsche:** перенос бюджета между двумя узлами → saga с operation ID.
- **Amazon:** Coupa без callback'ов → polling как встроенная reconciliation, сверка сумм.

### S3 · Конкурентность и race conditions
- **Porsche:** «прочитать → прибавить» на дереве узлов → race condition → одна очередь, один consumer. И вы можете объяснить, почему не lock и как масштабировать (partition по ветке).
- **IKEA:** back-pressure — семафоры, не больше 5 параллельных записей, хотя BigQuery отдаёт быстрее.
- **Deka webhooks:** порядок событий по каждому asset'у, разные asset'ы параллельно.
- **Deka DIM:** single-flight — второй запуск не стартует, пока идёт первый.
- **Porsche:** graceful shutdown очереди.

### S4 · Идемпотентность как привычка
Upsert по бизнес-ключу (артикул IKEA, номер PR в TPV, номер счёта в Deutsche Bank), inbox с unique constraint, delete-then-create (Amazon, Deka), «перечитать текущее состояние, а не верить событию» (Deka).

### S5 · Обратная связь, которую бизнес может использовать
Это ваша самая узнаваемая черта.
- Четыре уровня: reject → комментарий в job → email → лог без stacktrace (Amazon).
- Fail slow: пользователь видит **все** ошибки сразу (Amazon `Validated`, IKEA Excel, Deutsche Bank CSV-лог с плохими строками).
- «Почему ничего не произошло» — объяснимые пропуски (BRITA).
- Валидация конфигурации против текущего job type (BRITA), проверка конфигурации при деплое (Amazon).

### S6 · Тестируемость и отладка продакшна
- Integration-тесты по реальному ID job вместо отладчика (почти все проекты).
- Mock-сервисы внешних систем, включая всегда падающий (Amazon Coupa).
- Testcontainers с MySQL и SFTP, Docker Compose (Deka), WireMock (Deutsche Bank, Porsche).
- Remote debug внутри контейнера, баг только в Docker (VW).
- Quality gates: FindBugs, PMD, JaCoCo 90% (IKEA).

### S7 · Архитектура на уровне компонента
- Functional core / imperative shell, Reader, Either vs Validated (Amazon).
- Chain of responsibility (Deutsche Bank import), strategy (Deutsche Bank reports).
- Общий шаблон модулей для всех компонентов клиента (IKEA, Deka, TPV) — вторая интеграция строится быстрее первой.
- Собственные библиотеки: REST client wrapper, typed configuration.

### S8 · Работа с данными: JPA, Hibernate, миграции
*Схемы были простые, но вы работали с реальными вопросами персистентности — говорите об этом уверенно.*
- **JPA/Hibernate:** entity со связями one-to-many (Deka: импорт → строки → ошибки → история статусов), подводные камни (immutable collection в entity ломает `merge`, `@MapsId`).
- **Две persistence unit** в одном приложении (Deutsche Bank: база планировщика на чтение, своя база на запись).
- **Stateless sessions и scrollable cursors** для больших чтений и миграций (Deutsche Bank).
- **Unique constraints** как защита от дубликатов (inbox, mapping tables).
- **Транзакции:** `READ_COMMITTED`, `@TransactionAttribute(NOT_SUPPORTED)` для долгих задач, `saveAndFlush` после каждого шага, чтобы состояние пережило падение.
- **Liquibase** почти везде, миграция данных при старте, которую безопасно запустить дважды (Deutsche Bank, BRITA).

### Как это сказать — «What makes you different?»
<details><summary>Show answer</summary>

I always start with the question: what happens if this fails in the middle?
Most of my work was between systems that share no transaction,
so idempotency, retries, ordering and reconciliation are my daily tools, not theory.
And I always think about the person who sees the error. My goal is that a business user can fix the problem without calling us.

</details>

---

## 3. Какие проекты учить глубоко

*Восемь проектов не нужны. Выберите три, которые вместе покрывают почти любой вопрос. Остальные — по одной фразе, если спросят «what else».*

### 🥇 1. Deka — «я взял чужой проект и сделал его надёжным»
**Почему первым:** самый современный стек (Spring Boot, Java 21, Docker, Testcontainers, MySQL, Liquibase), больше всего реального кода, и в нём почти все темы сразу: legacy takeover, state machine в БД, lost response и MD5, retries, outbox, webhooks с порядком по asset'у, логи с correlation ID.
**Файл:** `claude/topic-experience-deka.md` — C1, C2, C3.

### 🥈 2. Porsche — «webhooks сделали неправильно, я переделал»
**Почему:** готовая история про race condition и одну упорядоченную очередь, плюс saga для перевода бюджета и деньги (валюты, округление). Отлично отвечает на «tell me about a concurrency problem» и «distributed transactions».
**Файл:** `claude/topic-experience-porsche.md` — C1, C2.

### 🥉 3. Amazon — «самая сложная интеграция и функциональный дизайн»
**Почему:** показывает дизайн, а не только надёжность: три системы, асинхронный Coupa, functional core / imperative shell, fail fast vs fail slow, изменчивый REST API (`@JsonAnySetter`). Хорошо для «most complex system you designed».
**Файл:** `claude/topic-experience-amazon.md` — C4, C1, C2.

### Запасной: IKEA — для вопросов про облако и объёмы
BigQuery, Cloud Storage, back-pressure семафорами, watermark для delta-загрузки, GDPR.
**Файл:** `claude/topic-experience-ikea.md` — C1.

### Остальные — одной фразой
<details><summary>Show answer</summary>

- **BRITA:** a self-service configuration UI in React, with validation that tells admins when their configuration breaks.
- **Deutsche Bank:** budget control in the planner: an invoice import as a chain of handlers, and a saga across three REST calls.
- **TPV:** a daily SAP import with currency conversion by month, safe to run twice.
- **Volkswagen:** a reliable transfer of approved assets into Adobe AEM, and an audit-proof approval report.

</details>

---

## 4. Карта: технический вопрос → какая история

| Вопрос интервьюера | Главная история | Запасная |
|---|---|---|
| Retries, back-off, transient vs permanent errors | Deka C2 | BRITA C5, IKEA C1 |
| Dual write, outbox, saga, distributed transaction | Deka C2 (outbox) | Deutsche Bank C2, Porsche C2 |
| Idempotency, duplicates | Deka C2 (MD5) | Porsche C1 (inbox), TPV C1 |
| Race condition, concurrency | Porsche C1 | IKEA C1 (semaphores), Deka C3 |
| Webhooks, event-driven, ordering | Porsche C1 | Deka C3 |
| JPA, Hibernate, database | Deka C1 (state machine) | Deutsche Bank C3/C4 |
| Legacy code, taking over a project | Deka C1 | Porsche C4 |
| Most complex system you designed | Amazon | Deka |
| Clean architecture, design patterns | Amazon C1 (functional core) | Deutsche Bank C1 (chain), C5 (strategy) |
| Validation, error handling, UX of errors | Amazon C3 | IKEA C3, BRITA C1 |
| Testing strategy | Deka C7 (Testcontainers) | Amazon C5, VW C3 |
| Hard bug, production debugging | VW C3 (container-only) | Topic 4: missing header, time zone |
| Cloud, big data | IKEA C1, C2 | — |
| Modernisation, Docker, Spring Boot | Porsche C4 | Deka C1 |

---

## 5. Зоны роста — честно, но без драмы

*Это не слабости, которые нужно прятать. Это следующий уровень, если целиться в Staff, Principal или архитектора. На интервью уровня Senior их почти не спросят, а если спросят — у каждой есть спокойный ответ.*

### G1 · Масштаб
Объёмы у вас были умеренные: сотни и тысячи событий в день, десятки тысяч записей. Высоконагруженных систем (тысячи запросов в секунду, шардинг, многоуровневый кэш) не было.
<details><summary>Show answer</summary>

My systems were not high-traffic. Their difficulty was correctness across systems, not throughput.
But the same principles scale: idempotency, back-pressure, partitioning by key.
I know where I'd start if volume grew, for example partitioning the Porsche queue by branch, like Kafka partitions.

</details>

### G2 · Платформа и эксплуатация
Kubernetes, AWS, infrastructure as code, метрики и трейсинг принадлежали другой команде. Docker и Docker Compose — ваши.
<details><summary>Show answer</summary>

Production infrastructure was owned by a separate team. I packaged everything as Docker images and worked closely with them.
Kubernetes and the cloud are exactly what I'm learning now, and what I want to do more of in my next role.

</details>

### G3 · Брокеры сообщений как владелец
Kafka и JMS — как consumer. Проектирования топиков, схем, partition-стратегий для всей компании не было.
<details><summary>Show answer</summary>

I used Kafka and JMS as a consumer, and I know the delivery guarantees well, because I had to handle duplicates and ordering myself.
I haven't designed a company-wide event platform yet.

</details>

### G4 · Наблюдаемость
В основном логи: уровни, correlation ID, request logging. Метрики и распределённый трейсинг — меньше.
<details><summary>Show answer</summary>

My focus was making problems visible: correlation IDs, readable logs, status APIs, emails to the business.
The next step for me is metrics and tracing, with Prometheus and OpenTelemetry.

</details>

### G5 · Архитектура на уровне организации
Ваши решения — на уровне компонента и интеграции внутри одной платформы. Решений уровня «как 10 команд строят систему», ADR, C4-диаграмм для всей организации было меньше.
<details><summary>Show answer</summary>

I designed integrations end to end, but within one platform. I didn't define the architecture for many teams.
That's a direction I want to grow into, and my integration experience is a good base: I've seen most of the ways systems fail at their boundaries.

</details>

### Что из этого следует
- Для **Senior Backend / Integration Engineer** ваш профиль сильный, в некоторых темах сильнее типичного.
- Для **Staff / архитектора** не хватает масштаба и эксплуатации, и это ровно то, что закрывает ваш план (`skills-plan.md`: Kubernetes, Kafka, AWS).

---

## 6. Перед интервью: проверьте, что можете ответить глубже

*Технические интервьюеры копают туда, где звучит уверенно. По этим темам будьте готовы к 2–3 уточняющим вопросам подряд:*

- **Outbox:** где таблица, что в одной транзакции, кто читает, что если worker упал после отправки (at-least-once), почему получатель должен быть идемпотентным.
- **Inbox:** какой ключ, почему unique constraint, а не проверка «select, потом insert» (race condition между ними).
- **Exponential back-off + jitter:** формула (база × 2^попытка + случайность), лимит попыток, какие ошибки повторять (5xx, 429, таймаут), какие нет (4xx).
- **`@JsonAnySetter` / `@JsonAnyGetter`:** как неизвестные поля попадают в map и возвращаются обратно, зачем это при PUT.
- **Watermark** (IKEA): где хранится, почему не «за последние 24 часа».
- **Saga:** orchestration vs choreography, компенсация, почему 2PC не подходит для REST.
