# Linked list

## Simple idea

A node holds a value and a pointer to the next node. There is no index. You cannot jump to the middle. You follow `next`. The usual interview moves are: reverse the pointers, and detect a cycle with two speeds.

## Picture

```text
1 -> 2 -> 3 -> null

reverse:
null <- 1 <- 2 <- 3
                 ^
                 new head

cycle:
1 -> 2 -> 3 -> 4
         ^         |
         +---------+
slow moves 1 step, fast moves 2. If they meet, there is a cycle.
```

A dummy head is a fake node in front. It makes "delete the first real node" the same code as "delete a middle node."

## Java example

```java
class Node {
    int value;
    Node next;
    Node(int value) { this.value = value; }
}

Node reverse(Node head) {
    Node prev = null;
    Node cur = head;
    while (cur != null) {
        Node next = cur.next; // save, or the rest of the list is lost
        cur.next = prev;
        prev = cur;
        cur = next;
    }
    return prev;
}

boolean hasCycle(Node head) {
    Node slow = head;
    Node fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

Reverse is O(n) time and O(1) extra memory. Cycle detection is the same. Use `==` on the nodes, not on the values. Two different nodes can hold the same number.

## When to use it

Use a linked list when you insert or delete at a position you already hold, and you rarely need random access. In Java production code, `ArrayList` and `ArrayDeque` are usually faster. The interview still wants the pointer moves. `LinkedHashMap` is the library form you meet again in the LRU note.

**Corner case:** losing `cur.next` before you save it drops the rest of the list. A cycle makes a naive reverse or length loop infinite. One node. An empty list (`head == null`). A cycle of length 1 (`node.next = node`). Comparing values instead of node identity reports a false cycle when the number 5 appears twice.

## Practice

- Reverse a list — the loop above.
- Reverse between positions left and right — find the node before left, then reverse the stretch, then reconnect.
- Detect a cycle, then find its start — after they meet, put one pointer back at the head and move both one step.
- Merge two sorted lists — dummy head, always attach the smaller node.
- Remove the n-th node from the end — fast leads by n, then both move. Dummy head if you might delete the first node.
- Add two numbers stored as digits in lists — dummy head and a carry. A carry can remain after both lists end.

## Interview Q&A — 6 to 8 years

### S1. How do you find the start of a cycle after Floyd's pointers meet?

<details>
<summary>Show Answer</summary>

**Answer:**

When slow and fast meet, the meeting point is inside the cycle, not necessarily the start. I leave one pointer there and move the other back to the head. I then move both one step at a time. They meet at the start of the cycle. That is the standard result. I use it and I can sketch why: the fast pointer has walked a multiple of the cycle length farther, so the head-to-start distance matches the meet-to-start distance.

**Corner cases:** No cycle: the first loop ends and I return null. I must not run the second loop. A cycle that starts at the head. One node pointing at itself. Finding the start and then "breaking" the cycle by setting `next` to null is a different problem. I only do that if asked, because it destroys the list the caller still holds.

</details>

### S2. Reverse between indexes left and right. Where do people drop a node?

<details>
<summary>Show Answer</summary>

**Answer:**

I keep `before`, the node just outside the reversed stretch. After I reverse from left to right, the old left node is the tail of the stretch and must point at the node that was after right. `before.next` must point at the old right node, which is the new head of the stretch. A dummy node is `before` when left is 1, so I do not special-case a new list head.

**Corner cases:** left = right: no pointer changes. left = 1 without a dummy: the method returns the old head and the list is missing its new front. Indexes that are 1-based in the problem and 0-based in my head. Reversing with a cycle still in the list never finishes. I assume the problem's list is finite unless it says otherwise. Saving `next` after I overwrite it is the same bug as a full reverse.

</details>

### S3. Why is a hand-rolled linked list rare in a Java service, and when is it still right?

<details>
<summary>Show Answer</summary>

**Answer:**

Each node is an object. Walking it jumps around the heap. An `ArrayList` of the same values is one contiguous block and is faster for almost every scan. I use a linked structure when the library already did the careful work: `LinkedHashMap` for insertion order or LRU, `ArrayDeque` for a queue. I write nodes in an interview because the question is about pointers, and I say I would not drop this class into a service as a general list.

**Corner cases:** `LinkedList` in the JDK is a real doubly linked list and still usually loses to `ArrayDeque` for queues, because of allocation. A node class that is not `static` inside an outer instance holds the outer `this` and leaks the service object. I make interview node classes static, or top-level. Concurrent reversal of a shared list is a data race. I do not share the structure without a lock or a concurrent queue from `java.util.concurrent`.

</details>

### S4. Add-two-numbers leaves a wrong extra digit or drops the last carry. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

The loop stopped when both lists were null and ignored a carry of 1. `19 + 1` stored as `9 -> 1` and `1` must become `0 -> 2`. After the loop I append a node if carry is 1. I also keep a dummy head so the first digit is not a special case. I use an `int` sum of two digits plus carry. Digits are 0–9, so the sum fits. I do not use `Integer` and unbox a missing node.

**Corner cases:** Different lengths. A carry that propagates through a run of 9s. Leading zeros in the list, which are least-significant zeros if the list stores digits reversed, and which change the number if the list stores the most-significant digit first. I ask which end is the units place. Returning the dummy instead of `dummy.next` puts a 0 on the front. The second call is safe if I allocate new nodes. Reusing input nodes splices the caller's lists together and the next call sees a corrupted addend.

</details>

**Senior recap:** save `next` before you repoint, compare node identity for cycles, use a dummy head when the first node can disappear, and flush a final carry.

Next: [02_LRU_Cache.md](02_LRU_Cache.md)
