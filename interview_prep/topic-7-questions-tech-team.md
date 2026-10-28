# Topic 7 — Questions to the Technical Team

*Как пользоваться: в конце интервью задайте 3–5 вопросов, не больше. Если времени мало — берите из раздела 1. Хороший вопрос показывает, как вы думаете: о сбоях, о сопровождении, о качестве. После ответа — короткая реакция и мостик к своей истории (раздел 3). Вопросы — на английском, пояснения — по-русски.*

*Правило из вашего файла: спрашивайте про пользу, а не про факт. Не «Do you use microservices?», а «How do microservices help you scale?»*

---

## 1. Top 5, if time is short

1. What is the biggest technical challenge for the team right now?
2. What does your stack look like today, and where are you moving to?
3. What would my first three months look like?
4. How does code get from a merge to production, and how long does it take?
5. When something fails in production, how do you find out, and how do your users find out?

*Вопрос 5 — ваш «фирменный»: он сразу показывает ваш главный принцип, design for failure и видимые ошибки.*

---

## 2. All questions by theme

### The team

- **How is the team organized: how many engineers, and who does what?**
- How do you do code reviews and share knowledge in the team?
- How do new people get onboarded? Is there a mentor or a buddy?
- How does the team work with product, QA and other teams?
- Do you build mostly in-house, or do you also work with external partners or offshore teams?
  *→ Для вас важный вопрос: в Uptempo вы видели, к чему приводит вынос разработки наружу. Спрашивайте нейтрально, без своей истории.*

### Stack and architecture

- **What does your stack look like today, and are there plans to modernize it?**
- Which Java version and frameworks do you use?
- How does your architecture help you scale: microservices, event-driven, something else?
- How do your services handle failures between each other: retries, dead-letter queues, reconciliation?
  *→ Показывает ваш опыт. Если ответ слабый, у вас готова история (тема 4, A).*
- How do you handle breaking API changes between teams: versioning, contract tests?
  *→ Ваша тема: ломающиеся релизы продукта и REST-обёртка.*
- How do you test: unit, integration, end-to-end? Do you use Testcontainers?

### Delivery and operations

- **How does code get from a merge to production, and how long does it take?**
- How often do you release?
- Where do your services run: Kubernetes, which cloud? Does the team own the infrastructure, or a platform team?
  *→ Узнаёте, будет ли у вас доступ к Kubernetes и облаку, то есть к тому, чему вы хотите научиться.*
- What do you use for monitoring and observability? Do you work with SLOs?
- How do you handle on-call and production incidents?
- **When something fails, how do you find out, and how do your users find out?**

### Challenges

- **What is the biggest technical challenge for the team right now?**
- What is the hardest problem you solved recently?
- How do you deal with technical debt? Is there time for refactoring?
  *→ Ответ «на это нет времени» значит, что будет как в Uptempo с бюджетом на robustness.*
- What problem would you like the new person to solve first?

### Ways of working

- Do you work in Scrum, Kanban, or something else?
- How are technical decisions made, and who has the final word?
- How does the team use AI coding assistants? Are there any rules for it?
- Is English the language of meetings, code reviews and documentation?
  *→ Для вас критично. Ответ «meetings are mostly in German» — серьёзный сигнал.*

### Growth

- What does a strong senior engineer look like here after one year?
- What kind of projects could I work on in the first six to twelve months?
- Is there a path toward architecture responsibilities while staying hands-on?

---

## 3. React and connect

*После ответа не переходите сразу к следующему вопросу. Одна короткая реакция превращает список вопросов в разговор.*

> That sounds familiar. At Uptempo we had [a similar problem], and I [what you did].

> That's interesting. How did you decide on [their choice]?

> That's exactly the kind of work I'd like to do more of.

---

## 4. Не спрашивать на техническом интервью

- **Зарплату, отпуск, переезд.** Это для HR (тема 9).
- **То, что написано в вакансии или на сайте.** Иначе видно, что вы не подготовились.
- **Вопросы «да или нет».** Получите короткий ответ и неловкую паузу.
