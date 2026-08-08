# 63. SQL & Database Questions

Oracle / production depth for **5–8 year** interviews. All 17 questions from `000_JAVA_Questions.md` #63.

---

## Frequently Asked

---

# 1. Primary Key?

<details>
<summary>Show Answer</summary>

**Answer:**

A **primary key (PK)** uniquely identifies each row in a table. It must be **NOT NULL** and **unique**. A table can have **only one** primary key (which may span multiple columns as a composite PK).

### Rules

| Rule | Detail |
|------|--------|
| Uniqueness | No two rows share the same PK value |
| NOT NULL | PK columns cannot be NULL |
| One per table | Only one PK constraint per table |
| Immutable (best practice) | Avoid changing PK values in production |

### Oracle Example

```sql
CREATE TABLE employees (
    emp_id   NUMBER(10)     NOT NULL,
    name     VARCHAR2(100)  NOT NULL,
    email    VARCHAR2(200),
    CONSTRAINT emp_pk PRIMARY KEY (emp_id)
);

-- Oracle also supports surrogate keys via SEQUENCE + trigger or IDENTITY (12c+)
CREATE TABLE orders (
    order_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount   NUMBER(12,2) NOT NULL
);
```

### PK vs Natural vs Surrogate

| Type | Example | Production note |
|------|---------|-----------------|
| **Natural** | SSN, email | Risky if business value changes |
| **Surrogate** | `emp_id`, UUID | Preferred in OLTP — stable, narrow index |
| **Composite** | `(tenant_id, order_id)` | Common in multi-tenant SaaS |

### Behind the Scenes (Oracle)

- Oracle creates a **unique index** automatically for PK (unless you specify `USING INDEX` on an existing index).
- PK is the preferred **parent** side of foreign keys — child FKs reference it.

**Interview Point:**

> PK = unique + NOT NULL + one per table. In production, prefer **surrogate numeric keys** (IDENTITY/SEQUENCE) over natural keys. Oracle auto-creates a unique index for PK.

</details>

---

# 2. Foreign Key?

<details>
<summary>Show Answer</summary>

**Answer:**

A **foreign key (FK)** is a column (or set of columns) that **references the primary key** (or unique key) of another table. It enforces **referential integrity** between parent and child tables.

### Behavior on Parent Change/Delete

| FK action | Meaning |
|-----------|---------|
| `ON DELETE CASCADE` | Delete child rows when parent deleted |
| `ON DELETE SET NULL` | Set FK column to NULL (if nullable) |
| `ON DELETE RESTRICT` / `NO ACTION` | Block parent delete if children exist (Oracle default) |
| `ON UPDATE CASCADE` | Rare in Oracle; PK updates are uncommon |

### Oracle Example

```sql
CREATE TABLE departments (
    dept_id   NUMBER PRIMARY KEY,
    dept_name VARCHAR2(100)
);

CREATE TABLE employees (
    emp_id   NUMBER PRIMARY KEY,
    dept_id  NUMBER,
    CONSTRAINT emp_dept_fk
        FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
);

-- Delete blocked if employees still reference dept_id
DELETE FROM departments WHERE dept_id = 10;  -- ORA-02292 if children exist
```

### Production Considerations

```text
✅ Use FK in OLTP when data integrity matters (finance, orders)
⚠️ Bulk ETL loads: FK checks slow inserts — sometimes disable, load, re-enable
⚠️ Microservices: FK across DB boundaries impossible — enforce in app/event layer
⚠️ Index the FK column on CHILD table — Oracle does NOT auto-index FK columns
```

```sql
-- Always index FK columns on child table (critical for joins & cascades)
CREATE INDEX idx_emp_dept ON employees(dept_id);
```

**Interview Point:**

> FK enforces parent-child integrity. Oracle does **not** auto-index FK columns — **always index child FK columns** for join/delete performance. Cross-service FKs don't exist in microservices.

</details>

---

# 3. Unique Key?

<details>
<summary>Show Answer</summary>

**Answer:**

