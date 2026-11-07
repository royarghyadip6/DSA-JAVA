# 3. Backup and Recovery

## Simple idea

A **backup** is a copy you can restore **from an earlier time**. **Recovery** is the act of putting that copy back into service.

```text
Tuesday  backup
Wednesday someone drops the orders table
Thursday you restore Tuesday's copy
```

A live replica is not this. The replica also drops the table, a moment later. Backups are how you undo deletes, bad deploys, and ransomware. Replicas are how you survive a dead machine.

---

## Why you need one

- disk or software failure
- a person deletes the wrong data
- ransomware encrypts the live disks
- a data centre is destroyed
- a regulator says you must keep records for years

Replication and redundancy help with the dead machine. They copy the mistake too.

---

## Full, incremental, differential

| Type | What is copied | Restore needs | Cost |
|------|----------------|---------------|------|
| **Full** | Everything | That one backup | Slow and large |
| **Incremental** | Changes since the **last** backup (full or incremental) | The full backup **plus every** incremental after it, in order | Small and fast to take. Slow and fragile to restore |
| **Differential** | Changes since the **last full** backup | The full backup **plus the latest** differential | Grows during the week. Restore is simpler than a long incremental chain |

```text
Sunday full
Monday incremental     (only Monday's changes)
Tuesday incremental    (only Tuesday's changes)
restore Tuesday = Sunday + Monday + Tuesday

Sunday full
Wednesday differential (Mon + Tue + Wed since Sunday)
restore Wednesday = Sunday + Wednesday
```

A common pattern: full backup once a week, then daily incrementals if you must save storage, or daily differentials if you want a shorter restore.

---

## Cold, warm, and hot

These are **how ready the recovery site is**. They are not the same thing as S3 storage classes (Standard, Glacier). Glacier is a cheap place to **keep the bytes**. A cold site is a cheap, slow way to **become a running system again**.

| Posture | What is already there | Downtime | Cost |
|---------|----------------------|----------|------|
| **Cold** | Backups on a shelf or in archive storage. You build the system after the disaster | Long | Low |
| **Warm** | Machines or data partly ready. The app is not fully live | Medium | Medium |
| **Hot** | A second system is synced and can take traffic quickly | Short | High |

Examples: tapes or archive storage are cold. A standby server that still needs a database restore is warm. A load-balanced pair with a synced database is hot.

Hot is close to high availability. You pay for two of everything so RTO stays small.

---

## RTO and RPO

| | Question | Example |
|--|----------|---------|
| **RTO** (Recovery Time Objective) | How long may we be down? | Checkout must be back in 15 minutes |
| **RPO** (Recovery Point Objective) | How much data may we lose, measured in time? | We can lose at most 5 minutes of orders |

```text
failure at 10:00
RPO 5 min  →  data from 9:55 must exist
RTO 15 min →  users are served again by 10:15
```

Shorter RTO and RPO cost more. Sync replication and a hot standby push both down. A nightly backup means you can lose up to a day (RPO of 24 hours) and you restore only as fast as the tape or the snapshot allows.

Match the number to the business:

- payments, orders — small RPO and RTO (replication, point-in-time recovery)
- a weekly report job — a cold backup is enough

---

## Tradeoffs

| You want | You pay |
|----------|---------|
| Faster restore | Hot standby, more storage, more ops |
| Less storage | Incrementals, and a longer, more fragile restore |
| Long retention | A growing bill, and lifecycle rules to move old copies to archive |
| Compliance | Encryption, immutability, and a region the law allows |

Do not give every service the payments treatment. Tier it.

---

## 3-2-1, and ransomware

| Rule | Meaning |
|------|---------|
| **3** | Three copies of the data (the live one plus two) |
| **2** | Two different kinds of storage |
| **1** | One copy offsite (another region or a separate account) |

Also make one copy **immutable or offline**. Ransomware that can see your backups will encrypt those too. Object lock, or a backup account the app cannot delete from, is the usual cloud version of "offline".

Encrypt backups at rest and on the wire. Alert when a backup job fails. A green "we scheduled it" dashboard is not a backup.

---

## Cloud backups

**Easier:** snapshots (EBS, RDS), durable object storage, lifecycle rules, and a second region.

**Harder:** the bill grows with frequency and retention. You can get locked into one vendor's snapshot format. You must lock down who can delete backups. Some data is not allowed to leave a country.

