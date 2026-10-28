# Topic 4 — Challenges & Complicated Tasks I Managed

*Как пользоваться: в каждой истории четыре ключевых слова — Situation → Problem → Action → Result, и в конце одна фраза-вывод (Lesson). Учите ключевые слова и вывод, остальное рассказывайте своими словами. Одна история — 60–90 секунд. Ответы — на английском, пояснения — по-русски.*

---

## 1. Какую историю на какой вопрос

| Если спрашивают про… | История |
|---|---|
| a difficult technical problem · the most complex thing you built | **A. Webhooks** |
| debugging · troubleshooting · a problem nobody could solve | **B. The missing header** |
| disagreement · conflict with another team · convincing people | **C. The time zone bug** |
| learning something new fast · pressure · a tight deadline | **D. JasperReports** |
| initiative · going beyond your role · improving how the team works | **E. The logging wrapper** |
| pushing back on a requirement | **F. The one-gigabyte SOAP call** |
| a mistake · a failure · something you'd do differently | **G. Too optimistic estimates** |

---

## 2. The stories

### A. Webhooks: messages lost and duplicated

**Chain:** external teams → synchronous handlers → lost and duplicate messages → I redesigned it → recovery built in

> **[Situation]** In the last years, part of the development went to external teams, working under time pressure. Their webhook handlers ran the whole logic synchronously, before answering the sender.
>
> **[Problem]** So the sender waited and timed out. Messages were lost or came twice, and jobs ended up in the wrong state. I was asked to fix it.
>
> **[Action]** I redesigned it. First, acknowledge right away and process asynchronously. Second, collapse several events for the same job into one, because what matters is the job's current state, not every single event. Third, put the work into a queue. And because anything can fail, a reconciliation job asks the source system which jobs are still waiting, and checks the audit service for what changed. So anything that was missed gets processed again.
>
> **[Result]** After that, lost and duplicated messages stopped being a recurring problem.
>
> **[Lesson]** Acknowledge fast, process asynchronously, and always have a way to recover what was missed.

**Likely follow-up:** *"Your queue was in memory — what about a restart?"* → ответ есть в Topic 2, раздел 4.

---

### B. The missing header: a problem nobody could find

**Chain:** one customer, one feature broken → support stuck → I logged the traffic → their security filter

> **[Situation]** At one customer, a feature in our product's web interface simply stopped working. Only at that customer. It had nothing to do with our integrations.
>
> **[Problem]** Support couldn't find anything. Everything looked fine on our side, and nobody knew where to look.
>
> **[Action]** I switched on request and response logging at the Apache level, to see exactly what came from the customer and what went back. A JavaScript library in the product used a custom HTTP header, and I saw that the header never arrived. The customer's security filters removed custom headers they didn't expect.
>
> **[Result]** The cause was finally clear, and I could prove it: it was on their side, not in our product.
>
> **[Lesson]** When nobody can find the cause, look at what actually goes over the wire.

---

### C. The time zone bug: convincing another team

**Chain:** missing events → product team said "not a bug" → two log lines side by side → accepted

> **[Situation]** We stopped receiving some events, and the audit service, which the product team maintained for us, stopped returning some records.
>
> **[Problem]** I found the cause in the product: in the same module, one place used UTC, and another used local time with a time zone. So time-based queries missed records. The developers didn't accept it as a bug at first.
>
> **[Action]** I didn't argue opinions. I put two log lines side by side: the same kind of object, logged in two different time formats. Here it's right, here it's wrong.
>
> **[Result]** With the evidence side by side, there was nothing left to discuss.
>
> **[Lesson]** Don't argue, show evidence, and never blame. The goal is to fix the system, not to win.

*Это частая ситуация в вашей работе. Можно добавить: «Job Manager had many bugs like this, so proving them clearly became part of my job.»*

---

### D. JasperReports: learning fast under a deadline

**Chain:** complex report promised → almost no experience → learned while building → delivered

> **[Situation]** Our project managers promised a customer a very complex report: nested tables, images, a polished layout.
>
> **[Problem]** I had almost no experience with JasperReports, and the deadline was real.
>
> **[Action]** I learned it while building it. The whole project took about a month, and the first two weeks were the hardest: understanding how nested structures and images work in Jasper.
>
> **[Result]** I delivered it, and kept improving it afterwards.
>
> **[Lesson]** I can learn a new tool fast, to a serious level, when the deadline is real.

---

### E. The logging wrapper: initiative

**Chain:** slow communication with the product team → nobody asked → I planned it myself → hours became minutes

> **[Situation]** When a product release broke one of the APIs we used, we had to show the product team exactly what we sent and what we got back.
>
> **[Problem]** Normal request logging on a live system gives you a huge amount of information you can't filter. And when a response couldn't be deserialized, you got a stack trace, not the actual data.
>
> **[Action]** Nobody asked me to fix it. I found the time between projects, explained why it was worth it, and planned it myself. I built a thin wrapper around our RESTEasy client: you switch on debug logging for one package and see only those requests and responses, as readable JSON. When deserialization fails, the raw response goes to the log, so you see the real error.
>
> **[Result]** I could send the exact raw request to the product team in minutes, and they could test it right away. We published it as a library to Nexus and reused it across projects.
>
> **[Lesson]** Small tools that save time every week are worth planning in, even when nobody asks for them.

---

### F. The one-gigabyte SOAP call: pushing back on a requirement

**Chain:** customer wants data → PM proposes a full dump → I pushed back → incremental updates

> **[Situation]** A customer wanted certain information from our system.
>
> **[Problem]** The project manager proposed an existing SOAP call that returned almost the whole database: 30 to 40 minutes and around a gigabyte per call.
>
> **[Action]** I pushed back and explained why: too slow, too heavy, and fragile. I proposed regular incremental updates with only the fields the customer actually needed.
>
> **[Result]** The customer got the data they needed, without the heavy full export.
>
> **[Lesson]** Find the real need behind the request first, then choose the technical solution.

---

### G. Too optimistic estimates: my mistake

**Chain:** I trusted the published APIs → blocked and late → recorded every blocker → buffer and early warnings

*Главное правило этой истории: ошибка — ваш оптимизм, а не баги продукта. Если звучит «виноват продукт», интервьюер слышит «виноваты другие».*

> **[Situation]** We built integrations on top of the product's REST services, with fixed budgets and fixed estimates.
>
> **[Mistake]** My mistake was optimism. When a service was published, I assumed it worked as documented. More than once it didn't: it was missing or broken, and we were blocked and late. It wasn't a lack of knowledge. I simply trusted the product more than I should have.
>
> **[Action]** I took it seriously. We started recording every blocker and how much time it cost, often one or two days each.
>
> **[Result]** Now I plan a buffer for surprises, based on those real numbers, not on hope. And I tell the project manager about a blocker as soon as it appears, not at the deadline.
>
> **[Lesson]** Trust, but verify, and make the cost of surprises visible.

---

## 3. How to tell any story

- **Start with one sentence of context**, not with the technology.
- **Say "I" for your part**, "we" for the team's result.
- **End with the lesson.** It shows you learned something, and it signals that the answer is finished.
- **Stop after 90 seconds.** If they want more, they'll ask. Good phrase to end on: *"I can go into more technical detail if that's useful."*
