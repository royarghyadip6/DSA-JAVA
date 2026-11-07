# How to read these notes

You can write Java. You have not had to design a product for a huge number of people. Start here. Then open [00_COURSE_MAP.md](00_COURSE_MAP.md) and follow the folders in order.

A **system** is the whole product: the phone app, your servers, the database, and the wires between them. **System design** is deciding those parts and what happens when many people show up at once, or when one part breaks.

---

## How to read a picture

Every diagram in these notes is the same kind of drawing.

```text
Person on a phone
      |
      |  "please save my order"
      v
Your Java program
      |
      |  "store this row"
      v
Database
```

| You see | It means |
|---------|----------|
| A name in a box | A program, or a place data is stored |
| An arrow | A message. Someone asks. Someone answers |
| A number on an arrow | The order. Do step 1, then step 2 |
| Two arrows into one box | Two different jobs can reach that program |

Read a big picture **left to right, then top to bottom**. Find the person first. Follow one arrow until you reach the database or the reply. Ignore the other arrows until that one path makes sense.

---

## Words that show up everywhere

You do not need to memorise these now. Come back when a note uses one.

| Word | Everyday meaning |
|------|------------------|
| **Client** | The phone, browser, or app the person is holding |
| **Server** | A program that waits for requests and answers them. Your Spring Boot app is a server |
| **Request** | One question. "Give me this page." "Save this order." |
| **API** | A named door on the server. `POST /orders` means "create an order". `GET /orders/5` means "show order 5" |
| **POST** | Send something new to the server |
| **GET** | Ask the server to show something |
| **Database** | The notebook that is still there after the program restarts |
| **Row** | One record in that notebook. One user. One order |
| **Cache** | A small fast copy of something people ask for again and again. Like keeping a phone number on a sticky note instead of opening the phone book |
| **Queue** | A waiting line of jobs. The person gets an answer now. A worker does the slow job later (send the email) |
| **Load balancer** | A receptionist. Many identical servers stand behind one door. The receptionist sends each visitor to a free server |
| **Replica** | A second copy of the database, so a dead disk does not lose the data, and reads can be shared |
| **Latency** | How long one person waits for an answer |
| **Throughput** | How many people you can answer in one second |

Later folders add their own short word lists. Read that list before the numbered files.

---

## How one topic file is built

```text
Simple idea     one sentence and a tiny picture
Example         a normal product (a shop, a chat, a bank)
Details         the real rules
Interview Q&A   tap "Show Answer" only after you try
6 to 8 years    harder questions at the bottom: failure and corner cases
```

If you can explain the simple idea to a friend in two minutes, you can move on. The first interview answers are for later. The "6 to 8 years" block is for someone who has already shipped systems and must talk about what breaks.

---

## How one case study is built

The last folder (URL shortener, chat, YouTube, and the rest) uses the same four steps every time.

```text
1. What can the person do?
2. How many people, roughly?
3. What is the one thing that must not go wrong?
4. A picture of the programs, then which tool you would pick
```

Do not start the case studies until the earlier folders feel ordinary. They reuse every word above.
