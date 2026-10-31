# Topic 5 — How I Work with People

*Как пользоваться: общий ответ на «How do you work with others?» — цепочкой. Истории — под конкретные вопросы. Учите цепочки и выводы, остальное рассказывайте своими словами. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

---

## 1. Какую историю на какой вопрос

| Если спрашивают про… | История |
|---|---|
| mentoring · leadership experience · helping others grow | **A. The junior** |
| sharing knowledge · raising quality in other teams | **B. Design for human error** |
| someone blaming others · fairness · team culture | **C. Email in three places** |
| disagreeing with your manager | **D. Robustness now or later** |
| a conflict · a difficult colleague · something you'd do differently | **E. Two versions of one component** |
| influencing without authority · convincing with facts | **F. A shared REST standard** |
| working with non-technical people | **G. Business people** |
| your communication style · team player | **H. Calm and focused** |
| how you keep learning | **I. Interviewing myself** |
| what you learned in an international team | **J. A new work culture** |

*Ещё две истории из темы 4 подходят и сюда: C «баг с часовыми поясами» — спор с командой продукта, F «SOAP-выгрузка на гигабайт» — спор с проектным менеджером.*

---

## 2. Цепочки

Сначала запомните цепочки. Если сбились: вспомните следующее звено, скажите его первую фразу, и рассказ продолжится.

### "How do you work with other people?" (about 60 seconds)

**Цепочка:** Learn → Teach → Share → No blame → Disagree → Commit → Tone

| # | Ключ | О чём звено | Первая фраза |
|---|---|---|---|
| 1 | **Learn** | Учусь, задавая себе вопросы: интервьюирую сам себя. | *"I keep learning by asking myself questions."* |
| 2 | **Teach** | Учу так же: работа в паре с джуниором, простые фундаментальные вопросы. | *"I teach the same way."* |
| 3 | **Share** | Делился с другими командами тем, что делало наши интеграции стабильными. | *"With other teams, I shared…"* |
| 4 | **No blame** | Главный принцип: система ждёт ошибок и показывает каждую тому, кто её сделал. | *"My key principle…"* |
| 5 | **Disagree** | Спорю фактами, без обвинений. | *"I disagree with facts, never with blame."* |
| 6 | **Commit** | Если бюджет говорит «нет» — принимаю решение и планирую улучшение на потом. | *"And when the budget says no…"* |
| 7 | **Tone** | Как сказать — так же важно, как быть правым. | *"And I learned that how you say something…"* |

### The stories

| История | Цепочка | Вывод |
|---|---|---|
| **A. The junior** | pair work → simple fundamental questions → he studied at home → he explained it back | *I teach through questions, not lectures.* |
| **B. Design for human error** | our integrations were more stable → I shared why → people will make mistakes → make each error visible | *Design for human error, not for perfect users.* |
| **C. Email in three places** | emails don't work → a developer blames the config team → I explained → a design problem | *Look at the system, not at the people.* |
| **D. Robustness now or later** | I wanted robustness now → he had a budget → I understood → planned for the next update | *Say clearly what I think; once it's decided, commit.* |
| **E. Two versions of one component** | a simple routing rule → IT refused → I escalated with facts → right on substance, wrong in tone | *Today I'd sit down together first, and only then go to the manager.* |
| **F. A shared REST standard** | different clients in every team → one interface for server and client → the reactive argument → choose by real use | *Choose by how we really use a tool, not by what it promises.* |
| **G. Business people** | translate → limits in business terms → never just "no" → offer an alternative | *Never just say no; offer an alternative.* |
| **H. Calm and focused** | calm and focused → not the loudest → explain complex issues clearly, with facts | *I'm the one who explains it clearly.* |
| **I. Interviewing myself** | learning never stops → interview myself → can't explain it = don't know it yet → enjoy sharing | *If I can't explain it, I don't know it yet.* |
| **J. A new work culture** | international team → share openly → disagree with respect → no authority at others' expense → help everyone see the problem | *Help people see the problem, without blaming.* |

---

## 3. "How do you work with other people?" — cards

### 1 · Learn
<details><summary>Show answer</summary>

I keep learning by asking myself questions.
I regularly interview myself on topics, because if I don't think about something, I forget it.

</details>

### 2 · Teach
<details><summary>Show answer</summary>

I teach the same way. With a junior, we worked in pairs, and I asked him simple but fundamental questions.

</details>

### 3 · Share
<details><summary>Show answer</summary>

With other teams, I shared what made our integrations stable.

</details>

### 4 · No blame
<details><summary>Show answer</summary>

My key principle: a good system expects people to make mistakes,
and shows each error clearly to the person who made it.
Blaming someone for not reading the documentation is not enough.

</details>

### 5 · Disagree
<details><summary>Show answer</summary>

I disagree with facts, never with blame.

</details>

### 6 · Commit
<details><summary>Show answer</summary>

And when the budget says no, I accept the decision and plan the improvement for later.

</details>

### 7 · Tone
<details><summary>Show answer</summary>

And I learned that how you say something matters as much as being right.

</details>

---

## 4. The stories — cards

### A. The junior: teaching through questions

### A · Situation
<details><summary>Show answer</summary>

We hired a junior developer who really wanted to work with us, and we worked in pairs.

</details>

### A · Approach
<details><summary>Show answer</summary>

I believe learning means answering questions again and again; otherwise you forget.
So while we worked, I asked simple but fundamental questions.
For example, when we used collections: what is a hash for? Why do we need both `hashCode` and `equals`?

</details>

### A · Result
<details><summary>Show answer</summary>

He went home, read about it, and the next day he explained it back to me.
That's the best sign that someone has understood: they can explain it.

</details>

