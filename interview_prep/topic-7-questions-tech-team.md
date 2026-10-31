# Topic 7 — Questions to the Technical Team

*Как пользоваться: в конце интервью задайте 3–5 вопросов, не больше. Если времени мало — берите из Top 5. Хороший вопрос показывает, как вы думаете: о сбоях, о сопровождении, о качестве. После ответа — короткая реакция и мостик к своей истории. Вопросы — на английском, пояснения — по-русски.*

*Правило из вашего файла: спрашивайте про пользу, а не про факт. Не «Do you use microservices?», а «How do microservices help you scale?»*

*Карточки: в заголовке — ключ, под ним спрятан сам вопрос. Прочитайте ключ, задайте вопрос вслух по-английски, потом откройте и сверьтесь.*

---

## 1. Цепочки

### Top 5, if time is short

**Цепочка:** Challenge → Stack → First three months → Merge to production → Failures

| # | Ключ | Зачем этот вопрос | Начало вопроса |
|---|---|---|---|
| 1 | **Challenge** | С чем вы будете работать на самом деле. | *"What is the biggest technical challenge…"* |
| 2 | **Stack** | Где они сейчас и куда движутся. | *"What does your stack look like today…"* |
| 3 | **First three months** | Чего ждут от вас на старте. | *"What would my first three months…"* |
| 4 | **Merge to production** | Насколько зрелая поставка кода. | *"How does code get from a merge to production…"* |
| 5 | **Failures** | Ваш «фирменный» вопрос: сразу показывает design for failure и видимые ошибки. | *"When something fails in production…"* |

### All questions by theme

**Цепочка тем:** Team → Stack → Delivery → Challenges → Ways of working → Growth

| Тема | Что узнаёте | Главный вопрос темы |
|---|---|---|
| **Team** | Состав, ревью, онбординг, внешние команды. | team structure |
| **Stack** | Стек, Java, масштабирование, сбои между сервисами, API, тесты. | stack today and modernization |
| **Delivery** | Путь до прода, релизы, Kubernetes и облако, мониторинг, дежурства. | merge to production · failures |
| **Challenges** | Главная проблема, техдолг, первая задача для новичка. | biggest challenge |
| **Ways of working** | Процесс, решения, AI-ассистенты, язык команды. | — |
| **Growth** | Как выглядит сильный сеньор, проекты, путь к архитектуре. | — |

---

## 2. Top 5 — cards

### Top 1 · Challenge
<details><summary>Show question</summary>

What is the biggest technical challenge for the team right now?

</details>

### Top 2 · Stack
<details><summary>Show question</summary>

What does your stack look like today, and where are you moving to?

</details>

### Top 3 · First three months
<details><summary>Show question</summary>

What would my first three months look like?

</details>

### Top 4 · Merge to production
<details><summary>Show question</summary>

How does code get from a merge to production, and how long does it take?

</details>

### Top 5 · Failures
<details><summary>Show question</summary>

When something fails in production, how do you find out, and how do your users find out?

</details>

*Вопрос 5 — ваш «фирменный»: он сразу показывает ваш главный принцип, design for failure и видимые ошибки.*

---

## 3. All questions by theme — cards

### The team

### Team · Structure
<details><summary>Show question</summary>

How is the team organized: how many engineers, and who does what?

</details>

### Team · Reviews and knowledge
<details><summary>Show question</summary>

How do you do code reviews and share knowledge in the team?

</details>

### Team · Onboarding
<details><summary>Show question</summary>

How do new people get onboarded? Is there a mentor or a buddy?

</details>

### Team · Other teams
<details><summary>Show question</summary>

How does the team work with product, QA and other teams?

</details>

### Team · In-house or external
<details><summary>Show question</summary>

Do you build mostly in-house, or do you also work with external partners or offshore teams?

</details>

*Для вас важный вопрос: в Uptempo вы видели, к чему приводит вынос разработки наружу. Спрашивайте нейтрально, без своей истории.*

### Stack and architecture

### Stack · Today and modernization
<details><summary>Show question</summary>

What does your stack look like today, and are there plans to modernize it?

</details>

### Stack · Java version
<details><summary>Show question</summary>