Tools named in the interview notes: AWS Backup, disk and database snapshots, multi-region buckets.

---

## Microservices

1. List what is precious: databases, object buckets, config, and queues that hold unprocessed work.
2. Give each store a policy. Critical databases get continuous log backup. A cache does not.
3. Weekly full, daily incremental, or the cloud snapshot equivalent.
4. Store backups in another region, encrypted, not in the same account an attacker uses.
5. Restore on a schedule. A backup you have never restored is a hope.

Infrastructure as code (Terraform and the pipelines you already use) should create the backup policy, not a wiki page.

---

## A multi-terabyte database

You cannot copy the whole database every night without pain, and you cannot take a long outage.

- **Point-in-time recovery**: PostgreSQL WAL, or MySQL binlogs. Replay logs up to a chosen second.
- Take backups from a **replica** so the primary keeps serving.
- Use **online snapshots** from the cloud disk or the managed database.
- Prefer incremental or log backup over a daily full copy of every terabyte.
- Compress, encrypt, and park old copies on a colder tier.

Practice the restore. Restore time, not backup time, is the RTO.

---

## Interview lines

- Backups let you go back in time. Replicas copy the present, including mistakes.
- Incremental is since the last backup. Differential is since the last full. Restore incremental as a chain. Restore differential as full plus one.
- RTO is how long you may be down. RPO is how much time of data you may lose.
- Cold is cheap and slow. Hot is expensive and quick.
- 3 copies, 2 media, 1 offsite, and one copy the attacker cannot delete. Test the restore.

---

## Interview Q&A

### 1. Full vs incremental vs differential?

<details>
<summary>Show Answer</summary>

**Answer:**

A full backup copies everything. Restore is simple. It costs time and space. Use it on a weekly baseline.

An incremental backup copies changes since the last backup of any kind. It is small. Restore needs the full backup and every incremental after it, in order. Use it for daily copies when storage matters.

A differential backup copies changes since the last full backup. It grows through the week. Restore needs only the full backup and the latest differential. Use it when you want a simpler restore than a long incremental chain.

</details>

### 2. How do you balance RTO and RPO?

<details>
<summary>Show Answer</summary>

**Answer:**

RTO is the maximum time to restore service. RPO is the maximum data loss, measured in time. Lower numbers need replication, point-in-time logs, and a hot standby, which cost more. Set them from the SLA and from how bad a loss is. Payments get a tight pair. A batch report can wait on a cold backup.

</details>

### 3. Cold, warm, and hot recovery?

<details>
<summary>Show Answer</summary>

**Answer:**

Cold: only the backups exist. You rebuild after the disaster. Cheapest, slowest. Example: archive storage or tape.

Warm: some machines or data are ready, and the app is not fully live. Example: a standby host that still needs a database restore.

Hot: a synced system can take traffic quickly. Example: a second live site or a failover-ready database. Fastest and most expensive.

These are recovery postures. An S3 Glacier class is only where the backup bytes sit.

</details>

### 4. Backups for cloud microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

Find every database, bucket, and config that you cannot rebuild. Use the cloud backup service and snapshots. Replicate critical databases continuously. Take full backups weekly and incrementals or snapshots daily. Encrypt them and store them in another region and another account. Define the policy in code. Run restore drills.

</details>

### 5. Tradeoffs for a highly available service?

<details>
<summary>Show Answer</summary>

**Answer:**

Hot copies restore faster and cost more. Incrementals save space and complicate restore. Long retention fills disks and may be required by law. Immutable backups slow an attacker and add process. The RTO and RPO must match the business. Tier the data. Do not hot-standby every log file.

</details>

### 6. How does cloud storage change backups?

<details>
<summary>Show Answer</summary>

**Answer:**

It helps with durable storage, snapshots, lifecycle tiers, and a second region. It hurts when snapshots are locked to one vendor, when frequent copies and long retention surprise you on the bill, when permissions are loose, or when the law says the bytes cannot move to that region.

</details>

### 7. Automation and testing?

<details>
<summary>Show Answer</summary>

**Answer:**

Schedule backups. Create the policy with infrastructure as code. Alert on failure. Encrypt and check integrity. Follow 3-2-1: three copies, two kinds of media, one offsite, plus an immutable copy so ransomware cannot erase the history. Restore on a calendar. An untested backup is not a backup.

