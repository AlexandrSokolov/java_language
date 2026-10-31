# Topic 4 — Challenges & Complicated Tasks I Managed

*Как пользоваться: в каждой истории четыре ключевых слова — Situation → Problem → Action → Result, и в конце одна фраза-вывод (Lesson). Учите цепочку и вывод, остальное рассказывайте своими словами. Одна история — 60–90 секунд. Ответы — на английском, пояснения — по-русски.*

*Карточки: прочитайте заголовок, ответьте вслух, потом откройте и сверьтесь.*

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

## 2. Цепочки

Сначала запомните цепочку и вывод каждой истории. Если сбились: вспомните следующее звено, и рассказ продолжится.

| История | Цепочка | Вывод (Lesson) |
|---|---|---|
| **A. Webhooks** | external teams → synchronous handlers → lost and duplicate messages → I redesigned it → recovery built in | *Acknowledge fast, process asynchronously, and always have a way to recover what was missed.* |
| **B. The missing header** | one customer, one feature broken → support stuck → I logged the traffic → their security filter | *When nobody can find the cause, look at what actually goes over the wire.* |
| **C. The time zone bug** | missing events → product team said "not a bug" → two log lines side by side → accepted | *Don't argue, show evidence, and never blame.* |
| **D. JasperReports** | complex report promised → almost no experience → learned while building → delivered | *I can learn a new tool fast, to a serious level, when the deadline is real.* |
| **E. The logging wrapper** | slow communication with the product team → nobody asked → I planned it myself → hours became minutes | *Small tools that save time every week are worth planning in.* |
| **F. The one-gigabyte SOAP call** | customer wants data → PM proposes a full dump → I pushed back → incremental updates | *Find the real need behind the request first.* |
| **G. Too optimistic estimates** | I trusted the published APIs → blocked and late → recorded every blocker → buffer and early warnings | *Trust, but verify, and make the cost of surprises visible.* |

---

## 3. The stories — cards

### A. Webhooks: messages lost and duplicated

### A · Situation
<details><summary>Show answer</summary>

In the last years, part of the development went to external teams, working under time pressure.
Their webhook handlers ran the whole logic synchronously, before answering the sender.

</details>

### A · Problem
<details><summary>Show answer</summary>

So the sender waited and timed out. Messages were lost or came twice, and jobs ended up in the wrong state.
I was asked to fix it.

</details>

### A · Action: the redesign
<details><summary>Show answer</summary>

I redesigned it. First, acknowledge right away and process asynchronously.
Second, collapse several events for the same job into one,
because what matters is the job's current state, not every single event.
Third, put the work into a queue.

</details>

### A · Action: recovery built in
<details><summary>Show answer</summary>

And because anything can fail, a reconciliation job asks the source system which jobs are still waiting,
and checks the audit service for what changed. So anything that was missed gets processed again.

</details>

### A · Result
<details><summary>Show answer</summary>

After that, lost and duplicated messages stopped being a recurring problem.

</details>

### A · Lesson
<details><summary>Show answer</summary>

Acknowledge fast, process asynchronously, and always have a way to recover what was missed.

</details>

### A · Follow-up: your queue was in memory. What about a restart?
<details><summary>Show answer</summary>

We lose the queue, but nothing is lost for good. The job status in the source system is the source of truth.
Reconciliation, plus the platform's audit service with the history of changes,
finds everything that was missed and processes it again.
For a few external systems the state lived only in memory.
If I built that today, I'd keep it in a database table or in Kafka.

</details>

*Тот же ответ есть в Topic 2.*

---

### B. The missing header: a problem nobody could find

### B · Situation
<details><summary>Show answer</summary>

At one customer, a feature in our product's web interface simply stopped working. Only at that customer.
It had nothing to do with our integrations.

</details>

### B · Problem
<details><summary>Show answer</summary>

Support couldn't find anything. Everything looked fine on our side, and nobody knew where to look.

</details>

### B · Action
<details><summary>Show answer</summary>

I switched on request and response logging at the Apache level,
to see exactly what came from the customer and what went back.
A JavaScript library in the product used a custom HTTP header, and I saw that the header never arrived.
The customer's security filters removed custom headers they didn't expect.

</details>

### B · Result
<details><summary>Show answer</summary>

The cause was finally clear, and I could prove it: it was on their side, not in our product.

</details>

### B · Lesson
<details><summary>Show answer</summary>

When nobody can find the cause, look at what actually goes over the wire.

</details>

---

### C. The time zone bug: convincing another team

### C · Situation
<details><summary>Show answer</summary>

We stopped receiving some events, and the audit service, which the product team maintained for us,
stopped returning some records.

</details>

### C · Problem
<details><summary>Show answer</summary>

I found the cause in the product: in the same module, one place used UTC,
and another used local time with a time zone. So time-based queries missed records.
The developers didn't accept it as a bug at first.

