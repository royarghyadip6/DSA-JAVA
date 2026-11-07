# BFS and DFS

## Simple idea

Both visit every node you can reach. DFS goes deep on one path and then backtracks. BFS visits in layers: first the start, then everything one edge away, then two edges away. On an unweighted graph, the first time BFS reaches a node is a shortest path in number of edges.

## Picture

```text
graph:  0 — 1 — 3
        |
        2

DFS from 0, trying smaller neighbors first:
0, 1, 3, then back, 2
stack: go deep

BFS from 0:
level 0: 0
level 1: 1, 2
level 2: 3
queue: go wide
distance to 3 is 2 edges
```

## Java example

```java
java.util.List<Integer> bfs(java.util.List<java.util.List<Integer>> graph, int start) {
    java.util.List<Integer> order = new java.util.ArrayList<>();
    boolean[] seen = new boolean[graph.size()];
    java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
    seen[start] = true;          // mark when you enqueue
    queue.add(start);
    while (!queue.isEmpty()) {
        int node = queue.removeFirst();
        order.add(node);
        for (int next : graph.get(node)) {
            if (seen[next]) continue;
            seen[next] = true;
            queue.addLast(next);
        }
    }
    return order;
}
```

DFS is the same walk with a stack, or with recursion. Time is O(n + m): each node and each edge once. BFS memory is the widest layer. DFS memory is the deepest path.

## When to use it

Use BFS for shortest path when every edge counts as 1, for level order, and for "minimum steps" on a grid. Use DFS for cycle detection with colors, for topo sort, for connected components, and when you want a simple recursion that explores all paths. DFS does not give shortest paths just because it visits everyone.

**Corner case:** marking visited when you dequeue lets the queue hold many copies of one node. The answer can still be right and the memory is not O(n). A disconnected graph needs an outer loop over every node if the question is "the whole graph," not "reachable from start." Recursion DFS on a long chain throws `StackOverflowError`. An undirected edge is stored twice. A BFS that adds both directions is correct if the list was built that way. Doing it again doubles edges.

## Practice

- Shortest path in a binary grid, four directions — BFS, mark on enqueue.
- Number of provinces / connected components — DFS or BFS from every unvisited node.
- All paths from source to target — DFS that appends and then removes the node (backtracking). BFS is clumsier here because you must copy the path.
- Rotting oranges — multi-source BFS. Put every rotten orange in the queue at time 0.
- Word ladder — BFS on words that differ by one letter. The shortest transformation is the BFS depth.

## Interview Q&A — 6 to 8 years

### S1. Why is the first BFS visit the shortest path, and when is that false?

<details>
<summary>Show Answer</summary>

**Answer:**

BFS expands level by level. Every edge has the same cost, so a path with fewer edges is found before a path with more edges. The first time I reach the target, no shorter route is still waiting in the queue. This is false when edges have different weights, when the cost is not the number of edges, or when I can travel an edge more than once to pick up a state that makes a later route cheaper in a way that is not "fewer edges." Then I use Dijkstra or a BFS on an explicit state.

**Corner cases:** A zero-one weight graph can use a deque: weight 0 goes to the front, weight 1 to the back. That is still a BFS idea and it is not Dijkstra. Negative weights: BFS is wrong, and Dijkstra is also wrong. A grid with a teleport that costs 0. Plain BFS over-counts if I model the teleport as a normal step, or under-counts if I forget it. I put the state in the node I enqueue.

</details>

### S2. You marked visited on dequeue and the judge ran out of memory. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

A node with many parents was enqueued once per parent before any copy was dequeued. The queue held a huge multiset of the same cells. Marking when I enqueue, after the `seen` check, means each node is queued at most once. I still set `seen[start] = true` before the first iteration. The distance I store when I first enqueue is the shortest distance.

**Corner cases:** I must not mark a node seen and then refuse to enqueue a better path in a weighted graph. That bug is the BFS habit applied to Dijkstra. For unweighted BFS, the first enqueue is the best, so the mark is correct. A visited grid that is shared between two test calls must be cleared, or the second call thinks every cell is already seen and returns -1. A `HashSet<int[]>` never hits, so the bug looks like "never seen" and the search explodes the other way.

</details>

### S3. Multi-source BFS: rotting oranges, or nearest exit. What goes in the queue first?

<details>
<summary>Show Answer</summary>

**Answer:**

Every source at time 0. All rotten oranges, or all exits. Then BFS proceeds normally. The distance is the minimum over sources, because the sources compete in one queue. If I run a separate BFS from each source, I repeat work and I can still get the min, at a higher cost. One queue is the interview answer.

**Corner cases:** A fresh orange the wave never reaches. I count fresh oranges at the start and decrement when one rots. If the count is not zero at the end, the answer is -1. Putting only one source in the queue computes the wrong fire. Time is the number of layers, not the number of oranges processed in layer 0. An orange that is already rotten is not fresh. Walls are not enqueued. I do not mark an empty cell as rotten.

</details>

### S4. DFS recursion versus an explicit stack. When does the order differ?

<details>
<summary>Show Answer</summary>

**Answer:**

Recursion visits the first neighbor immediately. An explicit stack that pushes all neighbors in order 0, 1, 2 will pop 2 first, so it explores the reverse order. Both are valid DFS if I only need a visit, a component, or a cycle. They are not the same sequence. If a problem asks for the exact recursive order, I push neighbors in reverse, or I just recurse. I also prefer the explicit stack when the depth can be n.

**Corner cases:** Forgetting `seen` on the explicit stack revisits nodes and can loop on undirected edges. Setting `seen` only after pop is the same explosion as BFS. A recursive method that returns early on the first path and leaves the graph half-marked is fine for "does a path exist" and wrong for a second query on the same array. I allocate `seen` inside the method. Tail recursion is still a frame in Java.

</details>

**Senior recap:** BFS is shortest only when every edge costs the same, mark on enqueue, put every source in the queue at time 0, and an explicit stack pops the last neighbor first.

Next: [03_Topological_Sort.md](03_Topological_Sort.md)
