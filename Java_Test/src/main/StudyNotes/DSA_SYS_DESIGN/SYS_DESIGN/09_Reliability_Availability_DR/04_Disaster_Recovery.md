# 4. Disaster Recovery in Practice

## Simple idea

**Disaster recovery (DR)** is the plan for a big failure: a region, a corrupted database, or an attack. High availability handles a dead server. DR handles the day the whole site is gone and you must continue the business.

```text
Backup   →  get the data back
Failover →  get the service back
DR       →  both, written down, automated, and rehearsed
```

---

## Failover is not backup

| | Failover | Backup |
|--|----------|--------|
| Question | Can users keep working? | Can we return to an older, good copy? |
| Helps when | A server, zone, or region is down | Data was deleted, corrupted, or encrypted |
| Does not help when | The only copies were also destroyed or corrupted | You need the site up in seconds and you have not built a standby |

Use both. A replica fails over quickly and still contains the bad delete. A backup restores yesterday and does not, by itself, run the website.

---

## Mission-critical DR

Banking, healthcare, and a large shop need a written RTO and RPO, then redundancy at every layer: compute, data, and network.

- automated failover, not a hero with a laptop
- a runbook for the steps software cannot do
- a drill on the calendar

If you have not restored it, you do not have it.

---

## A high-traffic web app

1. Write the RTO and RPO with the business before you pick tools.
2. Run the app in more than one zone. Add a second region if losing the region is unacceptable.
3. **Active-active** across regions when both must serve users and you can tolerate replication lag, or you have a clear way to avoid conflicting writes.
4. **Active-passive** when one region is primary and the other is a synced standby. Simpler for the database.
5. Replicate **synchronously** when RPO must be near zero and you can pay the latency. Replicate **asynchronously** when a few seconds of loss is allowed and cross-region delay must stay off the user path.
6. Fail over DNS or the global load balancer (Route 53, a global cloud load balancer) using health checks.
7. Keep snapshots and point-in-time backups offsite, so failover is not your only tool.
8. Automate the switch and the "is the data good?" check. Run a drill.

```text
Region A (primary)  users → app → database
        │  async or sync replication
Region B (standby)  app waiting, database caught up
        │
Region dies → DNS or load balancer sends users to B
```

Promoting B while A is still accepting writes is split brain. Fence A (stop its writes) before B becomes primary.

---

## Making RTO and RPO smaller

| Goal | What you add | What it costs |
|------|----------------|---------------|
| Lower **RTO** | Automatic failover, machines already running, a rehearsed script | Idle capacity, orchestration |
| Lower **RPO** | Synchronous replication, or shipping the database log continuously (WAL, binlog, change data capture) | Write latency, cross-region traffic |

Zero RPO and zero RTO together means a hot second site and synchronous commits. Many products choose a small RPO (seconds) and a short RTO (minutes) instead of paying for both at zero.

---

## Geo-redundancy

**Geo-redundancy** means a real second place: another zone or another region, far enough that one fire, one flood, or one regional outage does not take both.

Hard parts:

| Problem | What you feel |
|---------|----------------|
| Consistency | The far region is behind, or a partition stops sync writes |
| Latency | A synchronous write waits for the other region |
| Split brain | Both regions take orders |
| Data residency | A law such as GDPR may forbid storing those users in the other region |
| Orchestration | Traffic, DNS, database promotion, and background jobs must move together |

Background jobs must move too. If region B is live but the workers still run only in A, email and billing stop even though the website looks up.

---

## Quorum

**Quorum** is the minimum number of nodes that must agree before a write or a leader election succeeds.

```text
5 nodes  →  quorum is 3
lose 2   →  the other 3 can still elect a leader and accept writes
lose 3   →  the remaining 2 must stop, so they cannot diverge
```

That is why Raft, Paxos, etcd, ZooKeeper, and CockroachDB use a majority. A failover that does not check quorum can create two primaries and corrupt data.

The cost is the CAP choice you already know. If the cluster cannot gather a majority, it should **refuse writes** rather than split. Availability drops so consistency holds. Size the cluster so one zone or one failure domain cannot hold half the votes by accident. A 3-node cluster with two nodes in one zone still dies when that zone dies.

---

## Test and automate

Automate:

- the traffic switch
- database promotion
- a check that restored data is usable
- the alert that the drill or the real failover started

Then break it on purpose: kill a zone in a test, restore a backup into a scratch database, time it, and fix the runbook where it lied.

---

## Interview lines

- Failover continues service. Backups restore an older copy. DR uses both.
- Set RTO and RPO first. Hot standby lowers RTO. Log shipping or sync replication lowers RPO.
- Second region: lag, latency, law, and split brain.
- Quorum means a majority must agree, so two halves cannot both be primary.
- If you have not tested the restore and the failover, you do not have them.

---

## Interview Q&A

### 1. Failover vs backup?

<details>
<summary>Show Answer</summary>

**Answer:**

A backup copies data so you can restore an earlier good state. It does not keep the site online by itself. Failover switches to a standby so the service continues. Backups cover corruption, deletion, and ransomware. Failover covers a dead server or region. Use both. A replica will faithfully copy a bad delete.

</details>

### 2. DR for a high-traffic web app?

<details>
<summary>Show Answer</summary>

**Answer:**

