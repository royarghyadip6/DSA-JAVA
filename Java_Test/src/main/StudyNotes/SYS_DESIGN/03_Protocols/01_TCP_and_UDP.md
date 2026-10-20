# 1. TCP and UDP

## Simple idea

TCP and UDP are **transport** rules. They sit under HTTP, DNS, and games.

```text
Your app
   |
   +-- TCP   "Please deliver every byte, in order. I can wait."
   |
   +-- UDP   "Send it now. If a piece is lost, do not stop the show."
```

Both carry data from one machine to another. They disagree on **reliability vs speed**.

---

## TCP (Transmission Control Protocol)

TCP is **connection-oriented**.

Before data moves, the two sides shake hands and agree to talk. Then TCP:

- checks that packets arrived
- puts them back **in order**
- resends what was lost
- uses a checksum so a corrupted packet is noticed

Result: the file, the webpage, or the SQL query arrives **complete and correct**.

Cost: extra waiting and extra CPU.

---

## UDP (User Datagram Protocol)

UDP is **connectionless**.

There is no handshake. A packet (datagram) is sent and forgotten.

- no delivery promise
- no automatic resend
- packets may arrive out of order
- header is small (**8 bytes** vs TCP's **20 bytes**)

Result: **faster**, but the app must live with gaps.

---

## Side by side

| | TCP | UDP |
|--|-----|-----|
| Connection | Handshake first | No handshake |
| Reliability | Packets arrive (or the sender retries) | Some may be lost |
| Order | Reassembled in order | May be jumbled |
| Speed | Slower (checks + ACKs) | Faster |
| Header | About 20 bytes | 8 bytes |
| Best for | Web, files, email, databases | Streaming, games, VoIP, DNS lookups |

**One line:** TCP cares about **correct**. UDP cares about **now**.

---

## When to use TCP

Use TCP when a missing piece is a bug.

- **Web** — HTTP and HTTPS (the page must be complete)
- **Files** — FTP, SFTP
- **Email** — SMTP, IMAP, POP3
- **Databases** — a half query can corrupt meaning

---

## When to use UDP

Use UDP when a delay hurts more than a missing piece.

- **Video** — a dropped frame is better than a frozen screen waiting for a retry
- **Multiplayer games** — old position data is useless; send the newest one
- **Voice / Zoom / Skype** — a tiny gap in audio beats a long lag
- **DNS lookups** — the question is small; if it is lost, ask again

---

## How TCP stays reliable

1. **Three-way handshake** — open the connection before real data
2. **ACK** — receiver says "I got packet N"
3. **Retransmit** — no ACK in time → send again
4. **Checksum** — detect a damaged packet
5. **Sequence numbers** — put bytes back in the original order

```text
Client                Server
  |---- SYN ---------->|     "I want to talk"
  |<--- SYN-ACK -------|     "OK, I heard you"
  |---- ACK ---------->|     "Great, start sending"
  |==== data + ACKs ===|
```

That SYN → SYN-ACK → ACK is the **three-way handshake**.

**Congestion control** (course question): TCP also refuses to flood the network. It starts with a small send window (**slow start**), grows it while ACKs arrive, and **shrinks** it when packets are lost. So one greedy download does not crush everyone else. That extra caution is another reason TCP can feel slower.

---

## Why UDP is faster

- no handshake before the first byte
- no wait for ACKs
- no automatic resend
- smaller header

The time TCP spends "being careful" is the time UDP skips.

---

## Downsides

**TCP**

- slower
- uses more CPU and memory
- bad when every millisecond matters (raw live voice)

**UDP**

- loss is normal
- no built-in error fix
- order is not guaranteed
- the **application** must add reliability if it needs it

---

## If you still need some reliability on UDP

The app adds it, not the protocol:

| Trick | Meaning |
|-------|---------|
| Sequence numbers | Drop or reorder late packets |
| Custom ACKs | Confirm only the important messages |
| FEC (forward error correction) | Extra data so a lost packet can be rebuilt |
| App-level retry | Ask again for one missing piece |

Games and video players do this. They do **not** wait for every packet the way TCP does.

---

## Using both in one product

| Product | UDP | TCP |
|---------|-----|-----|
| Video app | Media (fast) | Pause, seek, login, control |
| Game | Player movement | Chat, inventory, stats |
| VoIP | Voice packets | Call setup |

---

## DNS

DNS **usually uses UDP**: the query is small and must be fast. If the packet is lost, the resolver asks again.

DNS **uses TCP** when the answer is large — for example a **zone transfer** (copying a whole zone between DNS servers).

---

## Firewalls

- **TCP** is easier to track: clear start (handshake) and end.
- **UDP** has no connection, so it is harder to follow and easier to abuse (UDP floods / DDoS).

Firewalls protect UDP with **rate limits**, filtering, and deeper inspection.

---

## Buffering question (say this)

If a video **buffers a lot** on TCP, TCP may be **waiting to resend** a lost packet before it plays the next seconds. Moving the media path toward **UDP** (or a UDP-based protocol such as QUIC) can cut that stall: show the next frames and skip a glitch.

Trade-off: you may see a blur or a skip instead of a spinner. Control actions (login, seek) can stay on TCP.

Many real players still download video chunks over **HTTPS**. The interview point is the trade-off, not a brand name.

---

## Interview lines

- TCP = handshake + ACK + retry + order. UDP = send and hope.
- Web, files, email, DB → TCP. Voice, games, live video, DNS query → UDP.
- UDP apps add their own sequence numbers if they care.
- DNS is UDP first, TCP for big answers.
- Congestion control is why TCP slows down on a lossy network.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is the key difference between TCP and UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

Both are transport protocols.

- **TCP** is connection-oriented. Handshake first. Packets arrive, in order, or they are resent.
- **UDP** is connectionless. No handshake. No promise of delivery, order, or error fix.

| | TCP | UDP |
|--|-----|-----|
| Connection | Yes | No |
| Reliable | Yes | No |
| Speed | Slower | Faster |
| Use | Web, files, email, DB | Stream, game, VoIP, DNS |

**Say this:** TCP chooses correctness. UDP chooses speed.

</details>

### 2. When should you use TCP over UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

When missing or corrupt data is unacceptable.

- HTTP/HTTPS pages
- FTP/SFTP files
- SMTP/IMAP/POP3 mail
- database traffic

</details>

### 3. When should you use UDP over TCP?

<details>
<summary>Show Answer</summary>

**Answer:**

When latency matters more than a perfect copy.

- video (a lost frame is OK)
- multiplayer movement
- voice calls
- DNS lookups (retry if needed)

</details>

### 4. How does TCP ensure reliability?

<details>
<summary>Show Answer</summary>

**Answer:**

- three-way handshake before data
- ACKs
- resend if no ACK
- checksums
- sequence numbers so order is restored

</details>

### 5. What is the three-way handshake?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
SYN      client → server    "let's connect"
SYN-ACK  server → client    "OK"
ACK      client → server    "start"
```

Only after this does the application data flow. UDP skips this step.

</details>

### 6. Why is UDP faster than TCP?

<details>
<summary>Show Answer</summary>

**Answer:**

No connection setup, no ACKs, no resend, smaller header (8 bytes vs about 20). It does not spend time proving delivery.

</details>

### 7. What are the main disadvantages of TCP and UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

TCP: slower, more CPU/memory, poor for raw real-time.

UDP: loss, no built-in correction, unordered delivery. The app must fix that if it cares.

</details>

### 8. How do applications handle reliability when using UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

They add it themselves: sequence numbers, selective ACKs, forward error correction, and application retries. Video and games recover what they can and drop what is already late.

</details>

### 9. Can TCP and UDP be used together?

<details>
<summary>Show Answer</summary>

**Answer:**

Yes.

- video: UDP (or similar) for pictures, TCP for pause/seek/login
- games: UDP for movement, TCP for chat and stats
- calls: UDP for voice, TCP for setup

</details>

### 10. Which protocol does DNS use, TCP or UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

**UDP** for normal lookups (small and fast). **TCP** when the answer is large, such as a zone transfer.

</details>

### 11. How do firewalls handle TCP vs UDP?

<details>
<summary>Show Answer</summary>

**Answer:**

TCP has a visible handshake and close, so it is easier to allow and track. UDP has no session, so firewalls use rate limits, UDP filters, and deeper inspection. UDP floods are a common DDoS shape.

</details>

### 12. Which protocol for a real-time multiplayer game? Why?

<details>
<summary>Show Answer</summary>

**Answer:**

**UDP** for player positions and shots. A late packet is useless; waiting for TCP retry adds lag. Use sequence numbers and keep only the latest state. Use **TCP** for login, chat, and purchases.

</details>

### 13. How does TCP handle congestion control?

<details>
<summary>Show Answer</summary>

**Answer:**

TCP limits how much unacknowledged data is in flight (a window). It grows the window while the path is healthy and cuts it when packets are lost. That is slow start plus congestion avoidance. It protects the network and is why TCP gets slower on a bad link.

</details>

### 14. How does UDP handle packet loss?

<details>
<summary>Show Answer</summary>

**Answer:**

It does **not**. The packet is gone. The app may ignore it (video frame), replace it with the newest update (game), or ask again (DNS).

</details>

### 15. If video buffers too much, would switching from TCP to UDP help?

<details>
<summary>Show Answer</summary>

**Answer:**

Often **yes for the media path**. TCP can pause playback while it retransmits. UDP can keep playing and skip a bad piece. You trade a glitch for less waiting. Keep reliable actions (login, billing, seek commands) on TCP. Say the trade-off; do not claim every stream on the internet is raw UDP.

</details>

**Q&A recap:** Reliable vs fast, handshake, ACK/retransmit, UDP app-level fixes, DNS, firewalls, game + video trade-off.

Next: [02_HTTP.md](02_HTTP.md)