A **unique key** constraint ensures all values in a column (or column set) are **distinct**. Unlike PK, unique keys allow **multiple NULLs** in Oracle (NULL ≠ NULL in unique check — each NULL is considered distinct).

### PK vs Unique Key

| | Primary Key | Unique Key |
|---|-------------|------------|
| NULL allowed | ❌ No | ✅ Yes (Oracle: multiple NULLs OK) |
| Count per table | One | Many |
| Purpose | Row identity | Business uniqueness (email, SKU) |
| Index | Unique index (auto) | Unique index (auto) |

### Oracle Example

```sql
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    email   VARCHAR2(200) CONSTRAINT users_email_uk UNIQUE,
    phone   VARCHAR2(20),
    CONSTRAINT users_phone_uk UNIQUE (phone)
);

-- Two rows with email = NULL are allowed in Oracle
INSERT INTO users (user_id, email) VALUES (1, NULL);
INSERT INTO users (user_id, email) VALUES (2, NULL);  -- OK
```

### Production Pattern

```sql
-- Soft-delete friendly unique: only active rows must be unique
CREATE UNIQUE INDEX uk_users_email_active
    ON users (CASE WHEN status = 'ACTIVE' THEN email END);
-- Or use composite: UNIQUE (email, deleted_at) in apps that store sentinel dates
```

**Interview Point:**

> Unique = no duplicate non-NULL values; multiple NULLs allowed in Oracle. Use for business keys (email, code). PK identifies row; unique enforces business rule.

</details>

---

# 4. Composite Key?

<details>
<summary>Show Answer</summary>

**Answer:**

A **composite key** is a primary key or unique key made of **two or more columns** together identifying a row uniquely.

### When to Use

| Scenario | Composite Key Example |
|----------|----------------------|
| Multi-tenant | `(tenant_id, user_id)` |
| Line items | `(order_id, line_no)` |
| Time-series partition key | `(device_id, reading_date)` |
| Junction / M:N table | `(student_id, course_id)` |

### Oracle Example

```sql
CREATE TABLE order_lines (
    order_id  NUMBER NOT NULL,
    line_no   NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    qty       NUMBER NOT NULL,
    CONSTRAINT order_lines_pk PRIMARY KEY (order_id, line_no),
    CONSTRAINT order_lines_order_fk
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
```

### Index & Query Implications

```text
Composite PK index order matters for queries:
  PK (tenant_id, user_id)
  ✅ Fast: WHERE tenant_id = ?
  ✅ Fast: WHERE tenant_id = ? AND user_id = ?
  ❌ Slow (no leading column): WHERE user_id = ? alone
```

```sql
-- Leading column rule applies to composite indexes too
CREATE INDEX idx_ol_product ON order_lines (product_id);  -- separate index if needed
```

**Interview Point:**

> Composite key = multi-column PK/UK. Index follows **leftmost prefix rule** — design leading column for your most common filter (e.g., `tenant_id` first in SaaS).

</details>

---

## Joins

---

# 5. Inner Join

<details>
<summary>Show Answer</summary>

**Answer:**

**Inner join** returns only rows where the join condition **matches in both tables**. Non-matching rows are excluded.

### Syntax (Oracle)

```sql
-- ANSI (preferred)
SELECT e.emp_id, e.name, d.dept_name
FROM   employees e
INNER JOIN departments d ON e.dept_id = d.dept_id;

-- Equivalent Oracle old style (avoid in new code)
SELECT e.emp_id, e.name, d.dept_name
FROM   employees e, departments d
WHERE  e.dept_id = d.dept_id;
```

### Join Types at a Glance

| Join | Returns |
|------|---------|
| **INNER** | Matching rows only |
| LEFT | All left + matched right (NULL if no match) |
| RIGHT | All right + matched left |
| FULL | All from both; NULL where no match |

### Execution (Oracle Optimizer)

```text
Nested Loops  → small driving set, indexed inner table
Hash Join     → large equi-joins, no useful index on inner
Merge Join    → both sides pre-sorted on join key
```