Agree RTO and RPO first. Deploy the app in multiple zones, and in a second region if a regional outage is in scope. Choose active-passive for a simpler database, or active-active only with a plan for conflicting writes. Replicate synchronously for near-zero data loss, or asynchronously to keep user latency down. Fail over with health-checked DNS or a global load balancer. Keep offsite snapshots and point-in-time backups. Automate the switch and run drills. Stop the old primary from accepting writes before the new one starts.

</details>

### 3. RTO and RPO, and how do you tighten them?

<details>
<summary>Show Answer</summary>

**Answer:**

RTO is the longest you can be down. RPO is the most data you can lose, in minutes of time. Cut RTO with automation, a hot standby, and orchestration that does not wait for a person. Cut RPO with synchronous replication or continuous log shipping. Each step costs money and complexity. Many systems aim for seconds of loss and minutes of downtime instead of zero and zero.

</details>

### 4. What is hard about geo-distributed DR?

<details>
<summary>Show Answer</summary>

**Answer:**

Regions lag or cannot talk, so consistency suffers. Synchronous replication adds latency. Without fencing, both regions can act as primary (split brain). Laws can forbid moving user data to the backup region. The failover must move traffic, the database, and the workers together, or the site is only half recovered.

</details>

### 5. What is quorum-based recovery?

<details>
<summary>Show Answer</summary>

**Answer:**

A majority must agree before a write commits or a new leader is elected. In a cluster of 5, quorum is 3. The side that cannot reach a majority stops, so it cannot diverge. Raft, Paxos, etcd, ZooKeeper, and CockroachDB work this way. Quorum prevents split brain. If a majority is down, the system refuses writes rather than guessing. Place nodes so one failure domain does not hold half the votes.

</details>

**Q&A recap:** write the downtime and data-loss numbers, keep a spare site and a restorable copy, and practice the switch.

## Interview Q&A — 6 to 8 years

### S1. Failover and backup both exist. Which one do you pull for which disaster?

<details>
<summary>Show Answer</summary>

**Answer:**

A dead host or a dead zone: failover to the spare that has been replicating. A bad delete, a bad deploy of data, or ransomware: backup, because the spare already has the bad write. I say RTO and RPO for each path. Failover RPO is replication lag. Backup RPO is how far back the last good copy is. Using failover for a bad delete makes the delete more available, not less.

**Corner cases:** A primary that is merely slow. Failing over starts a second writer. I fence. A region that is network-partitioned from me and still serving users. I may now have two worlds. I need a quorum or a manual decision, not a script that always promotes. A backup restore into the production endpoint while the sick production is still taking traffic. I restore beside it, check, then cut. DNS that keeps half the users on the dead site.

</details>

### S2. How do you run a game day without gambling the company?

<details>
<summary>Show Answer</summary>

**Answer:**

I start in a non-production environment that is a real copy of the topology, then a small production blast radius (one canary cell, one read replica). I announce it, I cap the time, and I have a stop condition. I actually promote, or I actually restore, and I time it. I write what surprised us (a firewall rule, a missing secret, a client timeout too short for the new path). I do not schedule the first ever regional failover for the day of the real fire.

**Corner cases:** A test that mocks the failure so well the real dependency is never unplugged. A game day that deletes data it cannot recreate. I use a dedicated account. Production traffic during a test that was supposed to be dark. I check the load balancer weights. A successful test from a year ago. Certificates and AMIs have expired since. I retest after major topology changes. People who can run the script are all in one city, on one VPN, which is the disaster.

</details>

### S3. Quorum and split brain. What do you set, and what do you refuse to automate?

<details>
<summary>Show Answer</summary>

**Answer:**

A majority must agree the old leader is out before a new leader takes writes. Two nodes cannot do this: they tie. I use three failure domains for a quorum system. If a partition leaves me without a majority, I stop writes (CP) rather than accept both sides. I refuse to automate a promotion when lag is past the promised RPO or when I cannot fence the old leader. That pages a human.

**Corner cases:** A witness node in the same zone as one replica, so one zone loss loses quorum even though "we had three." Clients that retry writes on both sides of a partition and create two orders. Clock-based leader election that flaps. A human override that forces a stale side to primary during an incident and loses the newer side's commits. I record the generation number so the stale side cannot win later. Quorum of data nodes versus quorum of voters. I name which one I mean.

</details>

### S4. Multi-region DR for a product that is mostly one primary. What is the smallest honest design?

<details>
<summary>Show Answer</summary>

**Answer:**

One writer region, async replication to a second region, backups in a third account, and a script that fences, promotes, and moves DNS or the anycast target. I state the RPO as the replication lag under load, not zero. I warm the second region's app servers so they are not cold-starting during the fire. I practice. If the business truly needs zero RPO, I pay for synchronous commit and I accept that a dead second region stalls writes.

**Corner cases:** Secrets, container images, and DNS hosted in the same region as the app, so the "spare region" cannot pull an image or read a key. Data residency that forbids the spare copy from leaving the country. I then need two zones, not two continents, and I say the disaster I cannot survive (the whole country). A runbook in a wiki that is in the dead region. I keep a copy offline. Customers with long-lived connections that do not re-resolve DNS. I drain and I set TTL before the disaster, not during it.

</details>

**Senior recap:** failover for dead machines, backup for bad data, practice the cut, require a majority and a fence, and state a real RPO for a warm spare region.

Next course section: **Security** — [10_Security](../10_Security/00_Section_Overview.md)
