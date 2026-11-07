# Topological sort

## Simple idea

A topological order is a line of nodes such that every directed edge goes forward in the line. It exists only if the directed graph has no cycle. Course prerequisites are the usual story: if A points at B, A comes before B.

Two ways: Kahn's algorithm (indegree and a queue) and DFS (finish a node, then put it in front of the order).

## Picture

```text
edges mean "must come first" -> "comes later"
0 -> 1 -> 3
0 -> 2 -> 3

one valid order: 0, 1, 2, 3
another:         0, 2, 1, 3

a cycle 1 -> 3 -> 1 has no order
```

Kahn:

```text
indegree: 0:0, 1:1, 2:1, 3:2
queue starts with indegree 0, which is node 0
pop 0, reduce 1 and 2
pop 1, reduce 3
pop 2, reduce 3
pop 3
if you popped fewer than n nodes, there was a cycle
```

## Java example

```java
java.util.List<Integer> topoKahn(int n, int[][] edges) {
    java.util.List<java.util.List<Integer>> graph = new java.util.ArrayList<>();
    int[] indegree = new int[n];
    for (int i = 0; i < n; i++) graph.add(new java.util.ArrayList<>());
    for (int[] edge : edges) {
        graph.get(edge[0]).add(edge[1]); // edge[0] before edge[1]
        indegree[edge[1]]++;
    }
    java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
    for (int i = 0; i < n; i++) if (indegree[i] == 0) queue.add(i);
    java.util.List<Integer> order = new java.util.ArrayList<>();
    while (!queue.isEmpty()) {
        int node = queue.removeFirst();
        order.add(node);
        for (int next : graph.get(node)) {
            if (--indegree[next] == 0) queue.add(next);
        }
    }
    if (order.size() != n) return java.util.List.of(); // cycle
    return order;
}
```

O(n + m). DFS version: three colors, a cycle if you see gray, and you add a node to the front of the order when it turns black (after its neighbors).

## When to use it

Use it for prerequisites, build systems, spreadsheet cells, and any "do this before that" on a directed graph. If the graph is undirected, this problem is the wrong tool. If you only need "is there a cycle?", you can stop when a node is not processed or when DFS sees gray. If several orders are valid, say which one you return. Kahn with a normal queue returns nodes in the order they hit indegree 0. A `PriorityQueue` instead of a queue returns the smallest available node first, which is the usual "smallest lexicographical order" follow-up.

**Corner case:** a cycle makes `order.size() < n`. Returning the partial order looks like a schedule that skips a course. I return empty or throw, and I say which. An edge listed twice increments indegree twice and the node never reaches 0. Self-loop: indegree never clears. Nodes that never appear in the edge list still belong in the order if they are real courses. I start the queue with every indegree 0, not only nodes mentioned in edges. Edge direction reversed: I scheduled the dependent first. I confirm "edge means prerequisite -> course" before I code.

## Practice

- Course schedule — return true if `order.size() == n`.
- Course schedule II — return the order, or an empty array if there is a cycle.
- Alien dictionary — compare adjacent words to build edges between letters, then topo. A longer word before a prefix of itself is invalid (`abc` before `ab`).
- Smallest topological order — Kahn with a min-heap instead of a FIFO queue.

## Interview Q&A — 6 to 8 years

### S1. Kahn versus DFS. When do you pick each?

<details>
<summary>Show Answer</summary>

**Answer:**

Kahn is the one I write first. The queue and the indegree array make the cycle check obvious: if I did not output n nodes, there is a cycle. It is iterative, so a long chain will not blow the Java stack. DFS is natural when I am already coloring the graph for another reason, or when the order I want is "finish time." Both are O(n + m). I do not claim they produce the same order. I claim both are valid if any order is accepted.

**Corner cases:** DFS that appends a node when I first see it is a preorder, not a topological order. I append after the neighbors return, then I reverse the list, or I add to the front. Forgetting the reverse returns the opposite schedule. A recursive DFS on n = 10^5 in a chain throws `StackOverflowError` even when the graph is a valid line. Kahn survives. A gray-node check that uses a boolean visited cannot tell "on the stack" from "already finished," so a DAG with a merge is reported as a cycle.

</details>

### S2. The schedule is empty on a graph you can see is a line. What did you point backwards?

<details>
<summary>Show Answer</summary>

**Answer:**

The edge direction. If the input pair is `(course, prerequisite)` and I added an edge course -> prerequisite and also incremented the wrong indegree, the course looks ready and the prerequisite waits forever, or the order is reversed. I write one sentence: "u -> v means u first." I trace two nodes before I handle n nodes. A partial order that still has size n can be the reversed meaning and still pass "no cycle." The tests with one valid order catch it. A diamond may not.

**Corner cases:** 0-index versus 1-index. Course ids from 1 to n stored in an array of length n. The last course throws, or course 0 is a fake node with indegree 0 that I then return to the caller. Duplicate prerequisites. I either ignore the second edge or I document that indegree counts parallel edges. An isolated course must appear. Filtering "only nodes that occur in edges" drops it.

</details>

### S3. How do you return the lexicographically smallest order?

<details>
<summary>Show Answer</summary>

**Answer:**

I replace the queue with a min-heap (`PriorityQueue`) of the nodes whose indegree is 0. Whenever several nodes are ready, I take the smallest id next. That greedy choice is valid because those nodes have no remaining constraints between them. Time becomes O((n + m) log n) because of the heap. A sort of one Kahn result at the end is not the same. The smallest first step is right, and later choices depend on what I released.

**Corner cases:** A heap of nodes that are not yet indegree 0. I only insert when the count hits 0, once. Ties in a FIFO queue depend on insertion order, which depends on the edge-list order. Tests that accept any order hide that. A comparator that uses subtraction overflows. `Integer.compare`. If a cycle exists, the heap version also ends with fewer than n nodes. I still return empty.

</details>

### S4. Alien dictionary builds the wrong graph from the words. Which comparison is illegal?

<details>
<summary>Show Answer</summary>

**Answer:**

I compare adjacent words, not every pair, and I stop at the first character that differs. That difference is one directed edge: the character in the earlier word comes first. If one word is a prefix of the other, the shorter one must come first in a real dictionary. If the longer one is listed first (`abc`, then `ab`), the input is invalid and I return an empty order. I do not invent an edge from a missing character.

**Corner cases:** Several edges between the same letters. Indegree must not count them twice, or I use a set of edges. Letters that never differ still need to appear in the output. I start from every letter, not only the ones with an edge. A cycle in the letter graph (`a` before `b` and `b` before `a` from different pairs) means the dictionary is inconsistent. Same Kahn check. Assuming 26 letters is fine only when the problem says lowercase English. I size the indegree array to the real alphabet.

</details>

**Senior recap:** an order exists only with no directed cycle, Kahn counts processed nodes, edge direction is the whole bug, and a min-heap picks the smallest ready node.

Next: [04_Dijkstra.md](04_Dijkstra.md)