```sql
-- Hint only for diagnosis — don't leave in production
SELECT /*+ USE_NL(e d) */ ...
FROM employees e JOIN departments d ON ...
```

**Interview Point:**

> Inner join = intersection of keys. Oracle picks nested loops / hash / merge join based on stats and indexes. Always join on **indexed columns** in production.

</details>

---

# 6. Left Join

<details>
<summary>Show Answer</summary>

**Answer:**

**Left outer join** returns **all rows from the left table** plus matching rows from the right. If no match, right-side columns are **NULL**.

### Example

```sql
-- All employees, even without a department
SELECT e.emp_id, e.name, d.dept_name
FROM   employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;

-- Find employees with NO department (anti-join pattern)
SELECT e.emp_id, e.name
FROM   employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE  d.dept_id IS NULL;
```

### LEFT vs INNER — Production

| Use LEFT when | Use INNER when |
|---------------|----------------|
| Optional relationship (dept may be missing) | Relationship required |
| Find "missing" child/parent rows | Only care about matches |
| Reporting "all X with optional Y" | Strict filtered dataset |

### Oracle Note

```sql
-- Oracle outer join old syntax (legacy) — use ANSI instead
SELECT e.name, d.dept_name
FROM employees e, departments d
WHERE e.dept_id = d.dept_id(+);  -- (+) on optional side
```

**Interview Point:**

> LEFT join keeps all left rows; unmatched right = NULL. `LEFT JOIN ... WHERE right.key IS NULL` = **anti-join** (find orphans). Prefer ANSI syntax over Oracle `(+)`.

</details>

---

# 7. Right Join

<details>
<summary>Show Answer</summary>

**Answer:**

**Right outer join** returns **all rows from the right table** plus matches from the left. Unmatched left columns are NULL.

### Example

```sql
-- All departments, even with zero employees
SELECT d.dept_name, e.emp_id, e.name
FROM   employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
```

### RIGHT vs LEFT

```text
RIGHT JOIN A ON B  ≡  LEFT JOIN B ON A
```

Always rewrite RIGHT as LEFT for readability — most teams ban RIGHT JOIN in style guides.

```sql
-- Preferred equivalent
SELECT d.dept_name, e.emp_id, e.name
FROM   departments d
LEFT JOIN employees e ON e.dept_id = d.dept_id;
```

**Interview Point:**

> RIGHT join = all right rows preserved. **Rewrite as LEFT join** by swapping table order — same result, better readability. Rarely used in production code reviews.

</details>

---

# 8. Full Join

<details>
<summary>Show Answer</summary>

**Answer:**

**Full outer join** returns **all rows from both tables**. Matching rows are combined; non-matching rows have NULLs on the opposite side.

### Example

```sql
SELECT e.emp_id, e.name, d.dept_id, d.dept_name
FROM   employees e
FULL OUTER JOIN departments d ON e.dept_id = d.dept_id;

-- Employees without dept + departments without employees + matched pairs
```

### Full Join via UNION (Oracle & portability)

```sql
-- Equivalent pattern when FULL OUTER JOIN not available or for clarity
SELECT e.emp_id, d.dept_id FROM employees e JOIN departments d ON ...
UNION ALL
SELECT e.emp_id, d.dept_id FROM employees e LEFT JOIN departments d ON ... WHERE d.dept_id IS NULL
UNION ALL
SELECT e.emp_id, d.dept_id FROM employees e RIGHT JOIN departments d ON ... WHERE e.emp_id IS NULL;
```

### Production Use

| Use case | Example |
|----------|---------|
| Data reconciliation | Compare source vs target row counts/keys |
| Master data diff | Find records only in system A or B |
| ETL validation | Orphan detection both directions |

**Interview Point:**

> FULL OUTER JOIN = union of LEFT + RIGHT. Expensive on large tables — use for **reconciliation/diff** reports, not hot OLTP paths. Oracle supports ANSI FULL OUTER JOIN natively.

</details>

---

# 9. Cross Join

<details>
<summary>Show Answer</summary>

**Answer:**