Which Java version and frameworks do you use?

</details>

### Stack · Scaling
<details><summary>Show question</summary>

How does your architecture help you scale: microservices, event-driven, something else?

</details>

### Stack · Failures between services
<details><summary>Show question</summary>

How do your services handle failures between each other: retries, dead-letter queues, reconciliation?

</details>

*Показывает ваш опыт. Если ответ слабый, у вас готова история (тема 4, A).*

### Stack · Breaking API changes
<details><summary>Show question</summary>

How do you handle breaking API changes between teams: versioning, contract tests?

</details>

*Ваша тема: ломающиеся релизы продукта и REST-обёртка.*

### Stack · Testing
<details><summary>Show question</summary>

How do you test: unit, integration, end-to-end? Do you use Testcontainers?

</details>

### Delivery and operations

### Delivery · Merge to production
<details><summary>Show question</summary>

How does code get from a merge to production, and how long does it take?

</details>

### Delivery · Release frequency
<details><summary>Show question</summary>

How often do you release?

</details>

### Delivery · Where it runs
<details><summary>Show question</summary>

Where do your services run: Kubernetes, which cloud?
Does the team own the infrastructure, or a platform team?

</details>

*Узнаёте, будет ли у вас доступ к Kubernetes и облаку, то есть к тому, чему вы хотите научиться.*

### Delivery · Monitoring
<details><summary>Show question</summary>

What do you use for monitoring and observability? Do you work with SLOs?

</details>

### Delivery · On-call
<details><summary>Show question</summary>

How do you handle on-call and production incidents?

</details>

### Delivery · Failures
<details><summary>Show question</summary>

When something fails, how do you find out, and how do your users find out?

</details>

### Challenges

### Challenges · Biggest challenge
<details><summary>Show question</summary>

What is the biggest technical challenge for the team right now?

</details>

### Challenges · Hardest problem
<details><summary>Show question</summary>

What is the hardest problem you solved recently?

</details>

### Challenges · Technical debt
<details><summary>Show question</summary>

How do you deal with technical debt? Is there time for refactoring?

</details>

*Ответ «на это нет времени» значит, что будет как в Uptempo с бюджетом на robustness.*

### Challenges · First problem for me
<details><summary>Show question</summary>

What problem would you like the new person to solve first?

</details>

### Ways of working

### Ways · Process
<details><summary>Show question</summary>

Do you work in Scrum, Kanban, or something else?

</details>

### Ways · Decisions
<details><summary>Show question</summary>

How are technical decisions made, and who has the final word?

</details>

### Ways · AI assistants
<details><summary>Show question</summary>

How does the team use AI coding assistants? Are there any rules for it?

</details>

### Ways · Language
<details><summary>Show question</summary>

Is English the language of meetings, code reviews and documentation?

</details>

*Для вас критично. Ответ «meetings are mostly in German» — серьёзный сигнал.*

### Growth

### Growth · Strong senior
<details><summary>Show question</summary>

What does a strong senior engineer look like here after one year?

</details>

### Growth · First projects
<details><summary>Show question</summary>

What kind of projects could I work on in the first six to twelve months?

</details>

### Growth · Architecture path
<details><summary>Show question</summary>

Is there a path toward architecture responsibilities while staying hands-on?

</details>

---

## 4. React and connect — cards

*После ответа не переходите сразу к следующему вопросу. Одна короткая реакция превращает список вопросов в разговор.*

### React · A familiar problem
<details><summary>Show phrase</summary>

That sounds familiar. At Uptempo we had [a similar problem], and I [what you did].

</details>

### React · Ask about their choice
<details><summary>Show phrase</summary>

That's interesting. How did you decide on [their choice]?

</details>

### React · Show interest
<details><summary>Show phrase</summary>

That's exactly the kind of work I'd like to do more of.

</details>

---

## 5. Не спрашивать на техническом интервью

- **Зарплату, отпуск, переезд.** Это для HR (тема 9).
- **То, что написано в вакансии или на сайте.** Иначе видно, что вы не подготовились.
- **Вопросы «да или нет».** Получите короткий ответ и неловкую паузу.