</details>

### C · Action
<details><summary>Show answer</summary>

I didn't argue opinions. I put two log lines side by side:
the same kind of object, logged in two different time formats. Here it's right, here it's wrong.

</details>

### C · Result
<details><summary>Show answer</summary>

With the evidence side by side, there was nothing left to discuss.

</details>

### C · Lesson
<details><summary>Show answer</summary>

Don't argue, show evidence, and never blame. The goal is to fix the system, not to win.

</details>

*Это частая ситуация в вашей работе. Можно добавить: «Job Manager had many bugs like this, so proving them clearly became part of my job.»*

---

### D. JasperReports: learning fast under a deadline

### D · Situation
<details><summary>Show answer</summary>

Our project managers promised a customer a very complex report: nested tables, images, a polished layout.

</details>

### D · Problem
<details><summary>Show answer</summary>

I had almost no experience with JasperReports, and the deadline was real.

</details>

### D · Action
<details><summary>Show answer</summary>

I learned it while building it. The whole project took about a month.
The first two weeks were the hardest: very intensive learning, a lot of trial and error,
until I understood how nested structures and images work in Jasper.

</details>

### D · Result
<details><summary>Show answer</summary>

I delivered it, and kept improving it afterwards.

</details>

### D · Lesson
<details><summary>Show answer</summary>

I can learn a new tool fast, to a serious level, when the deadline is real.

</details>

---

### E. The logging wrapper: initiative

### E · Situation
<details><summary>Show answer</summary>

When a product release broke one of the APIs we used,
we had to show the product team exactly what we sent and what we got back.

</details>

### E · Problem
<details><summary>Show answer</summary>

Normal request logging on a live system gives you a huge amount of information you can't filter.
And when a response couldn't be deserialized, you got a stack trace, not the actual data.

</details>

### E · Action: nobody asked
<details><summary>Show answer</summary>

Nobody asked me to fix it. I found the time between projects, explained why it was worth it,
and planned it myself.

</details>

### E · Action: the wrapper
<details><summary>Show answer</summary>

I built a thin wrapper around our RESTEasy client: you switch on debug logging for one package
and see only those requests and responses, as readable JSON.
When deserialization fails, the raw response goes to the log, so you see the real error.

</details>

### E · Result
<details><summary>Show answer</summary>

I could send the exact raw request to the product team in minutes, and they could test it right away.
We published it as a library to Nexus and reused it across projects.

</details>

### E · Lesson
<details><summary>Show answer</summary>

Small tools that save time every week are worth planning in, even when nobody asks for them.

</details>

---

### F. The one-gigabyte SOAP call: pushing back on a requirement

### F · Situation
<details><summary>Show answer</summary>

A customer wanted certain information from our system.

</details>

### F · Problem
<details><summary>Show answer</summary>

The project manager proposed an existing SOAP call that returned almost the whole database:
30 to 40 minutes and around a gigabyte per call.

</details>

### F · Action
<details><summary>Show answer</summary>

I pushed back and explained why: too slow, too heavy, and fragile.
I proposed regular incremental updates with only the fields the customer actually needed.

</details>

### F · Result
<details><summary>Show answer</summary>

The customer got the data they needed, without the heavy full export.

</details>

### F · Lesson
<details><summary>Show answer</summary>

Find the real need behind the request first, then choose the technical solution.

</details>

---

### G. Too optimistic estimates: my mistake

*Главное правило этой истории: ошибка — ваш оптимизм, а не баги продукта. Если звучит «виноват продукт», интервьюер слышит «виноваты другие».*

### G · Situation
<details><summary>Show answer</summary>

We built integrations on top of the product's REST services, with fixed budgets and fixed estimates.

</details>

### G · Mistake
<details><summary>Show answer</summary>

My mistake was optimism. When a service was published, I assumed it worked as documented.
More than once it didn't: it was missing or broken, and we were blocked and late.
It wasn't a lack of knowledge. I simply trusted the product more than I should have.

</details>

### G · Action
<details><summary>Show answer</summary>

I took it seriously. We started recording every blocker and how much time it cost,
often one or two days each.

</details>

### G · Result
<details><summary>Show answer</summary>

Now I plan a buffer for surprises, based on those real numbers, not on hope.
And I tell the project manager about a blocker as soon as it appears, not at the deadline.

</details>

### G · Lesson
<details><summary>Show answer</summary>

Trust, but verify, and make the cost of surprises visible.

</details>

---

## 4. How to tell any story

- **Start with one sentence of context**, not with the technology.
- **Say "I" for your part**, "we" for the team's result.
- **End with the lesson.** It shows you learned something, and it signals that the answer is finished.
- **Stop after 90 seconds.** If they want more, they'll ask. Good phrase to end on: *"I can go into more technical detail if that's useful."*