**Cross join** (Cartesian product) returns **every row of table A paired with every row of table B**. Row count = `rows(A) × rows(B)`. No join condition (or `CROSS JOIN` keyword).

### Example

```sql
-- 1000 employees × 500 departments = 500,000 rows — dangerous!
SELECT e.emp_id, d.dept_id
FROM   employees e
CROSS JOIN departments d;

-- Accidental cross join (missing join condition)
SELECT e.emp_id, d.dept_id
FROM   employees e, departments d;  -- no WHERE → Cartesian product
```

### Legitimate Uses

```sql
-- Generate date series / number generator
SELECT d.dt, p.product_id
FROM   (SELECT TRUNC(SYSDATE) + LEVEL - 1 AS dt FROM dual CONNECT BY LEVEL <= 30) d
CROSS JOIN products p;

-- Combinatorial test data, pivot scaffolding
```

### Production Warning

```text
❌ Missing JOIN condition in multi-table FROM → silent cross join → DB meltdown
✅ Code review: every multi-table query must have explicit join predicate
✅ EXPLAIN PLAN row count explosion = first sign of accidental cross join
```

**Interview Point:**

> Cross join = Cartesian product — **no ON clause**. Almost always a bug in OLTP. Valid for **generators** and small dimension combos. Watch for accidental `,` syntax without WHERE.

</details>

---

# 10. Self Join

<details>
<summary>Show Answer</summary>

**Answer:**

A **self join** is a table joined **to itself** using aliases. Used for hierarchical or peer relationships within the same table.

### Classic: Employee → Manager

```sql
SELECT e.emp_id,
       e.name       AS employee,
       m.name       AS manager
FROM   employees e
LEFT JOIN employees m ON e.manager_id = m.emp_id;
```

### Other Patterns

| Pattern | Self join on |
|---------|--------------|
| Org hierarchy | `manager_id = emp_id` |
| Adjacent pairs | `created_at > prev.created_at` |
| Duplicate detection | `a.email = b.email AND a.id < b.id` |

```sql
-- Find duplicate emails (keep lowest id)
SELECT a.email
FROM   users a
JOIN   users b ON a.email = b.email AND a.user_id > b.user_id;
```

### Oracle Hierarchical Alternative

```sql
-- For deep org trees, CONNECT BY is often better than repeated self joins
SELECT emp_id, name, LEVEL, SYS_CONNECT_BY_PATH(name, ' > ') AS path
FROM   employees
START WITH manager_id IS NULL
CONNECT BY PRIOR emp_id = manager_id;

-- 11g+: recursive WITH (portable)
WITH emp_tree (emp_id, name, lvl) AS (
    SELECT emp_id, name, 1 FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.emp_id, e.name, t.lvl + 1
    FROM   employees e JOIN emp_tree t ON e.manager_id = t.emp_id
)
SELECT * FROM emp_tree;
```

**Interview Point:**

> Self join = same table, different aliases. Manager-employee is classic. For **deep hierarchies** in Oracle, prefer `CONNECT BY` or recursive CTE over chained self joins.

</details>

---

## Advanced — Indexes

---

# 11. Index?

<details>
<summary>Show Answer</summary>

**Answer:**

An **index** is a separate database structure that speeds up **data retrieval** by key (like a book index). It trades **extra storage** and **slower writes** (INSERT/UPDATE/DELETE must maintain the index) for **faster reads**.

### Types in Oracle (common)

| Type | Description |
|------|-------------|
| **B-tree** (default) | Equality & range on columns — most OLTP indexes |
| **Bitmap** | Low-cardinality columns (status, gender) — DWH |
| **Function-based** | Index on expression: `UPPER(email)` |
| **Composite** | Multiple columns in one index |

### Create & Use

```sql
CREATE INDEX idx_emp_dept ON employees (dept_id);

-- Function-based (case-insensitive lookup)
CREATE INDEX idx_users_email_upper ON users (UPPER(email));

SELECT * FROM employees WHERE dept_id = 10;  -- likely INDEX RANGE SCAN
```

### When to Index

