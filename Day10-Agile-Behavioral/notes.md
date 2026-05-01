# 📘 Day 10 – Agile & Behavioral Questions (May 11th)

> **Goal:** Be confident and articulate in HR and Agile rounds.

---

## ✅ Checklist
- [ ] Agile/Scrum terminology
- [ ] Prepare 5 STAR-format behavioral answers
- [ ] Questions to ask the interviewer
- [ ] Resume talking points

---

## 1. Agile/Scrum Terminology

| Term | Definition |
|---|---|
| **Sprint** | Time-boxed iteration (usually 2 weeks) to deliver working software |
| **Product Backlog** | Prioritized list of all features/requirements |
| **Sprint Backlog** | Items selected from product backlog to complete in current sprint |
| **User Story** | Feature from the user's perspective: "As a [user], I want [feature], so that [benefit]" |
| **Story Points** | Relative estimate of effort (Fibonacci: 1, 2, 3, 5, 8, 13) |
| **Velocity** | Average story points completed per sprint |
| **Daily Standup** | 15-min daily meeting: What did I do? What will I do? Any blockers? |
| **Sprint Review** | Demo completed work to stakeholders at end of sprint |
| **Sprint Retrospective** | Team reflects: What went well? What to improve? |
| **Definition of Done (DoD)** | Agreed criteria for when a story is truly complete (coded, tested, reviewed, deployed) |
| **Epic** | Large user story broken into multiple smaller stories |
| **Spike** | Time-boxed research/investigation task |
| **Scrum Master** | Facilitates Scrum, removes blockers (NOT a manager) |
| **Product Owner** | Defines and prioritizes the backlog, voice of the customer |

---

## 2. STAR Method for Behavioral Questions

**S**ituation → **T**ask → **A**ction → **R**esult

---

### Q: Tell me about a challenging technical problem you solved.

**S:** At [Company], our order processing service was experiencing intermittent timeouts during peak hours. Latency was spiking from 200ms to 8 seconds, affecting checkout conversion.

**T:** I was responsible for investigating and resolving the issue within one sprint to prevent revenue loss.

**A:** 
- Added distributed tracing (Zipkin) and discovered the bottleneck: a downstream inventory service was doing N+1 database queries on every order request.
- Optimized the query using JOIN FETCH and added Redis caching for product stock data with a 5-minute TTL.
- Added a circuit breaker (Resilience4j) so that if the inventory service was slow, we'd use cached data.

**R:** P99 latency dropped from 8s to 350ms. The fix also reduced inventory service database load by 70%. The solution was deployed with zero downtime using a rolling update.

---

### Q: Describe a time you disagreed with a teammate.

**S:** A senior developer wanted to add a synchronous REST call to an external tax calculation service in our checkout flow.

**T:** I disagreed because if the tax service was slow or down, our entire checkout would fail.

**A:** I prepared a short proposal showing: 1) The single point of failure risk, 2) An alternative using async Kafka messages with a fallback tax calculation, 3) Data showing the tax service had 99.5% uptime (not enough for a critical path). I presented it in our architecture review meeting.

**R:** The team agreed to use the async approach. We implemented it with Kafka and a fallback. Three weeks later, the tax service had a 20-minute outage — our system handled it gracefully while competitors' checkouts failed.

---

### Q: Tell me about a time you had to learn something new quickly.

**S:** My team decided to migrate from Spring MVC to Spring WebFlux for reactive programming. I had no reactive experience.

**T:** I was assigned to implement the first reactive endpoint within the sprint.

**A:** I spent 2 days intensively studying Project Reactor (Mono/Flux), watched Baeldung and Spring.io tutorials, and built a small practice project. I then pair-programmed with a colleague who had reactive experience for code review.

**R:** I delivered the endpoint on time. I also created an internal wiki page on reactive patterns that became our team reference guide, used by 5 other developers in subsequent sprints.

---

### Q: How do you handle multiple priorities/deadlines?

**S:** I had three concurrent tasks: a critical production bug, a sprint feature, and a tech debt refactor.

**T:** I needed to manage all three without dropping any.

**A:** I immediately communicated with my Scrum Master and Product Owner about the production bug, which reprioritized it to the top. I created a simple priority list: 1) Bug fix (P0), 2) Sprint feature (committed), 3) Tech debt (best effort). I fixed the bug in 4 hours using a hotfix branch, then resumed sprint work. The tech debt was moved to the next sprint with documentation.

**R:** Bug was resolved and deployed same day. Sprint commitment was met. The team appreciated the transparent communication about trade-offs.

---

### Q: Where do you see yourself in 3 years?

"I see myself growing into a Senior Backend Engineer, taking ownership of complex distributed systems and mentoring junior developers. I'm particularly interested in deepening my expertise in cloud-native architectures and real-time data processing. I want to contribute to technical decisions at the architecture level — designing systems that are resilient and scalable from the ground up."

---

## 3. Questions to Ask the Interviewer

These show genuine interest and intelligence:

1. "What does a typical sprint look like for this team? How are priorities set?"
2. "What are the biggest technical challenges the team is facing right now?"
3. "How do you handle on-call and production incidents?"
4. "What does the onboarding process look like for a new engineer?"
5. "How does the team approach code reviews and technical decision-making?"
6. "What technologies are you considering adopting in the next year?"
7. "What do you enjoy most about working on this team?"

---

## 4. Key Numbers to Remember

Memorize these — they come up in system design conversations:

| Fact | Value |
|---|---|
| L1 cache access | ~0.5 ns |
| L2 cache access | ~7 ns |
| RAM read | ~100 ns |
| SSD read | ~150 μs |
| HDD read | ~10 ms |
| Packet around the world | ~150 ms |
| 1 Gbps network | ~125 MB/s |
| 99.9% SLA downtime/year | 8.7 hours |
| 99.99% SLA downtime/year | 52 minutes |