### A · Lesson
<details><summary>Show answer</summary>

I teach through questions, not lectures.

</details>

*Не говорите, что ему было стыдно. Скажите «he was motivated to close the gaps».*

---

### B. Design for human error: sharing with other teams

### B · Situation
<details><summary>Show answer</summary>

The integrations I built or led were clearly more stable than those of the external teams,
so I shared what we did differently.

</details>

### B · Key point
<details><summary>Show answer</summary>

Developers often blame users: "they didn't read the documentation". I think that's a weak argument.
A good system expects people to make mistakes, and makes each error explicit.
It shows the error to the person who made it, so they can fix it themselves.

</details>

### B · Lesson
<details><summary>Show answer</summary>

Design for human error, not for perfect users.

</details>

---

### C. Email in three places: look at the system, not at the people

### C · Situation
<details><summary>Show answer</summary>

Email settings in our system were configured in three different places.
Almost everyone had forgotten about the third one.
Emails didn't work, and nobody understood why, because according to the settings everything was correct.

</details>

### C · Problem
<details><summary>Show answer</summary>

A developer found the third place in the debugger and told the configuration team:
it's your mistake, fix it here, here and here.

</details>

### C · Action
<details><summary>Show answer</summary>

I explained, calmly, that this wasn't a support mistake but a design problem.
The same configuration in three places will always lead to unclear behaviour.

</details>

### C · Lesson
<details><summary>Show answer</summary>

When something keeps going wrong, look at the system, not at the people.

</details>

---

### D. Robustness now or later: disagreeing with my manager

### D · Situation
<details><summary>Show answer</summary>

My manager and I often disagreed on one thing: whether to build extra robustness now,
like validations and checks for future maintenance, or later.
Sometimes these checks were more work than the feature itself.

</details>

### D · Both sides
<details><summary>Show answer</summary>

I wanted it now. He was limited by the budget, and I understood him: for the company, features come first.

</details>

### D · Resolution
<details><summary>Show answer</summary>

We agreed to plan the improvements into the next yearly update, when there was maintenance budget for them.

</details>

### D · Lesson
<details><summary>Show answer</summary>

I say clearly what I think, with arguments.
When the decision is made, I commit to it and find the right moment for the improvement.

</details>

---

### E. Two versions of one component: a conflict, and what I learned

### E · Situation
<details><summary>Show answer</summary>

We needed to run two versions of one component side by side: the old one for old jobs, the new one for new jobs.
Technically it was a simple routing rule in the Apache configuration, managed in Ansible.

</details>

### E · Problem
<details><summary>Show answer</summary>

A colleague from IT tried, it didn't work, and he refused.
He wanted us to rename packages and Maven coordinates, so it would look like a completely separate component.

</details>

### E · Action
<details><summary>Show answer</summary>

I explained that rebuilding one component as two, just for routing, was the wrong solution.
When we couldn't agree, I wrote to my manager with the facts. He understood and made it happen.

</details>

### E · Reflection
<details><summary>Show answer</summary>

Looking back, I was right on the technical side, but not in how I said it.
I told him his position was absurd, and our relationship was difficult afterwards.
Today I'd first offer to sit down together and write the configuration, and only then go to the manager.

</details>

*Сила этой истории — в последней карточке. Без неё история звучит как «я был прав, а он нет». Это и запасная история про ошибку.*

---

### F. A shared REST standard: convincing with facts

### F · Situation
<details><summary>Show answer</summary>

Different teams used different REST clients. I proposed a shared approach:
we define the Jakarta REST interface once.
The server implements it, and the client uses a RESTEasy proxy built from the same interface,
so a remote call is just a method call. In our integrations it made development much faster.

</details>

### F · Discussion
<details><summary>Show answer</summary>

Another team preferred a reactive library, because it allows non-blocking calls.
But when I looked at their code, every call blocked and waited for the result.
So I asked: in which project did we actually use the non-blocking part?

</details>

### F · Lesson
<details><summary>Show answer</summary>

I want technical choices to be based on how we really use a tool, not on what it promises.

</details>

*Не рассказывайте, что коллега не смог ответить. Это звучит как победа над человеком. Ваша цель — решение на фактах.*

---

### G · How do you work with non-technical people?
<details><summary>Show answer</summary>

I translate. I explain the limits of a request in business terms: time, cost, risk.
And I never just say no; I offer an alternative.
For example, instead of a one-gigabyte export that takes 40 minutes,
regular small updates with only the fields the customer needs.

</details>

### H · How would you describe your communication style?
<details><summary>Show answer</summary>

I'm calm and focused. I'm not the loudest person in the room, and I don't do much small talk.
But when there's a complex issue with many dependencies, I'm the one who explains it clearly
to the customer, the developers and the project manager, with facts.

</details>

*Это ваш честный портрет без «Russian bear». Подробнее о слабых сторонах — в теме 6.*

### I · How do you keep learning?
<details><summary>Show answer</summary>

In IT everything changes all the time, so learning never stops.
My personal method is to interview myself: I take a topic and ask myself questions about it, again and again.
If I can't explain it, I don't know it yet.
I learned this from watching how doctors in medical series discuss hard cases over and over.
And I enjoy sharing what I learn.

</details>

*Если сериал кажется лишним — уберите фразу про врачей. Метод и без неё звучит хорошо.*

### J · What did you learn in an international team?
<details><summary>Show answer</summary>

Working in an international team changed how I work.
I learned to share knowledge openly, to disagree politely and with respect,
and not to build my authority at the expense of others.
I've worked with very strong colleagues and with weaker ones.
With everyone, my job is to help them see the problem, without blaming and without talking down.

</details>

*Здесь не нужно рассказывать, каким вы были раньше. Только какой вы сейчас.*