```text
✅ FK columns, WHERE/JOIN columns, ORDER BY columns (selective)
✅ High-read, selective columns
❌ Low-cardinality alone (flag with 2 values) — bitmap or skip
❌ Small tables (full table scan cheaper)
❌ Heavy write tables — too many indexes hurt INSERT/UPDATE
```

### Oracle Storage

```sql
-- Check index usage (production monitoring)
SELECT index_name, table_name, num_rows, distinct_keys, clustering_factor
FROM   user_indexes
WHERE  table_name = 'EMPLOYEES';

-- Index may be UNUSABLE after partition maintenance — rebuild
ALTER INDEX idx_emp_dept REBUILD ONLINE;
```

**Interview Point:**

> Index = faster SELECT, slower DML, more disk. Oracle default is **B-tree**. Index **FK columns**. Monitor unused indexes — drop them to speed writes.

</details>

---

# 12. Clustered Index?

<details>
<summary>Show Answer</summary>

**Answer:**

In databases that support it (SQL Server, MySQL InnoDB), a **clustered index** defines **physical row order** on disk — the table data **is** the leaf level of that index. **Only one** clustered index per table.

### Oracle Equivalent — Important Interview Distinction

```text
Oracle does NOT have a "clustered index" like SQL Server.
Oracle heap-organized tables: rows stored unordered (by default).
Oracle Index-Organized Tables (IOT): table stored in PK B-tree index structure.
```

| Concept | SQL Server | Oracle |
|---------|------------|--------|
| Clustered index | Data rows in PK index order | No direct equivalent |
| Closest Oracle feature | — | **Index-Organized Table (IOT)** |
| Default table | Clustered on PK (InnoDB/SQL Server) | **Heap** (unordered) |

### Oracle IOT Example

```sql
CREATE TABLE session_tokens (
    token_id   VARCHAR2(64) PRIMARY KEY,
    user_id    NUMBER,
    expires_at TIMESTAMP
) ORGANIZATION INDEX;  -- rows stored in PK index structure
```

### When IOT Helps (Oracle)

```text
✅ Primary key lookups dominate (session store, code lookup tables)
✅ Small narrow rows, always accessed by PK
❌ Heavy secondary index access — secondary indexes point to logical ROWID (extra hop)
```

**Interview Point:**

> **Clustered index** = SQL Server/MySQL term — data physically sorted by index. **Oracle has no clustered index** on normal tables; closest is **IOT (ORGANIZATION INDEX)**. Don't confuse with **index clustering factor** (stats metric).

</details>

---

# 13. Non-clustered Index?

<details>
<summary>Show Answer</summary>

**Answer:**

A **non-clustered index** (secondary index) is a **separate structure** from table data. Leaf nodes contain **key + ROWID** (Oracle) or key + bookmark (SQL Server) pointing to actual rows.

### Oracle B-tree Structure (simplified)

```text
Root → Branch → Leaf (key, ROWID)
                      ↓
                 Table row (heap)
```

```sql
CREATE INDEX idx_emp_name ON employees (last_name);

-- Lookup: index range scan on 'SMITH' → ROWIDs → table access by ROWID
SELECT * FROM employees WHERE last_name = 'SMITH';
```

### Covering Index Concept

```sql
-- Index contains all columns needed — avoids table access (index-only scan)
CREATE INDEX idx_emp_cover ON employees (dept_id, emp_id, last_name);

SELECT emp_id, last_name
FROM   employees
WHERE  dept_id = 10;
-- Possible: INDEX RANGE SCAN (no TABLE ACCESS BY ROWID if all cols in index)
```

### Clustering Factor (Oracle stat)

```text
Low clustering factor  → index key order matches table row order → fewer block visits
High clustering factor → scattered rows → more I/O per index lookup
```

```sql
SELECT index_name, clustering_factor FROM user_indexes WHERE table_name = 'EMPLOYEES';
```

**Interview Point:**

> Non-clustered = separate B-tree + ROWID pointer. **All Oracle normal indexes are non-clustered** (heap table). Aim for **index-only scan** with covering indexes. Watch **clustering factor** in execution plans.

