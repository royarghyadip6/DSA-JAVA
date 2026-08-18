# 3rd Round — Manager Interview (5–8 Years, Java Backend)

This file is **only for the manager / hiring-manager round**.

Round 1 (Java, Spring, SQL, coding) and Round 2 (microservices, Kafka, AWS, design) are separate files. HR (salary, offer, documents) is next.

At 5–8 years they are not hiring a junior who “completes tickets”. They want a person who **owns delivery, unblocks others, talks to QA and support, and does not hide bad news**.

**What this round usually is (30–60 minutes)**

1. “Tell me about yourself” — 90 seconds to 2 minutes
2. Project ownership and impact (not HashMap internals)
3. People: conflict, mentoring, stakeholders, pushback
4. Delivery: estimate, Agile, quality vs speed, production
5. Light technical a manager still asks (testing, microservices cost, DevOps)
6. Motivation: why leave, why us, notice period, location / WFO
7. “Do you have questions for me?”

**How to use this file**

- All answers are **open**. Practise out loud.
- Use **STAR**: Situation → Task → Action → Result. Keep each story **60–90 seconds**.
- Replace the numbers in `[brackets]` with **your real** numbers before the interview. Fake metrics get caught.
- Do **not** repeat Round 2 architecture. One diagram sentence is enough. Then talk **impact, risk, people**.
- Never blame a named colleague. Blame a missing process, then say what you changed.

**Study time: about 3–4 hours**

| Time | What to finish |
|---|---|
| 0.5h | 2-minute intro + ESM manager pitch |
| 1.0h | Six STAR stories (speak them) |
| 0.75h | People, conflict, mentoring |
| 0.5h | Delivery, Agile, quality, production |
| 0.5h | Light technical for managers |
| 0.5h | Why leave / why us / NP / questions to ask |

---

## Table of contents

