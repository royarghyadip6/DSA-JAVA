# Dijkstra

## Simple idea

Dijkstra finds the cheapest path from one source to every other node, when every edge weight is zero or positive. It grows a set of finished nodes. Each step settles the unfinished node with the smallest distance so far. A min-heap makes that step fast.

It is BFS with weights. If every weight is 1, BFS is enough and simpler.

## Picture

```text
0 --2--> 1 --3--> 3
|                 ^
1                 |
v                 1
2 ----------------+

start at 0
dist: 0:0, 1:inf, 2:inf, 3:inf
settle 0, relax edges: 1 gets 2, 2 gets 1
settle 2 (dist 1), relax: 3 gets 1+1 = 2
settle 1 (dist 2), relax: 3 would get 2+3 = 5, worse, ignore
settle 3
best to 3 is 2, path 0 -> 2 -> 3
```

A negative edge breaks the story. Settling a node means "I will never find a cheaper way." A negative edge later can make that a lie.

## Java example

```java
int[] shortest(int n, java.util.List<int[]>[] graph, int source) {
    int[] dist = new int[n];
    java.util.Arrays.fill(dist, Integer.MAX_VALUE);
    dist[source] = 0;
    java.util.PriorityQueue<int[]> heap = new java.util.PriorityQueue<>(
            java.util.Comparator.comparingInt(a -> a[0]));
    heap.offer(new int[]{0, source}); // distance, node
    while (!heap.isEmpty()) {
        int[] cur = heap.poll();
        int d = cur[0];
        int node = cur[1];
        if (d != dist[node]) continue; // stale pair, a better one was already found
        for (int[] edge : graph[node]) {
            int next = edge[0];
            int weight = edge[1];
            long nd = (long) d + weight;
            if (nd < dist[next]) {
                dist[next] = (int) nd;
                heap.offer(new int[]{dist[next], next});
            }
        }
    }
    return dist;
}
```

With a binary heap this is O((n + m) log n) in the form that may push a node more than once. Each stale pop is skipped by `d != dist[node]`.

`graph[u]` is a list of `{neighbor, weight}`.

## When to use it

Use it for one source, non-negative weights: road networks, cheapest flights with positive prices, a grid where moves cost different positive amounts. Use BFS when every move costs 1. Use Bellman-Ford when a weight can be negative and you also need to notice a negative cycle. Bellman-Ford is O(n * m) and simpler to code badly. Do not use Dijkstra on a negative edge "because the heap still runs."

**Corner case:** `Integer.MAX_VALUE + weight` overflows to a negative number and looks like a great distance. I add in `long`, or I skip the relax when `dist[node]` is still `MAX_VALUE`. A zero-weight edge is allowed. A negative cycle is not this algorithm's job. An unreachable node stays `MAX_VALUE`. I do not return that as a real distance. The heap can contain several pairs for one node. Forgetting the stale check still gives the right answer and does extra work, unless I also mark the node finished on the first pop and ignore later better edges. The usual bug is marking finished too early and also using a decrease that never happens.

## Practice

- Network delay time — Dijkstra, the answer is the max finite distance, or -1 if some node is unreachable.
- Cheapest flights within K stops — this is not plain Dijkstra. The state is `(node, stops used)`. A stop limit means a longer cheap path may be illegal.
- Path with minimum effort on a grid — effort is the max edge on the path, not the sum. The relax step uses `max(dist[node], weight)` instead of a sum. The heap idea stays.
- Swim in rising water — similar "minimax" edge, still a Dijkstra-shaped search.

## Interview Q&A — 6 to 8 years

### S1. Why do you refuse Dijkstra when one edge is negative?

<details>
<summary>Show Answer</summary>

**Answer:**

When I settle a node, I claim no future path can improve it. That claim is true only if extra edges cannot reduce the cost. A negative edge can. I may have already finished the node and moved on. The heap will not go back. Bellman-Ford relaxes every edge n - 1 times and can still improve a distance, then a nth pass that still improves means a negative cycle. I name that switch. I do not "fix" Dijkstra by taking absolute values. That answers a different graph.

**Corner cases:** A negative edge that is not reachable from the source does not affect this source's distances. The algorithm is still the wrong promise if the graph is allowed to contain one. Zero edges are fine. A negative cycle not on any path from the source: distances from the source can still be well defined. Detecting "any negative cycle in the component" is a different question. I say which one the problem asks.

</details>

### S2. The distance became negative on a graph with positive weights. Where did it overflow?

<details>
<summary>Show Answer</summary>

**Answer:**

`dist[node] + weight` was computed in `int`. `Integer.MAX_VALUE` plus a positive weight wraps to a negative int, and the comparison `nd < dist[next]` succeeds. I use `long` for the sum. I also treat `MAX_VALUE` as "unreached" and I do not relax from an unreached node. Returning `MAX_VALUE` to the caller without mapping it to -1 makes the API look like a huge real distance.

**Corner cases:** Weight 0 added to `MAX_VALUE` stays, and a later check thinks the node was reached. I initialize unreached to a value I never add, or I keep a separate `boolean[]`. A `long` distance stored back into an `int` array truncates. If the true distance can exceed `Integer.MAX_VALUE`, the array should be `long`. The heap orders by the int I stored. A truncated key pops in the wrong order.

</details>

### S3. Why does the heap contain the same node more than once?

<details>
<summary>Show Answer</summary>

**Answer:**

The JDK `PriorityQueue` cannot decrease a key in place. When I find a better distance I push a new pair. The old pair is still in the heap. On pop, if its distance does not equal the best known `dist[node]`, it is stale and I skip it. This is correct. The cost is extra log factors and extra memory. A Fibonacci heap can decrease-key in theory. I will not write one in an interview. I mention the stale-pair pattern.

**Corner cases:** Skipping the stale check and also marking the node done on first pop freezes a worse distance if a better pair was still in the heap and I refuse to process it. I either skip stale pairs and process the best one, or I use a real decrease-key. Doing both "mark done" and "ignore worse" without checking I popped the best one is the bug. A node popped with the best distance can be marked done. Further edges cannot improve it if weights are non-negative. That optimization is valid. I only mark done when `d == dist[node]`.

</details>

### S4. "At most K stops" is not the distance array above. What is the state?

<details>
<summary>Show Answer</summary>

**Answer:**

A cheap path with many stops can block a slightly more expensive path that is still legal. If I keep only the best distance per node, I throw away a worse distance that used fewer stops. The state is `(node, stopsUsed)` or `(node, stopsLeft)`. I relax a neighbor only when the stop budget allows it. The heap orders by price. Time grows by a factor of K. Plain Dijkstra is the special case where K is unlimited.

**Corner cases:** K = 0 means only a direct edge, or only the source, depending on whether K counts stops or edges. I restate the problem's meaning with one example before coding. A path that revisits a node with fewer stops can be useful, so a simple visited set per node is wrong. Visited must include the stops left, or I rely on "only push when this state improves." Prices that are zero. A cycle of zero-price edges and a stop limit still terminates because stops decrease. Without the stop limit, a zero cycle can push forever if I am not careful. Non-negative prices plus a settled state `(node, stops)` avoids that.

</details>

**Senior recap:** non-negative weights only, add distances in `long`, push a new pair and skip stale pops, and a stop limit is part of the state.

Next folder: [Greedy](../08_Greedy_DP/01_Greedy.md)