</details>

### 8. Terabytes of data, almost no downtime?

<details>
<summary>Show Answer</summary>

**Answer:**

Use point-in-time recovery (PostgreSQL WAL or MySQL binlogs) so you do not rely on one huge nightly file. Back up from a replica or with an online snapshot so the primary stays up. Prefer incremental and log backups. Compress, encrypt, and tier old copies. Measure a real restore. That duration is your RTO.

</details>

**Q&A recap:** decide how long you may be down and how much data you may lose, then keep a copy that a mistake cannot destroy.

## Interview Q&A — 6 to 8 years

### S1. Backups run every night and the restore failed. What was the design missing?

<details>
<summary>Show Answer</summary>

**Answer:**

A restore test. I restore on a schedule into a scratch instance and I run a query that must pass (row counts, a known order, a login). A backup job that exits zero is not a backup. I also record how long the restore took, because that time is part of RTO. If restore takes 12 hours and I promised 1 hour, the schedule is the bug, not the disk.

**Corner cases:** A backup of a replica that was already corrupted, copied faithfully. I need a check of the data, not only of the file checksum. A snapshot taken while a migration was half applied. Point-in-time logs that were on the same disk as the database and died with it. A secret required to decrypt the backup that lives only on the dead machine. A restore that works for a 1 GB test and not for the 20 TB production set because nobody budgeted disk or time. Permissions so tight that the on-call cannot read the backup, or so loose that everyone can.

</details>

### S2. How do you choose full, incremental, and PITR without hand-waving?

<details>
<summary>Show Answer</summary>

**Answer:**

Full copies are simple and slow and expensive. Incrementals are smaller and depend on the chain. If one incremental is bad, everything after it is bad. I take a full on a cadence I can re-copy from, plus logs for point-in-time recovery so I can stop just before a bad delete. RPO decides how often the log must ship off the box. RTO decides whether I can afford to replay a week of logs or I need a recent full plus a short tail.

**Corner cases:** A delete or an update that replicated into the replica and into the next incremental before anyone noticed. I must go back before that commit, which means the replica is not a backup. A long-running transaction that is open across the snapshot and restores as a roll-back, losing work I thought was covered. A clock jump that makes "restore to 10:00" ambiguous. Retention that deletes the full backup the incrementals still need. I expire a chain together.

</details>

### S3. Ransomware and a bad `DELETE` have the same design lesson. What is it?

<details>
<summary>Show Answer</summary>

**Answer:**

The copy an attacker or a bug can reach is not a backup. I use an offline or immutable copy (object lock, a different account the app cannot write, a delay before the backup account trusts deletes). 3-2-1: three copies, two media, one off-site, and that off-site one is not writable by the production role. I also limit who can delete production data. Backups do not replace an authorization check.

**Corner cases:** A backup job using the same cloud keys as the app, so the attacker deletes snapshots first. Immutable lock for 7 days, and the legal hold needs 7 years. I set retention to the stricter rule. A replica in another region that applies the drop table immediately. That is HA, not backup. Restoring after ransomware without rebuilding from trusted images re-infects the new database. I restore data onto clean systems. A quiet exfiltration the backup will not undo. Backup is not detection.

</details>

### S4. How do you back up a system that is more than one database?

<details>
<summary>Show Answer</summary>

**Answer:**

I name the user-visible object (an order) and I list every store that holds a piece of it: SQL, object storage, search, queue. I restore to a consistent-enough point. Perfect cross-store time alignment is rare. I use outbox ids and I accept a repair job that rebuilds search from SQL after the SQL restore. I do not restore SQL to yesterday and leave object storage on today so the rows point at missing files, or the reverse, without a checker.

**Corner cases:** A queue that still contains messages to charge cards I just restored past. Replaying them double-charges. I drain or I dedupe by payment key before I open traffic. A backup of SQL without the blob bucket. The site comes up and every image 404s. Different retention per store, so the blob is gone and the row remains. A partial restore of one shard. Cross-shard references dangle. I restore the shard set that owns the tenant, or I block that tenant until repair finishes.

</details>

**Senior recap:** test the restore and time it, keep a log chain you can roll back before a bad delete, make one copy immutable, and restore every store an order touches.

Next: [04_Disaster_Recovery.md](04_Disaster_Recovery.md)