</details>

---

# 14. Composite Index?

<details>
<summary>Show Answer</summary>

**Answer:**

A **composite index** (concatenated index) includes **multiple columns** in a single B-tree, ordered **left to right**.

### Leading Column Rule

```sql
CREATE INDEX idx_emp_dept_status ON employees (dept_id, status, hire_date);

-- Uses index efficiently:
WHERE dept_id = 10
WHERE dept_id = 10 AND status = 'ACTIVE'
WHERE dept_id = 10 AND status = 'ACTIVE' AND hire_date > DATE '2024-01-01'

-- May NOT use index (dept_id not leading):
WHERE status = 'ACTIVE'
WHERE hire_date > DATE '2024-01-01'
```

### Column Order Design

| Put first | Reason |
|-----------|--------|
| Highest equality filter selectivity | Narrows fastest |
| Most frequent WHERE column | Matches query patterns |
| `=` columns before range columns | B-tree prefix preserved |

```sql
-- Bad for: WHERE status = 'A' (low selectivity first)
CREATE INDEX idx_bad ON orders (status, customer_id);

-- Better if queries filter customer_id often
CREATE INDEX idx_good ON orders (customer_id, status);
```

### Index Skip Scan (Oracle)

```text
Oracle can sometimes use composite index even without leading column (skip scan)
— but don't rely on it; design index for real queries
```

**Interview Point:**

> Composite index column **order matters** — leftmost prefix rule. Design from actual SQL (AWR, V$SQL). Put equality filters first, then range. Oracle **skip scan** is optimizer trick, not a design strategy.

</details>

---

## Query Optimization

---

# 15. Explain query optimization techniques.

<details>
<summary>Show Answer</summary>

**Answer:**

Query optimization makes SQL run faster using **better plans**, **indexes**, **SQL rewrite**, and **schema/design** choices.

### Techniques Table

| Technique | What to do |
|-----------|------------|
| **Indexes** | FK, WHERE, JOIN keys; composite for query patterns |
| **Statistics** | `DBMS_STATS.GATHER_TABLE_STATS` — stale stats = bad plans |
| **SQL rewrite** | Avoid `SELECT *`, functions on indexed columns, implicit conversion |
| **Join order & method** | Optimizer chooses NL / hash / merge — help with stats & indexes |
| **Partitioning** | Partition pruning on date/tenant — scan less data |
| **Materialized views** | Pre-aggregate heavy reporting queries |
| **Bind variables** | Reduce hard parses; shared pool reuse |
| **Hints (last resort)** | `/*+ INDEX(...) */` — fix after proving plan issue |

### Oracle-Specific Examples

```sql
-- ❌ Function on column kills index
SELECT * FROM users WHERE UPPER(email) = 'A@B.COM';
-- ✅ Function-based index OR store normalized
CREATE INDEX idx_email_upper ON users (UPPER(email));

-- ❌ Implicit type conversion
SELECT * FROM orders WHERE order_id = '12345';  -- VARCHAR compared to NUMBER

-- ✅ Partition pruning
SELECT * FROM sales WHERE sale_date >= DATE '2025-01-01' AND sale_date < DATE '2026-01-01';
-- Only relevant partitions scanned

-- Bind variables
VAR v_dept NUMBER;
EXEC :v_dept := 10;
SELECT * FROM employees WHERE dept_id = :v_dept;
```

### Application Layer

```text
✅ Pagination: OFFSET/FETCH or keyset (WHERE id > :lastId) — not ROWNUM on huge sets
✅ Batch DML: FORALL/BULK COLLECT in PL/SQL; JDBC batch inserts
✅ Connection pooling (HikariCP) — avoid connection churn
✅ Read replicas for reporting — offload OLTP primary
```

**Interview Point:**

> Optimize in order: **correct SQL → indexes → fresh stats → partitioning/MV → hints**. In Oracle, stale statistics and **functions on indexed columns** are top production culprits. Always measure with **AWR/ASH**, not guesses.

</details>

---

# 16. Why index not used?

<details>
<summary>Show Answer</summary>

