# 5. Chat (WhatsApp)

## Simple idea

Deliver a message to the right people in **under a second**, keep the order inside that conversation, and still work when the phone drops off the network.

---

## Requirements

One-to-one and groups. Typing and online status. Images, video, files. Sent and read receipts. The same account on several devices, kept in sync.

**Qualities:** sub-second delivery, high availability, end-to-end encryption, millions of users, usable on a bad network.

**Hard parts:** no lost messages and no wrong order after a retry. Presence is noisy. Receipts multiply writes. The server should not be able to read the message if you promise end-to-end encryption.

---

## Numbers

| | Figure |
|--|--------|
| Active users | 100 million per day |
| Messages | 50 each → **5 billion per day** |
| Average rate | about **60,000 messages per second** |
| Peak | about 3× |
| Connections | **20–30 million** online at once |

History, fan-out to devices and groups, and the WebSocket tier are the bottlenecks. Not the login API.

---

## The connection

A phone holds a **WebSocket**. Any server can accept the send, but only the server that holds that socket can push to the phone.

```text
userId + deviceId  →  which gateway owns the socket
```

Store that map in Redis. When a message is saved, publish it. The gateway that owns the socket writes it to the phone. Sticky sessions help. They are not enough after a reconnect, so the registry is the source of routing.

If the user is offline, store the message and send a **push** (FCM). On reconnect, the client sends the last id it has. The server replays newer messages. That is the poor-network path.

**Order** is per conversation, not global. Partition the message log by `conversationId`. Retries use a client message id so a duplicate send is ignored.

**Receipts** (delivered, read) are more writes than the message itself. Put them in a write-optimized store (DynamoDB or Cassandra), or sample them. Do not add a heavy transaction on the user path for every tick.

**Presence and typing** are ephemeral. A Redis key with a short TTL means "online". Do not write PostgreSQL on every heartbeat.

**Groups.** Look up members, then fan out to each member's active devices. Large groups should fan out through a queue, like the news feed, not inside one HTTP call.

**End-to-end encryption.** Clients encrypt. The server stores ciphertext and routes it. It cannot search message text. Group encryption needs a strategy for members who join later (the Signal-style sender keys). Say that it is extra protocol, not "turn on TLS". TLS still protects the connection. It is not end-to-end.

The tech slide says MongoDB for messages. At 5 billion messages a day, say a **partitioned append log** (Cassandra, DynamoDB, or a log) keyed by conversation. MongoDB can hold smaller chat. PostgreSQL holds users and settings. Redis holds presence and the connection map. Kafka buffers fan-out. Media goes to object storage. The message stores the URL.

---

## APIs

WebSocket: `connect` (with the access token), `send_message`, `message_ack`, `typing_indicator`.

| REST | Job |
|------|-----|
| `GET /messages?conversationId=` | History, paginated |
| `POST /media/upload` | Blob URL |
| `GET /presence/{userId}` | Last seen |

JWT on the handshake and on REST.

---

## Diagrams from the slides

Three pictures. The tables are what you store. The middle picture is one message walking to one person or to a group. The last picture is every program in the product.

**Direct and group tables**

```text
DIRECT_CHAT_MESSAGES
  message_id PK, sender_id, receiver_id
  message_content, timestamp, status, message_type
  indexes on sender_id, receiver_id, timestamp

GROUP_CHAT (group_id PK, group_name, created_at, created_by)
    | has members                         | contains
    v                                     v
GROUP_MEMBERS                         GROUP_CHAT_MESSAGES
  group_id FK, user_id FK              message_id PK, group_id FK
  join_date                            sender_id, message_content
                                       timestamp, message_type
                                       indexes on group_id, timestamp
```

**One message**

```text
Direct:
  Sender --> Chat service --> Connection manager --> WebSocket server --> Receiver

Group:
  Sender --> Group chat service --> Connection manager
                                      | reads GROUP_MEMBERS
                                      +--> socket of member A
                                      +--> socket of member B
                                      +--> socket of member C
```

