# 4. Real-Time Communication

## Simple idea

**Real-time** means data moves with **almost no wait**. The user does not press refresh.

```text
Chat message, stock tick, game move, "driver is here"
        →  push it now
```

Normal HTTP is a bad fit: the client must **ask**, then the connection often **ends**.

---

## Why plain HTTP struggles

```text
Client: "Any new message?"
Server: "No."
Client: "Any new message?"
Server: "No."
```

That repeat asking is **polling**. It wastes requests and still feels late.

The course alternatives:

| Style | Idea |
|-------|------|
| **Polling** | Ask again and again on a timer |
| **Long polling** | Ask once; server **holds** the call until there is news |
| **SSE** | Server pushes a one-way stream of events over HTTP |
| **WebSocket** | One connection stays open; **both** sides send anytime |

SSE is one-way (server → client), good for live scores or logs. Chat needs both directions, so the course spends its time on **WebSockets** and **long polling**.

---

## WebSockets

A WebSocket is a **long-lived, full-duplex** pipe on **one TCP connection**.

Full-duplex = client and server can talk **at the same time**, without a new HTTP call.

```text
1. Client sends HTTP Upgrade: websocket
2. Server answers 101 Switching Protocols
3. The same connection stays open
4. Either side sends frames whenever it wants
5. Either side can close
```

Handshake (know the headers by name):

```text
GET /chat HTTP/1.1
Host: example.com
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: ...
Sec-WebSocket-Version: 13

HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: ...
```

`Sec-WebSocket-Key` proves this is a real upgrade, not a normal GET. The server hashes it and returns `Sec-WebSocket-Accept`.

After **101**, you are no longer in request/response mode.

| | WebSocket | HTTP |
|--|-----------|------|
| Life | Stays open | Usually ends after one response |
| Who can send | Both | Client starts each call |
| Latency | Low | Higher if you poll |
| Overhead | One connection | New headers every poll |

**Good for:** WhatsApp/Slack/Discord chat, stock prices, multiplayer games, Google Docs / Figma style collaboration.

**Why it wins vs polling:** less header waste, data is pushed, both directions.

---

## Long polling

The client sends HTTP and **waits**. The server does not answer "nothing." It holds the request until data exists (or a timeout).

```text
1. Client GET /notifications
2. Server waits
3. A new event appears → server responds
4. Client immediately opens the next wait
```

| | WebSocket | Long polling |
|--|-----------|----------------|
| Connection | One persistent pipe | Many HTTP requests |
| Overhead | Low | Higher |
| Latency | Lower | Higher |
| Direction | Both ways easily | Mostly server → client per held request |
| Best for | Chat, games, ticks | Occasional notifications |

**Use long polling when**

- WebSockets are blocked (old client, strict firewall)
- updates are rare (email badge, social notification)
- you want to stay on normal HTTP
- you do not want a WebSocket server yet

Course examples: Slack chat → WebSocket. Twitter-style notifications → long polling. Stocks → WebSocket. Some IoT devices → long polling because they wake up rarely.

---

## When the connection dies

WebSocket apps expect drops (Wi-Fi → mobile data).

- **Reconnect** automatically
- **Heartbeat** ping/pong so a dead socket is noticed
- **Backoff** — wait longer between retries so you do not stampede the server

Slack-style clients reconnect and catch up on missed messages.

---

## Load balancers and scale

A WebSocket is **stuck to one server** for its whole life. A normal round-robin LB that assumes short HTTP calls will break it.

What works:

| Trick | Why |
|-------|-----|
| **Sticky sessions** | Same user stays on the same node |
| **Nginx / HAProxy** | Must forward the `Upgrade` header |
| **WebSocket gateway** (for example API Gateway WebSocket APIs) | Managed fan-out |

**Hard parts at millions of users**

- connection state is on a specific machine
- millions of open sockets (memory, file descriptors)
- a message typed on server A must reach a friend on server B
- reconnect storms after a blip

**Usual fix**

```text
Many WebSocket servers
        |
        v
Redis Pub/Sub or Kafka     ← fan-out between servers
        |
Load balancer with stickiness (or a gateway)
```

The socket stays local. The **message bus** is shared.

---

## Interview lines

- Real-time = push with low delay, not "please refresh."
- WebSocket = upgrade to 101, then full-duplex on one TCP connection.
- Long polling = held HTTP request. Fine for rare notifications.
- Scale with sticky LB + Redis/Kafka between socket servers.
- Heartbeat + reconnect + backoff.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is real-time communication, and why is it important?

<details>
<summary>Show Answer</summary>

**Answer:**

Data is exchanged with very little delay, without the user refreshing.

It matters for chat, live scores, stocks, games, and IoT. Waiting for the next poll feels broken.

</details>

### 2. How do WebSockets work, and how do they differ from HTTP?

<details>
<summary>Show Answer</summary>

**Answer:**

Client asks to upgrade. Server returns **101**. The TCP connection stays open. Either side sends frames.

HTTP usually ends after one response and is client-started. WebSocket is persistent, two-way, and cheaper than polling.

</details>

### 3. Explain the WebSocket handshake.

<details>
<summary>Show Answer</summary>

**Answer:**

Client sends `Upgrade: websocket`, `Connection: Upgrade`, `Sec-WebSocket-Key`, version 13.

Server replies **101 Switching Protocols** and `Sec-WebSocket-Accept` (a hash of the key).

Then both sides use WebSocket frames, not new HTTP requests.

</details>

### 4. What is long polling, and how does it work?

<details>
<summary>Show Answer</summary>

**Answer:**

Client calls the server. Server **holds** the HTTP request until there is data, then responds. Client immediately calls again.

It feels live, but it is still HTTP. Gmail-style new-mail checks are the classic picture. Overhead is higher than a WebSocket.

</details>

### 5. What are the advantages of WebSockets over long polling?

<details>
<summary>Show Answer</summary>

**Answer:**

One connection, lower latency, fewer headers, and true two-way sends. Best for chat, stocks, and games.

</details>

### 6. When would you prefer long polling?

<details>
<summary>Show Answer</summary>

**Answer:**

When WebSockets are blocked, updates are infrequent (notifications, mail), you want plain HTTP, or the backend should stay simple. Social feeds and email badges fit.

</details>

### 7. How do WebSockets handle failures?

<details>
<summary>Show Answer</summary>

**Answer:**

Clients reconnect, send ping/pong heartbeats, and back off so retries do not all hit at once. The app should replay anything missed while the socket was down.

</details>

### 8. Can you use WebSockets with load balancers?

<details>
<summary>Show Answer</summary>

**Answer:**

Yes, if the LB understands long connections.

- sticky sessions so the socket stays on one instance
- Nginx/HAProxy must pass the Upgrade headers
- a WebSocket-aware gateway can sit in front

A LB that closes "idle" HTTP quickly will kill the socket.

</details>

### 9. What are the challenges of scaling WebSockets?

<details>
<summary>Show Answer</summary>

**Answer:**

State lives on one server, millions of connections cost memory, routing must be sticky, and failures need reconnect logic.

Fix: sticky LB or a gateway, plus **Redis Pub/Sub or Kafka** so every socket server can publish and receive messages for users connected elsewhere.

</details>

**Q&A recap:** 101 upgrade, full-duplex vs held HTTP, when polling is enough, heartbeat, sticky + bus to scale.

Next: [05_Beyond_REST.md](05_Beyond_REST.md)