**Answer:**

The optimizer may **ignore an index** when a **full table scan** or different access path is cheaper, or when the index **cannot** be applied to the predicate.

### Common Reasons

| Reason | Example |
|--------|---------|
| **Function on column** | `WHERE UPPER(name) = 'X'` — index on `name` unused |
| **Implicit conversion** | `WHERE num_col = '123'` — type mismatch |
| **Low selectivity** | `WHERE gender = 'M'` on 50/50 data — FTS cheaper |
| **Stale statistics** | Optimizer wrong row estimates |
| **Leading column missing** | `WHERE col2 = ?` on index `(col1, col2)` |
| **`OR` across columns** | May become FULL SCAN unless UNION ALL rewrite |
| **`NOT`, `<>`, `IS NULL`** | Often precludes efficient index use |
| **`LIKE '%abc'`** | Leading wildcard — index range scan impossible |
| **Small table** | FTS cheaper than index + ROWID hop |
| **Index UNUSABLE** | After failed load/partition ops |

### Diagnostic SQL (Oracle)

```sql
-- Execution plan
EXPLAIN PLAN FOR
SELECT * FROM employees WHERE dept_id = 10;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- Real-time plan from cursor
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL, NULL, 'ALLSTATS LAST'));

-- Is index valid?
SELECT index_name, status FROM user_indexes WHERE table_name = 'EMPLOYEES';
```

### Fixes

```sql
-- Function-based index
CREATE INDEX idx_name_upper ON employees (UPPER(last_name));

-- Rewrite OR
SELECT * FROM t WHERE a = 1 OR b = 2;
-- → SELECT * FROM t WHERE a = 1 UNION ALL SELECT * FROM t WHERE b = 2 AND a <> 1;

-- Gather stats
EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'EMPLOYEES', CASCADE => TRUE);

-- Force test (dev only)
SELECT /*+ INDEX(e IDX_EMP_DEPT) */ * FROM employees e WHERE dept_id = 10;
```

**Interview Point:**

> Index skipped when **not selective enough**, **predicate not indexable**, or **stats lie**. Top fixes: remove function on column, fix data types, refresh stats, fix composite column order. Prove with **EXPLAIN PLAN / DBMS_XPLAN**.

</details>

---

# 17. How to analyze slow query?

<details>
<summary>Show Answer</summary>

**Answer:**

Systematic slow-query analysis: **identify → measure → plan → fix → verify**.

### Step-by-Step (Oracle Production)

```text
1. FIND the SQL        → AWR, ASH, V$SQL, app logs, APM (Dynatrace/New Relic)
2. GET execution plan  → DBMS_XPLAN, SQL Developer, SQL Monitor report
3. CHECK wait events   → db file sequential read (index), direct path read (FTS/parallel)
4. VERIFY stats/index  → stale? missing? unusable?
5. FIX & re-test       → index, rewrite, partition, hint (last resort)
6. BASELINE plan       → SQL Plan Baseline if regression after upgrade
```

### Key Oracle Views / Tools

| Tool | Purpose |
|------|---------|
| `V$SQL` / `V$SQLAREA` | SQL text, elapsed time, executions |
| `DBMS_XPLAN.DISPLAY_CURSOR` | Actual execution plan |
| `DBMS_SQLTUNE.REPORT_SQL_MONITOR` | Real-time SQL monitor (long runners) |
| **AWR report** | Top SQL by elapsed/CPU/IO |
| **ASH** | Session-level wait breakdown |
| `SQL_TRACE` / **10046 trace** | Deep diagnostic (dev/support) |

```sql
-- Find top slow SQL in current instance
SELECT sql_id, elapsed_time/1e6 AS elapsed_sec, executions, buffer_gets,
       SUBSTR(sql_text, 1, 80) AS sql_snippet
FROM   v$sql
WHERE  executions > 0
ORDER  BY elapsed_time DESC
FETCH FIRST 10 ROWS ONLY;

-- SQL Monitor for running/long query
SELECT DBMS_SQLTUNE.REPORT_SQL_MONITOR(sql_id => '&sql_id', type => 'TEXT') FROM dual;
```