**Sequence (the slide's eight steps)**

```text
Phone --> Gateway --> Connection manager
  1 request socket, gateway checks the token
  2 socket is up
  3 send message
       chat service asks: group? presence?
       storage saves the row
  4 if online, connection manager pushes on the socket
  5 if offline, notification service sends push or SMS
  6 media: chat service --> media service --> object storage
  7 profile: chat service --> user service
  8 presence: chat service --> presence service (Redis, not the message table)
```

**Final picture**

```text
Clients --> CDN (media) ---------------> Media service --> S3
   |
   v
API Gateway --> Connection manager (Redis: who is on which socket)
   |                 |
   |                 v
   |           WebSocket servers (the live pipes)
   |
   +--> User service (PostgreSQL)
   +--> Chat service (message store)
   +--> Group service (members)
   +--> Presence service (Redis)
   |
   +--> Notification queue --> Notification service (FCM / APNs)
```

Solid lines are a request. Dashed lines on the slide are events and storage reads.

## Interview lines

- A registry maps user and device to the socket server. Pub/sub delivers to that server.
- Order and idempotency are per conversation.
- Presence is TTL. History is an append-only log. Offline users get push plus a catch-up cursor.
- End-to-end means the server stores ciphertext.

---

## Interview Q&A

### 1. How does a message reach the other phone?

<details>
<summary>Show Answer</summary>

**Answer:**

The sender's socket hits the chat service. The service stores the message under the conversation id, deduped by a client message id. It looks up the recipient's devices in the connection registry and publishes. The gateway that holds each WebSocket pushes the payload. If nobody is connected, it stores the message and sends a push. On reconnect the client asks for everything after its last id.

</details>

### 2. How do you scale 30 million connections?

<details>
<summary>Show Answer</summary>

**Answer:**

Many gateway nodes, each holding a share of the sockets. A load balancer can use sticky sessions. The registry still records which node owns which device, because the next message may arrive on a different node. Redis pub/sub or a similar bus crosses that gap. Presence stays in Redis with a TTL so heartbeats never hit the message database.

</details>

### 3. What does end-to-end encryption change?

<details>
<summary>Show Answer</summary>

**Answer:**

The payload is encrypted on the device. You can still route, store, and order ciphertext. You cannot read or search it on the server. Keys are exchanged by the clients. A new group member needs a key-distribution story. TLS is still required on the wire. It only protects the hop to your gateway.

</details>

**Q&A recap:** registry plus socket, per-chat order, cursor for sync, ciphertext if you promise end-to-end.

## Interview Q&A — 6 to 8 years

### S1. Two devices are online and the user sends from a third that was offline. What is the source of order?

<details>
<summary>Show Answer</summary>

**Answer:**

The server assigns a per-conversation sequence when it accepts the message. Client clocks do not order the chat. Each device syncs from the last sequence it stored. The offline device uploads with a client message id so a retry is not a second bubble. The server may reject or merge a client id it already applied. Other devices get the server sequence, not the send order of the flaky network.

**Corner cases:** Two offline devices both send "sequence 5" in their own minds. Without server assignment I split history. A server crash after persisting and before ack makes the client retry. The client id saves me. A gap (device has 1..4 and 6). I fetch the gap or I wait briefly, and I do not block other conversations. A device with a corrupted cursor that asks for the whole history. I cap the page and I require auth on that conversation. Read receipts that arrive before the message on another device. I key receipts by message id and I tolerate "receipt before message" by storing the receipt until the message lands.

</details>

### S2. The connection registry says the user is on node A, and node A just died. What do senders do?

<details>
<summary>Show Answer</summary>

**Answer:**

The registry entry has a TTL refreshed by heartbeats. When A dies, the entry expires or a disconnect hook deletes it. The sender stores the message first (the log is the truth), then tries the registry. If there is no live socket, the message waits for sync. I do not lose the message because a push failed. Reconnect uses jitter so all of A's users do not stampede one node.

**Corner cases:** A stale registry entry sends a push to a dead node and I drop the message if I treated push as success. Persist first. Two connections for one user (phone and web). The registry is a list, not one node. I define whether both get the message. A heartbeat that keeps a dead TCP session "online" because the proxy hides the reset. I use an application ping. Presence shown as online for an hour after a crash because TTL was long. I shorten TTL and I accept extra heartbeats. Multi-region: the registry in region 1 is invisible to region 2, so I double-deliver or I miss. One log, and push is best-effort.

</details>

### S3. How do large group chats avoid a hot partition?

<details>
<summary>Show Answer</summary>

**Answer:**

The log of the room is still ordered, so one partition owns that room's writes. I do not pretend I can shard one room's sequence across partitions and keep order. I scale the fan-out of delivery, not the write of the log. For a huge room I store the message once and I do not copy it into a million inboxes. Members pull or receive a lightweight push. I isolate a celebrity room so it cannot block a small chat's partition. Partition by conversation id, and put known huge rooms on their own key range if I must.

**Corner cases:** A worker that fan-outs a 100,000-person room onto the same path as 1:1 chat and adds minutes of lag to both. Separate queues. Unread counts that update one hot row per message per member. I store unread as "last read sequence" on the member and compute the count, or I approximate. Typing indicators at room scale. I drop them. History scroll that reads the primary. Replicas can serve history. The writer stays the sequencer. A user removed from the group who still has a socket. I check membership on send and I stop delivery. Old history policy is a product choice I state (they keep what they had, or they lose it).

</details>

### S4. You promised end-to-end encryption. What can the server no longer do?

<details>
<summary>Show Answer</summary>

**Answer:**

The server stores ciphertext and routes it. It cannot search plaintext, cannot moderate content, and cannot reset a lost device key without a recovery path the user set up. I still do metadata: who talked to whom, and when, unless I design to hide that too. Multi-device means a key per device and a way to enroll a new device (a verification code or a signed prekey). I say this limit before I draw a "smart search" box.

**Corner cases:** A server-side search feature that requires plaintext and quietly breaks the promise. Notifications that preview the message body on the lock screen via a server that can read it. I send a generic push and let the device decrypt. Key backup in my database encrypted with a weak password. A group key that is not rotated when a member leaves, so the ex-member still decrypts new messages. I rotate. A client bug that logs plaintext. The server design cannot fix that, and I mention the client as part of the system.

</details>

**Senior recap:** the server sequence is the order, persist before push, isolate huge rooms, and end-to-end means the server cannot search or moderate plaintext.

Next: [06_Auction.md](06_Auction.md)
