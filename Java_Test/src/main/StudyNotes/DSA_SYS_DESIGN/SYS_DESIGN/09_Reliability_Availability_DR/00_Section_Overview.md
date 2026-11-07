# 09 — Reliability, Availability, and Disaster Recovery

This folder matches the course section **Reliability, Availability & Disaster Recovery**.

Notes use the Section 9 slides and the interview PDFs. Each file is **theory first**, then **interview Q&A**.

---

## Why this section exists

Systems fail. Disks die, deploys break, a region goes dark, someone deletes a table.

```text
Does it keep working?     →  reliability and availability
Is the data still there?  →  durability and backups
How fast are we back?     →  RTO
How much data did we lose? →  RPO
```

Performance asked "is it fast?". This section asks "does it survive, and can we restore it?".

**Words in this folder**

| Word | Meaning |
|------|---------|
| **Availability** | People can use the product right now |
| **Durability** | After a successful save, the data is still there later |
| **Failover** | Traffic moves to a spare when the main machine dies |
| **Backup** | An older copy you can restore if someone deletes or encrypts the live data |
| **RTO** | How long you are allowed to be down |
| **RPO** | How much recent data you are allowed to lose |
| **Quorum** | A majority of machines must agree, so two halves cannot both claim to be in charge |

A replica is not a backup. The replica copies a mistake too. The backup is how you go back in time.

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_System_Reliability.md](01_System_Reliability.md) | Reliability, MTBF, MTTR, SLA, nines + Q&A |
| 2 | [02_High_Availability_and_Failover.md](02_High_Availability_and_Failover.md) | Redundancy, active-active, graceful degradation + Q&A |
| 3 | [03_Backup_and_Recovery.md](03_Backup_and_Recovery.md) | Full, incremental, differential, RTO, RPO + Q&A |
| 4 | [04_Disaster_Recovery.md](04_Disaster_Recovery.md) | Failover vs backup, geo-redundancy, quorum + Q&A |

---

## One-page picker

| You need | Usual tool |
|----------|------------|
| Stay up when one server dies | More than one instance, a load balancer, health checks |
| Stay up when one data centre dies | Another zone or region, and a tested failover |
| Undo a bad delete or ransomware | Backups and point-in-time recovery, not only a replica |
| Know the promise | SLA, and the downtime budget of that percentage |
| Avoid two leaders | Quorum (majority) and fencing |

A replica is not a backup. Failover keeps the service up. A backup lets you go back in time.

---

## What is next

**Security**: [10_Security](../10_Security/00_Section_Overview.md)

---

## Source PDFs used

- `System Design - updated - Section 9.pdf`
- System Reliability Q&A
- High Availability, Fault Tolerance & Failover Q&A
- Backup & Recovery Strategies Q&A
- Disaster Recovery in Practice Q&A