1. [Opening](#1-opening)
2. [Project ownership (manager view of ESM)](#2-project-ownership-manager-view-of-esm)
3. [Six STAR stories you must have](#3-six-star-stories-you-must-have)
4. [People, conflict, mentoring](#4-people-conflict-mentoring)
5. [Delivery, Agile, quality](#5-delivery-agile-quality)
6. [Production, on-call, stakeholders](#6-production-on-call-stakeholders)
7. [Decisions, trade-offs, tech debt](#7-decisions-trade-offs-tech-debt)
8. [Light technical (managers still ask this)](#8-light-technical-managers-still-ask-this)
9. [Motivation, switch, logistics](#9-motivation-switch-logistics)
10. [Scenario questions](#10-scenario-questions)
11. [Questions you should ask them](#11-questions-you-should-ask-them)
12. [Red flags in your answers](#12-red-flags-in-your-answers-do-not-do-these)
13. [Night-before checklist](#night-before-checklist-round-3)
14. [How to talk in Round 3](#how-to-talk-in-round-3)

---

## 1. Opening

---

### Q. Tell me about yourself.

**Answer (about 90 seconds — memorise the shape, not every word)**

> I am a Java backend developer with **[X] years** of experience. I build and support services using Java, Spring, Oracle/SQL, Kafka, and CI/CD.
>
> Right now I work on **ESM — Ethernet Service Management** — inside Nokia **WS-NOC**. Operators use it to create and manage carrier Ethernet services. My work is the backend: REST APIs, service lifecycle, database, and the Kafka path to the device adapter. I also debug production issues with logs, SQL, and Kafka traces.
>
> Before that I worked on **[earlier project, e.g. TRIMS / feature services]** with Java and Spring Boot, JUnit/Mockito, Jenkins, and OpenShift.
>
> I like work where I own a feature from design to lab to production, not only a small ticket in the middle. I am looking for a role where I can go deeper on backend systems and take more ownership of delivery in a team.

**Stop.** Let them ask the next question.

**Do not:** list every technology. Do not start with your 10th class. Do not say “I am a hard-working team player” with no proof.

---

### Q. Walk me through your resume / experience.

**Answer**

Speak **latest first**. For each job: product, your role, one result.

> Most recent: ESM at Nokia WS-NOC. I own backend work on service create/deploy, list screens, and southbound failures. Stack is Java, Spring, JPA, Oracle, Kafka.
>
> Before that: **[TCS / previous]** — about **[N]** years, **[2–3]** projects. Example: a Spring Boot service that automated a manual process in a legacy system. We used JUnit, Mockito, JaCoCo, Jenkins, OpenShift.
>
> I have also helped with **[mentoring / reviews / a side stack if true, e.g. a small UI task]** when the team needed it.

If they ask “only backend?”: “Yes, that is my core. I can read logs, SQL, and Kafka. I am not positioning myself as a full-time frontend or SRE.”

---

### Q. Describe your current role in one minute.

**Answer**

> I am an individual contributor on the ESM backend. I take features and defects from Jira, design the change, implement it, write or extend tests, get it through review, test on the lab, and support it after release. I work with QA, and with support when a create/deploy fails in the field. I am not a people manager. I do influence design and help juniors on the team.

---

### Q. What is your biggest strength? Biggest weakness?

**Strength (pick one, with proof)**

> I stay calm on production issues. I do not guess. I go logs → SQL / Kafka → then a fix or a rollback. Example: **[slow list page / timeout]** — we found **[N+1 / missing timeout / join]** instead of adding more hardware.

Other safe strengths: “I make work small enough to finish in a sprint.” “I write down how to test, so QA is not blocked.”

**Weakness (must be real + what you do about it)**

> I used to take too much on myself before asking for help. That delayed a lab issue by a day. Now I raise a blocker in standup if I am stuck more than a few hours, and I ping the adapter/NE owner early.

**Do not** say: “I am a perfectionist.” “I work too hard.” “I have no weakness.”

---

### Q. Where do you see yourself in 3–5 years?

**Answer**

> I want to stay technical. In 3 years I want to be the person the team trusts for backend design and production issues — a tech lead kind of IC, not necessarily a people manager on day one. In 5 years I am open to leading a small squad if that is the path here, but I do not want to leave coding completely.

If they only have a people-manager track, add: “I can grow into that if the team needs it. My first value is still delivering software well.”

---

## 2. Project ownership (manager view of ESM)

Round 2 was the drawing. Here they want **who you are in the team**.

---

### Q. Explain your current project to a manager (not an architect).

**Answer**

> ESM is the service in our NMS that lets operators design and deploy Ethernet services on the live network. My part is the Java backend.
>
> The operator action hits our APIs. We save the intent in Oracle. To configure the real device we send a Kafka request to an adapter; we do not talk to the box ourselves. If the device is slow or down, the operator must see a clear failure, not a hung screen.
>
> The hard parts of the job are: keeping NMS and the device in sync, list screens that join a lot of data, and debugging across four places — our app, Kafka, the adapter, and the device.

If they want business value: “If ESM is wrong or slow, the operator cannot provision customer services. That is revenue and SLA, not a toy CRUD app.”

---

### Q. What was *your* contribution? What did you own?

**Answer**

Be specific. Use this shape:

> I owned **[feature / defect area]**. Before: **[pain]**. I **[designed / implemented / tested on lab]**. After: **[result]**. I also **[reviews / runbook / helped QA]**.

Examples you can pick from real work (only if true):

- List page / alarm join performance
- Deploy/create failure when Kafka reply times out
- Sync mismatch between NMS and NE
- A security or authorizer check on a write API
- Lab bring-up / test automation support

**Do not** say “I worked on everything in ESM.” That sounds like you owned nothing.

---

### Q. Who do you interact with daily?

**Answer**

> Backend teammates and my lead for design and review. QA for test data and lab. Sometimes the adapter / SNA team when southbound fails. Support or a senior when a field issue is not reproducible. Product/PO for what “done” means. I use Jira for the work item and Slack/Teams for blockers.

---

### Q. What SDLC / process do you follow? Which tools?

**Answer**

> **Agile / Scrum** (or Kanban if that is true). Sprint of **[2 weeks]**. Jira for stories and bugs. Git + pull requests. Jenkins (or GitLab CI) for build. We demo or at least show QA on the lab before the release train.

Ceremonies I actually join: standup, planning, review, retro. I do not pretend I run the Scrum of Scrums if I do not.

---

### Q. How is success measured for you?

**Answer**

> Stories completed without coming back as production bugs. Lab pass for the flows I own. Review quality — I do not dump 2000-line PRs. When I am on a defect, time to find the cause and a safe fix. I also care that QA is not blocked waiting for me.

If they use OKRs: “I align to the team’s sprint goal. I do not invent personal OKRs in the interview.”

---

## 3. Six STAR stories you must have

Prepare **six**. Write your real names and dates in `[brackets]`. Practise until each is under 90 seconds.

---

### Story A — Production incident (they will ask)

**Question they use:** “Tell me about a production issue you handled.”

**STAR (fill in)**

- **S:** After a release / under load, **[list API / deploy / consumer]** became slow or failed. Users **[could not provision / page timed out]**.
- **T:** Restore service, then find the cause. Do not guess.
- **A:** Checked metrics or logs first (error rate, time, pool wait, Kafka lag). Rolled back or turned a flag off if we had one. Then found **[N+1 / missing timeout / lock / bad query]**. Added **[test / index / timeout / alarm]**.
- **R:** Service recovered in **[X minutes/hours]**. The same class of bug did not return. We added a **[dashboard / test]**.

**Line that sounds 5–8 YOE:** “The real gap was we learned from users first. I added an alarm so the next time we see it before they call.”

---

### Story B — You owned a feature end to end

**Question:** “Walk me through a feature you delivered.”

- **S:** Operators needed **[X]**. Old way was **[manual / slow / error-prone]**.
- **T:** Deliver in **[N sprints]**, including lab.
- **A:** Clarified acceptance with PO/QA. Split: DB expand first, then API, then southbound, then tests. Wrote “how to test” on the ticket. Sat with QA on the first lab run. Watched **[one metric]** after prod.
- **R:** Shipped in **[N]** sprints. **[Fewer tickets / faster provision]**. What I would do better: **[thinner slice first]**.

---

### Story C — You disagreed (design or process)

**Question:** “Tell me about a time you disagreed with someone.”

- **S:** A proposal was **[hold HTTP until the device answers / add another join on the list query / skip tests]**.
- **T:** Protect reliability without blocking the sprint for no reason.
- **A:** I did not say “you are wrong.” I showed **[thread pool / EXPLAIN / duplicate create]**. I offered a middle path: **[202 + status API / fetch in two queries / test on the happy path + one timeout]**.
- **R:** We shipped the safer option. Relationship stayed fine because I argued the **risk**, not the person.

---

### Story D — Tight deadline / you pushed back

**Question:** “A stakeholder wants it this Friday. What do you do?”

- **S:** Scope was **[full deploy + UI polish + extra NE type]**.
- **T:** Protect the date **or** protect quality — be honest which.
- **A:** I listed must-have vs later. I said: “Friday we can do A and B with lab on one NE type. C needs another sprint.” I put it in Jira so it was visible.
- **R:** We hit Friday with A+B. C did not leak as a prod incident. The manager trusted the date because I did not silently cut tests.

---

### Story E — Mentoring / helping a junior

**Question:** “How do you work with juniors?”

- **S:** A teammate was stuck on **[LazyInitializationException / Kafka timeout / flaky test]**.
- **T:** Unblock them and leave them stronger.
- **A:** I did not take the keyboard first. I asked them to explain the failure path. We found **[session closed / no timeout]**. I asked them to write the test. I reviewed the PR the same day.
- **R:** They shipped it. Next similar ticket they did without me. I did not create a dependency on me.

---

### Story F — You failed or got negative feedback

**Question:** “Tell me about a failure / feedback you received.”

- **S:** I **[under-estimated / missed a case / merged without a test]**. Impact: **[bug in QA / extra weekend]**.
- **T:** Fix the user pain, then the process.
- **A:** I owned it in standup. Hotfix or revert. Then I added **[checklist / test / smaller PR]**.
- **R:** Same miss did not happen again. Feedback from my lead: **[be louder earlier]**. I now raise risks in planning, not on the last day.

**Never:** “The QA missed it.” “The requirement was wrong” with no what *you* changed.

---

## 4. People, conflict, mentoring

---

### Q. How do you handle conflict with a teammate?

**Answer**

> I talk to them 1:1 first, not in a group chat roast. I state the impact: “This PR has no test for the timeout path, we have been burned by that.” If we still disagree, we take it to the lead with two options and a recommendation. I do not keep a silent war. I also do not escalate as the first step.

---

### Q. What if you disagree with your manager?

**Answer**

> I disagree once, with data, in private. If they still decide, I execute and I do not sabotage. If the decision is unsafe (security, data loss), I write the risk in the ticket so it is visible. I am not a hero who ignores the manager. I am also not a yes-person on production safety.

---

### Q. How do you review PRs?

**Answer**

> I review behaviour and risk, not commas. I ask: failure modes, transaction boundary, index for the new query, timeout on the new remote call, a test that would have failed before the fix. I do not block on style if the linter will fix it. Large PRs I ask to split. I review the same day if I can — delayed review is a hidden blocker.

---

### Q. Have you mentored anyone? How many?

**Answer**

Be honest.

> I have mentored **[N]** juniors / campus hires informally: pairing on first design, PR comments that explain *why*, and a short “how we debug ESM” note. I have not been a full-time people manager. At 5–8 years that is normal. I am ready to do more of it.

---

### Q. How do you work with QA?

**Answer**

> I write how to test on the ticket: data setup, expected device side-effect, negative cases (timeout, duplicate submit). I sit with QA on the first run of a southbound feature. When a bug is invalid I explain with evidence, not ego. When it is valid I take it. I never say “it works on my machine” as the last word.

---

### Q. How do you work with support / operations?

**Answer**

> I give a runbook: which log field, which SQL to check state, “do not retry more than N times.” When they page me I want the service id and time first. After the incident I add the missing log or alarm. I do not dump a 40-page design doc as a runbook.

---

### Q. Tell me about a time you influenced without authority.

**Answer**

Use Story C. Add: “I did not own the adapter team. I showed timeout metrics and the Tomcat thread dump. They agreed to a bounded retry. Influence was evidence plus a small ask.”

---

### Q. How do you handle a poor performer on your team? (they ask future leads this)

**Answer**

> I am not their manager, so I do not run a PIP. What I *do*: give clear PR feedback early, pair on one ticket, tell my lead with facts (“three regressions in two sprints, here are the PRs”), and offer to help. I do not gossip. I do not silently fix all their work — that hides the problem.

---

### Q. Cross-team / timezone / hybrid?

**Answer**

> I write decisions in the ticket, not only in a call. Overlap hours for design, async for status. If WFO is required I say my constraint honestly: “I can do **[N] days** in office” — do not promise 5 days if you cannot.

---

## 5. Delivery, Agile, quality

---

### Q. How do you estimate?

**Answer**

> I split by a **vertical slice** that works on the lab, not “all DTOs first.” If a task is more than 3–4 days, it is not split enough. Unknowns (new NE behaviour, dirty data) I call a **spike** with a time box. I give a range, not fake precision. If I slip, I say it when the unknown is resolved, not on the last day.

Story points: “We use Fibonacci. I compare to a story we already did. I do not fight for a 2 vs 3 for an hour.”

---

### Q. What do you do when requirements are unclear?

**Answer**

> I write my assumption on the ticket and a question list for the PO. I do not wait two days in silence. If they are unavailable I build the smallest slice behind a flag. I would rather show a wrong UI field early than code the wrong lifecycle for a week.

---

### Q. Speed vs quality? Technical debt?

**Answer**

> For a production hotfix I will do the smallest safe fix and a follow-up ticket for the real cleanup. For a feature I will not skip the timeout and the test. Debt I log with **cost of delay**: “this list query will not survive 10x rows.” I do not demand a rewrite. I ask for 10–20% of a sprint for the hottest debt.

---

### Q. How much unit testing? What coverage?

**Answer** (they asked this in a real manager round)

> I write unit tests for the logic I own — task/command behaviour with mocks for Kafka/DB. I do not chase 90% on getters. I care about **new code** and the failure paths (timeout, duplicate, empty list). JaCoCo/Sonar on new code is the gate I like. On ESM, southbound timing still needs a lab test; coverage % will never replace that.

If they insist on a number: “On my last Spring Boot service we aimed **[70–80%]** on new code, not on generated code.” Use the real number if you have it.

---

### Q. Definition of done?

**Answer**

> Code reviewed, tests added, Sonar/quality gate green, QA verified on lab for the flow, logging/metric if it is a new failure mode, Jira updated. “Done” is not “I pushed a branch.”

---

### Q. How do you prioritise when everything is P1?

**Answer**

> I ask: is a customer stuck now? Is there data loss? Is there a workaround? Production restore beats a new feature. Among features I ask the PO. I do not silently pick my favourite ticket. I make the queue visible.

---

### Q. How do you handle blockers?

**Answer**

> Standup is for blockers, not a diary. If I am blocked on another team I ping with a deadline: “Need the snaId mapping by 3pm or we slip the lab.” I try a stub or a mock so my tests still move.

---

### Q. Documentation — too much or too little?

**Answer**

> I document the **decision and the failure path**, not every getter. Ticket + a short README or runbook. Design of more than two days gets a one-pager: options, choice, risk. Wiki that nobody updates is worse than Jira comments.

---

## 6. Production, on-call, stakeholders

---

### Q. Are you OK with on-call?

**Answer**

> Yes, if the rotation is fair and we have runbooks. I have debugged production using logs, SQL, and Kafka. I will not pretend I enjoy 3 a.m., but I will own it. After a page I want a blameless note: what we missed.

If you have never been on a formal roster: “I have handled production defects during business hours and lab outages. I have not been primary on-call 24/7. I am willing to learn the roster here.”

---

### Q. A customer is angry. The issue is not in your code. What do you do?

**Answer**

> I do not say “not my module” to the customer. I stay on the call, gather service id and time, help find whose component failed, and I do not leave until someone owns the next step. Internally I am clear about the boundary. Externally I am one company.

---

### Q. How do you explain a delay to a non-technical stakeholder?

**Answer**

> No jargon. “The device is not answering in time. If we hide that, the operator will think the service is up when it is not. We need two extra days for a timeout message and a retry that does not double-configure.” I give a date and what they will see in the UI.

---

### Q. How do you communicate bad news?

**Answer**

> Early, with impact and options. “We will miss Thursday. Options: drop the extra NE type, or move to next train. I recommend dropping scope.” I do not wait until Thursday evening.

---

## 7. Decisions, trade-offs, tech debt

---

### Q. A design trade-off you pushed?

**Answer**

Pick **one**. Structure: options → cost → choice → what you monitored.

> Option A: wait on HTTP until the network element finishes. Simple UI. Cost: Tomcat threads die if the box is slow.
>
> Option B: accept the request, return 202, configure over Kafka, operator polls status. Cost: UI must handle “in progress.”
>
> We chose B for NE config. We added a status API and a timeout alarm.

Other good trade-offs: extra Kafka topic vs two services coupled; denormalise a list view vs a heavy join; retry vs fail-fast.

---

### Q. Why this stack (Java, Spring, SQL, Kafka)?

**Answer**

> SQL is the source of truth for inventory and lifecycle — we need joins and transactions. Kafka is for slow devices and other services so we do not block the user thread. Spring is how the team ships. I would add Redis when a measured read path needs it, not on day one. I would not put the only copy of lifecycle state in a Kafka topic — operators need queries.

---

### Q. If you had a free month on ESM, what would you improve?

**Answer**

One real thing:

> Readiness that includes DB and Kafka. Bounded retry and a dead-letter path for poison southbound messages. Stop growing the list-page join. Optimistic lock on concurrent edit of the same service.

Do **not** say “rewrite in Spring Boot 3 and Kafka Streams.”

---

### Q. How do you stay updated?

**Answer**

> I read what we actually upgrade: Java LTS, Spring Boot, the Kafka client. I learn from production issues and from reviews. I do not claim I read every JEP. If I study, I study the next thing the project needs (for example virtual threads only if we have blocking I/O scale problems).

---

## 8. Light technical (managers still ask this)

They are not the Round 2 interviewer. Short answers. If they go deep, you already have Round 2.

---

### Q. Microservices — advantage and disadvantage (including deployment)?

**Answer**

> Advantage: a team can release the payment or inventory service without shipping the whole monolith. Scaling and failure isolation are better if boundaries are real.
>
> Disadvantage: more moving parts. Network failures, eventual consistency, harder end-to-end tests.
>
> **Deployment disadvantage:** more pipelines, more versions, more config, more “works in my service, fails in integration.” You need good CI, observability, and contract discipline. Independent deploy is a benefit **and** an ops cost. For a small team a modular monolith can be the better manager decision.

---

### Q. Why not one big application?

**Answer**

> If one team owns it and we release together, one deployable is simpler. We split when teams and release cycles really differ. ESM is already a service in a larger NMS — we did not split it into twenty nano-services.

---

### Q. Docker / Kubernetes / DevOps — do you have it?

**Answer** (from a real manager script)

> I am not a full-time SRE. I can write a Dockerfile, read a pipeline, and explain rolling vs blue-green. I have used **[Jenkins / OpenShift / GitLab CI / Docker]** in **[project]**. I check health endpoints and logs after deploy. I do not claim I run a production cluster alone.

---

### Q. Java 8 vs 11/17 — why should the org move?

**Answer**

> Java 8 can still run supported enterprise workloads, but migration is also driven by vendor support, security patches, runtime performance, and operational cost—not only library compatibility. Spring Boot 3 needs 17. Java 17 gives records and LTS; 21 gives virtual threads for suitable blocking-I/O workloads. I would plan and test an LTS migration rather than upgrade for fashion or remain on an unpatched runtime.

---

### Q. How do you ensure quality without slowing the sprint?

**Answer**

> Small PRs, tests on the failure path, Sonar on **new** code, QA on the lab for southbound. I do not add a week of architecture theatre to a three-point bug. I do not skip the timeout test to save an hour.

---

### Q. Security — how do you think about it as a developer?

**Answer**

> Check **object** permission, not only “user is logged in.” Parameterised SQL. No secrets in Git. ESM uses RBAC / authorizer on writes. I would not open an internal API just because it is “inside the network.”

---

## 9. Motivation, switch, logistics

Keep this honest. HR will repeat some of it. The manager is testing **stability and attitude**.

---

### Q. Why are you leaving / why are you looking?

**Answer (safe, true patterns — pick yours)**

> I have learned a lot on ESM / my current product. I want a role with **[broader backend ownership / more modern Spring Boot / more product-facing delivery / a domain I want to grow in]**. It is not about running from a person. I have given/will give a proper handover.

**Do not:** insult your manager. Do not say “I am bored” with no example. Do not say “only for money” as the first sentence (salary can be second).

---

### Q. Why this company / this role?

**Answer**

Research **one** real thing: product, domain, scale, or engineering blog.

> You run **[product]** at **[scale / domain]**. My background is Java services with data and async integration. I can contribute from month one on backend delivery, and I want to learn **[their cloud / domain]**.

---

### Q. Why should we hire you?

**Answer**

> You get someone who has taken Java services to lab and production, who debugs across API, DB, and messaging, and who will not disappear when QA or support needs a partner. I am at the level where I can mentor juniors without being a full manager yet. That is usually what a 5–8 year seat needs.

---

### Q. Why did you resign without an offer? / Why a gap? (only if true)

**Answer**

> I **[reason: health / family / toxic hours / visa — pick true]**. I used the time to **[study / project / caregiving]**. I am ready to join and I am not shopping without focus. I understand the market risk; that is why I am being selective now.

Do not invent a heroic story.

---

### Q. Notice period — is it negotiable?

**Answer**

> My NP is **[60/90] days**. I will try to negotiate with my current employer after an offer, but I cannot promise a buyout. I will not leave a team without handover. If you need someone in 2 weeks, I may not be the right hire and I would rather say that now.

---

### Q. Any issue with WFO / location / shift?

**Answer**

Honest constraint.

> I can do **[hybrid: N days]**. Relocation: **[yes / need N months]**. Night shift: **[ok for on-call rotation / not as a permanent IST night job]**.

If you say yes to everything you cannot do, you will fail in month two and they will remember.

---

### Q. Are you interviewing elsewhere? How soon can you join?

**Answer**

> I am in process with a few companies. Yours is a serious option because **[reason]**. I can join after NP **[date]**. I will not use you only as a backup if I say you are my preference — I will mean it.

---

### Q. Expected CTC / current CTC?

**Answer**

Managers sometimes ask; HR owns the number.

> I am looking for a fair offer for **[X] years in this market**. Current CTC is **[Y]** (fixed + variable, be clean). I am happy to discuss details with HR. I am not here only to jump 5% — I care about the work too.

Do not lie about CTC. Background verification catches it.

---

### Q. What do you expect from your manager?

**Answer**

> Clear priorities, support when I push back on unsafe scope, and feedback that is specific. I do not need daily micromanagement. I do need them to hear a risk in week one, not in week six.

---

### Q. What motivates you? Work-life balance?

**Answer**

> Shipping something that works in the lab and stays up in production. I will do extra hours for an incident or a release. I will not live in permanent hero mode — that is how quality dies. I plan my work so late nights are the exception.

---

## 10. Scenario questions

Speak slowly. They care about **judgement**.

---

### Q. Two P1s land on Friday evening. What do you do?

**Answer**

> Confirm both are really P1. If one is a full outage, that wins. I message the lead and the other ticket owner. I do not silently drop a customer-facing outage for a nice-to-have. I write a short status in 30 minutes even if I have no fix yet.

---

### Q. QA says your build is blocked. Dev says QA data is wrong. You are in the middle.

**Answer**

> I reproduce with the same data QA used. If I cannot, I sit with them. If data is wrong I help fix the lab setup — winning the argument is not the goal. If the code is wrong I take the bug. I report facts in Jira, not blame.

---

### Q. Leadership wants a demo tomorrow of a half-ready Kafka flow.

**Answer**

> I will demo what is real: “API accepts, status is pending, adapter mock replies.” I will not fake a live NE. I say the remaining risk in one sentence. A dishonest demo costs more than a modest one.

---

### Q. You find a security hole two days before release.

**Answer**

> I stop and tell the lead. We do not ship an IDOR on a write API to keep a date. Options: delay, or disable the endpoint, or ship with the hole only if legal/security signs it — I have never seen that last one be wise. I would rather slip than leak.

---

### Q. A teammate takes credit for your work.

**Answer**

> I mention facts in the next review: “On that story I did the design and the lab bring-up; they helped on the UI.” I do it without a scene. If it repeats I talk to the manager with examples. I do not wage war in Slack.

---

### Q. You are asked to do something unethical (inflate coverage, hide a bug).

**Answer**

> I refuse. I explain the risk. If pressured, I escalate. I will not lie in a quality gate. This is a pass/fail character question — do not joke.

---

### Q. First 30 / 60 / 90 days here?

**Answer**

> **30:** learn the product, the deploy path, who owns what, ship a small bugfix. **60:** own a medium story with tests and lab. **90:** own a production-facing slice and one improvement (test, alarm, or doc) so I am not only consuming the team.

---

### Q. Why are you still an IC at [X] years? Why not already a manager?

**Answer**

> I chose depth in backend systems first. I already do lead work: reviews, mentoring, design trade-offs. Formal people management is a different skill. I am open to it when I have something to give, not just a title.

---

### Q. How do you delegate without losing control?

**Answer**

> I delegate an outcome with acceptance criteria, context, and checkpoints—not every keystroke. I match the task to the person’s level. A junior gets a smaller slice and an early design review; a senior gets the goal and constraints. I do not take the work back at the first mistake. I review risk before release.

---

### Q. Tell me about feedback you gave that was difficult.

**Answer**

> I gave it privately, close to the event, with an example and impact: “The last two PRs changed API behavior without tests; QA found both. For the next story, let’s agree on the test cases before coding.” I listened for missing context and agreed on one measurable change. Feedback is not a personality label.

---

### Q. How do you receive critical feedback?

**Answer**

> I do not defend immediately. I ask for an example, repeat what I heard, and choose an action. If I disagree, I still test the feedback for a sprint. A useful example for me was raising blockers earlier rather than trying to solve everything alone.

---

### Q. Product and engineering disagree on priority. What do you do?

**Answer**

> Product owns business priority; engineering owns technical risk. I make the trade-off visible: customer value, effort, operational risk, and what slips. If a security/data-loss issue exists, it is not just another backlog vote. Otherwise the product owner chooses with full information, and I support the decision.

---

### Q. How do you handle ambiguity in a new domain?

**Answer**

> I identify the irreversible decisions and delay those until we learn. I make a thin vertical slice, write assumptions, speak to a domain expert, and validate it with a real example. I do not spend three weeks producing a perfect architecture for an unclear requirement.

---

### Q. How do you measure your impact when you cannot quote business revenue?

**Answer**

> I use engineering outcomes I can prove: lead time, escaped defects, incident duration, p99 latency, support tickets, test/lab cycle, or manual steps removed. If I do not have a number, I say the concrete before/after instead of inventing one.

---

### Q. What is your biggest professional achievement?

**Answer**

> I am most proud of **[one real delivery]** because it changed **[operator/customer/team outcome]**. My specific part was **[design/implementation/lab/incident ownership]**. The result was **[real metric or concrete before/after]**. I also left behind **[test/runbook/monitor]**, so the success did not depend only on me.

This is different from listing your largest project. Pick the result you can defend.

---

### Q. What would your manager and peers say about you?

**Answer**

> My manager would say I am reliable on production issues and that I communicate risk early. My peers would say I give detailed reviews and help with debugging. A growth area they would mention is **[real area]**; I am improving it by **[action]**.

Use two strengths with evidence and one genuine growth area—not three adjectives.

---

### Q. Tell me about a process improvement you drove.

**Answer**

> We repeatedly lost time because **[QA lacked setup / alerts lacked service id / releases had manual checks]**. I proposed **[small checklist, runbook, automated check, dashboard]**, piloted it on one sprint, and got the team to adopt it. It reduced **[handoffs / repeat defects / diagnosis time]**. I did not begin with a company-wide transformation.

---

### Q. How do you ramp up on an unfamiliar codebase?

**Answer**

> I start with one user flow from API to DB/dependency, run or debug it, read recent incidents and PRs, and map module owners. In week one I fix a small issue. I write down corrections to my model and ask focused questions. I do not try to read every class before making progress.

---

### Q. What if only you understand a critical component?

**Answer**

> That is a bus-factor risk, not job security. I add a short architecture/runbook, pair another engineer on the next change, rotate reviews/support, and automate the dangerous manual steps. I remain accountable while removing the team’s dependency on me.

---

### Q. Walk me through a release or hotfix you supported.

**Answer**

> Before release: change list, migration/backward compatibility, owner, rollback, smoke test. During: one communication channel and timestamps. After: verify health/business metric, not only “deployment succeeded.” A hotfix is the smallest safe change, reviewed and tested; the root cleanup gets a follow-up item.

---

## 11. Questions you should ask them

Ask **2 or 3**. You look senior if you ask about delivery, not only WFH.

**Good**

1. What does success look like for this role in the first six months?
2. How big is the team? How do you split on-call and releases?
3. What is the biggest delivery risk in the next two quarters?
4. How do you handle tech debt — is there a budget, or only features?
5. How do backend, QA, and DevOps work together here?
6. Is this role more feature factory, or will I own production too?

**Avoid first**

- Exact CTC on minute one of a manager chat (unless they open it)
- “How soon can I be promoted to manager?”
- “Is there work-from-home forever?” as the only question

---

## 12. Red flags in *your* answers (do not do these)

- “We” for every success and “they” for every failure
- 10-minute architecture lecture
- “I have no conflict, we are a happy family”
- “I can work 16 hours every day”
- Bad-mouthing Nokia / TCS / a named person
- Lying about CTC, NP, or WFO
- “I don’t know, I only do what is assigned” — that is a 2-year answer, not 6-year

---

## Night-before checklist (Round 3)

1. 90-second intro: years, ESM in one breath, one older project, why you are here.
2. ESM for a manager: operator value, Oracle + Kafka + adapter, your **owned** area.
3. Six STAR stories: incident, feature, disagreement, deadline, mentoring, failure.
4. Estimate = thin slices + early bad news.
5. Conflict = 1:1 + impact + options. Never the first escalate.
6. Testing: failure paths + lab for southbound, not a fake 90%.
7. Microservices: independent deploy **and** ops cost.
8. Why leave + why them — no insult, one real reason each.
9. NP and WFO — the true number only.
10. Three questions for them: success in 6 months, team/on-call, biggest risk.

---

## How to talk in Round 3

1. Smile with your voice. Managers hire people they can sit with in an incident.
2. Answer in 60–90 seconds, then stop.
3. If you do not have a story, say so and give how you *would* handle it. Do not invent a hero story.
4. Match energy: if they are crisp, be crisp.
5. At the end: thank them, confirm next step.

Use Round 4 for salary, notice period, joining, location, and background-verification questions.