### What to Look For in Plan

```text
TABLE ACCESS FULL on large table     → missing index or bad predicate
NESTED LOOPS with high iterations    → wrong join order / missing index on inner
CARTESIAN JOIN                       → missing join condition
BUFFER SORT / TEMP usage             → sort/hash spill to disk — memory or rewrite
Rows estimate vs actual (12c+ note)  → stats problem if huge gap
```

### Java / App Side

```java
// Enable Hibernate SQL + bind logging (dev/staging only)
// spring.jpa.properties.hibernate.format_sql=true
// logging.level.org.hibernate.SQL=DEBUG

// JDBC: set query timeout
stmt.setQueryTimeout(30);

// HikariCP: leak detection
// spring.datasource.hikari.leak-detection-threshold=60000
```

**Interview Point:**

> Slow query flow: **AWR/V$SQL find SQL → XPLAN/Monitor get plan → waits + row estimates → fix index/stats/SQL → verify**. Mention **ASH** for real-time, **SQL Plan Baseline** for plan stability after Oracle patches.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Oracle clustered index on normal table?

<details>
<summary>Show Answer</summary>

**Answer:**

No. Oracle heap tables have **no clustered index**. Closest: **Index-Organized Table (IOT)**. Don't confuse with **clustering factor** statistic.

</details>

---

### Q: Must you index foreign key columns?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes, on the child table.** Oracle does not auto-create FK indexes. Missing FK index = slow joins, slow parent DELETE/UPDATE, lock contention.

</details>

---

### Q: PK vs Unique in Oracle NULL handling?

<details>
<summary>Show Answer</summary>

**Answer:**

PK: **no NULLs**. Unique: **multiple NULLs allowed** (each NULL distinct in unique check).

</details>

---

### Q: INNER vs LEFT in one line?

<details>
<summary>Show Answer</summary>

**Answer:**

INNER = matches only. LEFT = all left rows + matched right; NULL if no match.

</details>

---

### Q: Accidental cross join symptom?

<details>
<summary>Show Answer</summary>

**Answer:**

Row explosion (N×M), query hangs, plan shows **CARTESIAN JOIN**. Fix: add proper JOIN/WHERE condition.

</details>

---

### Q: Why `WHERE UPPER(col) = ?` skips index?

<details>
<summary>Show Answer</summary>

**Answer:**

Function on column prevents B-tree use on `col`. Fix: **function-based index** on `UPPER(col)` or store normalized value.

</details>

---

### Q: Composite index `(a,b,c)` — which WHERE uses it?

<details>
<summary>Show Answer</summary>

**Answer:**

`a`, `a+b`, `a+b+c` — **leftmost prefix**. `b` alone or `c` alone — generally no (unless skip scan).

</details>

---

### Q: First tool for slow query in Oracle prod?

<details>
<summary>Show Answer</summary>

**Answer:**

**AWR** (historical top SQL) + **V$SQL** + **DBMS_XPLAN** / **SQL Monitor** for plan and waits. ASH for live session waits.

</details>

---

### Q: Bitmap vs B-tree index Oracle?

<details>
<summary>Show Answer</summary>

**Answer:**

B-tree: OLTP, high cardinality, DML heavy. Bitmap: DWH, **low cardinality**, read-heavy — poor for concurrent DML (lock whole blocks).

</details>

---

### Q: Self join vs CONNECT BY?

<details>
<summary>Show Answer</summary>

**Answer:**

Self join: one level (employee-manager). **CONNECT BY** / recursive CTE: **full hierarchy** traversal.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> PK = unique NOT NULL identity; FK needs **indexed child column**; unique allows multiple NULLs in Oracle. Joins: INNER match only, LEFT keeps left, FULL reconciles both, CROSS = Cartesian. Oracle has **no clustered index** — use IOT if needed. Optimize: indexes + stats + rewrite; diagnose with **AWR/XPLAN/ASH**. Index skipped when predicate not sargable or FTS cheaper.

</details>
