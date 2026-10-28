# Topic 5 — How I Work with People

*Как пользоваться: раздел 1 — общий ответ цепочкой на «How do you work with others?». Раздел 2 — какую историю на какой вопрос. Раздел 3 — истории. Ответы — на английском, пояснения — по-русски.*

---

## 1. "How do you work with other people?" (about 60 seconds)

**Цепочка:** Learn → Teach → Share → No blame → Disagree → Commit → Tone

> **[Learn]** I keep learning by asking myself questions. I regularly interview myself on topics, because if I don't think about something, I forget it.
>
> **[Teach]** I teach the same way. With a junior, we worked in pairs, and I asked him simple but fundamental questions.
>
> **[Share]** With other teams, I shared what made our integrations stable.
>
> **[No blame]** My key principle: a good system expects people to make mistakes, and shows each error clearly to the person who made it. Blaming someone for not reading the documentation is not enough.
>
> **[Disagree → Commit]** I disagree with facts, never with blame. And when the budget says no, I accept the decision and plan the improvement for later.
>
> **[Tone]** And I learned that how you say something matters as much as being right.

---

## 2. Какую историю на какой вопрос

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

## 3. The stories

### A. The junior: teaching through questions

**Chain:** pair work → simple fundamental questions → he studied at home → he explained it back

> **[Situation]** We hired a junior developer who really wanted to work with us, and we worked in pairs.
>
> **[Approach]** I believe learning means answering questions again and again; otherwise you forget. So while we worked, I asked simple but fundamental questions. For example, when we used collections: what is a hash for? Why do we need both `hashCode` and `equals`?
>
> **[Result]** He went home, read about it, and the next day he explained it back to me. That's the best sign that someone has understood: they can explain it.
>
> **[Lesson]** I teach through questions, not lectures.

*Не говорите, что ему было стыдно. Скажите «he was motivated to close the gaps».*

---

### B. Design for human error: sharing with other teams

**Chain:** our integrations were more stable → I shared why → people will make mistakes → make each error visible

> **[Situation]** The integrations I built or led were clearly more stable than those of the external teams, so I shared what we did differently.
>
> **[Key point]** Developers often blame users: "they didn't read the documentation". I think that's a weak argument. A good system expects people to make mistakes, and makes each error explicit. It shows the error to the person who made it, so they can fix it themselves.
>
> **[Lesson]** Design for human error, not for perfect users.

---

### C. Email in three places: look at the system, not at the people

**Chain:** emails don't work → a developer blames the config team → I explained → a design problem

> **[Situation]** Email settings in our system were configured in three different places. Almost everyone had forgotten about the third one. Emails didn't work, and nobody understood why, because according to the settings everything was correct.
>
> **[Problem]** A developer found the third place in the debugger and told the configuration team: it's your mistake, fix it here, here and here.
>
> **[Action]** I explained, calmly, that this wasn't a support mistake but a design problem. The same configuration in three places will always lead to unclear behaviour.
>
> **[Lesson]** When something keeps going wrong, look at the system, not at the people.

---

### D. Robustness now or later: disagreeing with my manager

**Chain:** I wanted robustness now → he had a budget → I understood → planned for the next update

> **[Situation]** My manager and I often disagreed on one thing: whether to build extra robustness now, like validations and checks for future maintenance, or later. Sometimes these checks were more work than the feature itself.
>
> **[Both sides]** I wanted it now. He was limited by the budget, and I understood him: for the company, features come first.
>
> **[Resolution]** We agreed to plan the improvements into the next yearly update, when there was maintenance budget for them.
>
> **[Lesson]** I say clearly what I think, with arguments. When the decision is made, I commit to it and find the right moment for the improvement.

---

### E. Two versions of one component: a conflict, and what I learned

**Chain:** a simple routing rule → IT refused → I escalated with facts → right on substance, wrong in tone

> **[Situation]** We needed to run two versions of one component side by side: the old one for old jobs, the new one for new jobs. Technically it was a simple routing rule in the Apache configuration, managed in Ansible.
>
> **[Problem]** A colleague from IT tried, it didn't work, and he refused. He wanted us to rename packages and Maven coordinates, so it would look like a completely separate component.
>
> **[Action]** I explained that rebuilding one component as two, just for routing, was the wrong solution. When we couldn't agree, I wrote to my manager with the facts. He understood and made it happen.
>
> **[Reflection]** Looking back, I was right on the technical side, but not in how I said it. I told him his position was absurd, and our relationship was difficult afterwards. Today I'd first offer to sit down together and write the configuration, and only then go to the manager.

*Сила этой истории — в последнем абзаце. Без него она звучит как «я был прав, а он нет». Это и запасная история про ошибку.*

---

### F. A shared REST standard: convincing with facts

**Chain:** different clients in every team → one interface for server and client → the reactive argument → choose by real use

> **[Situation]** Different teams used different REST clients. I proposed a shared approach: we define the Jakarta REST interface once. The server implements it, and the client uses a RESTEasy proxy built from the same interface, so a remote call is just a method call. In our integrations it made development much faster.
>
> **[Discussion]** Another team preferred a reactive library, because it allows non-blocking calls. But when I looked at their code, every call blocked and waited for the result. So I asked: in which project did we actually use the non-blocking part?
>
> **[Lesson]** I want technical choices to be based on how we really use a tool, not on what it promises.

*Не рассказывайте, что коллега не смог ответить. Это звучит как победа над человеком. Ваша цель — решение на фактах.*

---

### G. Business people: translate, and offer an alternative

> I translate. I explain the limits of a request in business terms: time, cost, risk. And I never just say no; I offer an alternative. For example, instead of a one-gigabyte export that takes 40 minutes, regular small updates with only the fields the customer needs.

---

### H. Calm and focused: my communication style

> I'm calm and focused. I'm not the loudest person in the room, and I don't do much small talk. But when there's a complex issue with many dependencies, I'm the one who explains it clearly to the customer, the developers and the project manager, with facts.

*Это ваш честный портрет без «Russian bear». Подробнее о слабых сторонах — в теме 6.*

---

### I. Interviewing myself: how I keep learning

> In IT everything changes all the time, so learning never stops. My personal method is to interview myself: I take a topic and ask myself questions about it, again and again. If I can't explain it, I don't know it yet. I learned this from watching how doctors in medical series discuss hard cases over and over. And I enjoy sharing what I learn.

*Если сериал кажется лишним — уберите фразу про врачей. Метод и без неё звучит хорошо.*

---

### J. A new work culture: what I learned

> Working in an international team changed how I work. I learned to share knowledge openly, to disagree politely and with respect, and not to build my authority at the expense of others. I've worked with very strong colleagues and with weaker ones. With everyone, my job is to help them see the problem, without blaming and without talking down.

*Здесь не нужно рассказывать, каким вы были раньше. Только какой вы сейчас.*
