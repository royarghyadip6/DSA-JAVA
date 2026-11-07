# Graph basics

## Simple idea

A graph is a set of nodes and a set of edges. An edge connects two nodes. It may have a direction, and it may have a weight (a cost or a length). A tree is a special graph: connected, no cycle. Most interview graphs are not trees, so you must remember visited nodes.

## Picture

```text
undirected          directed                 weighted directed
1 --- 2             1 --> 2                  1 --3--> 2
|     |             |                        |
3 --- 4             v                        1
                    3                        |
edge {1,2} is       1 can go to 2            v
the same as {2,1}   2 cannot go to 1         3
```

How you store it:

```text
adjacency list, undirected, nodes 0..3
0: 1, 2
1: 0, 3
2: 0
3: 1

matrix: matrix[u][v] = 1 if there is an edge
```

A list uses memory proportional to nodes plus edges. A matrix uses nodes squared, and an edge check is O(1). Interviews usually want the list, because graphs are sparse (far fewer edges than every possible pair).

## Java example

```java
java.util.List<java.util.List<Integer>> undirected(int n, int[][] edges) {
    java.util.List<java.util.List<Integer>> graph = new java.util.ArrayList<>();
    for (int i = 0; i < n; i++) graph.add(new java.util.ArrayList<>());
    for (int[] edge : edges) {
        int u = edge[0];
        int v = edge[1];
        graph.get(u).add(v);
        graph.get(v).add(u); // skip this line if the edge is directed
    }
    return graph;
}
```

Weighted edges store a small pair `(neighbor, weight)` instead of a bare neighbor. I use a record or an `int[]` of length 2. I do not use a floating weight unless the problem forces it.

## When to use it

Use a list for almost every search, shortest path, and topo sort. Use a matrix when the problem already is a grid, or when n is tiny and you check edges constantly. A grid of cells is a graph even when nobody says the word: four neighbors, in-bounds, not a wall.

**Corner case:** a directed edge added in both directions by habit. Then "can I go from A to B?" lies. A self-loop. A duplicate edge. Nodes numbered from 1 through n while the list has size n and index 0 is unused, or the opposite: node n is out of bounds. An undirected edge list that contains both `{u,v}` and `{v,u}` inserts duplicates. A disconnected graph is still a graph. Starting BFS at node 0 and stopping misses the other components.

## Practice

- Build a list from an edge array — the method above. Do it once directed and once undirected.
- Count connected components — a walk from every unvisited node.
- Number of islands on a grid — each land cell is a node, four-direction edges.
- Detect a cycle in an undirected graph — parent pointer, so the edge back to parent is not a cycle.
- Clone a graph — map from original node to the copy, so a cycle does not copy forever.

## Interview Q&A — 6 to 8 years

### S1. List or matrix? Give the numbers you would say.

<details>
<summary>Show Answer</summary>

**Answer:**

Let n be nodes and m be edges. A list is O(n + m) memory and O(degree) to scan neighbors. A matrix is O(n^2) memory and O(1) to test one edge. If n is 10^5 and the graph is sparse, the matrix does not fit. If n is 500 and the problem asks "is there an edge?" in a tight loop, the matrix can be the simpler choice. I say which one I am building before I write the loops.

**Corner cases:** A grid is already a matrix of cells. I usually do not also allocate an adjacency list. I compute the four neighbors from the coordinates. A multigraph (two edges between the same pair) matters for weights. A boolean matrix cannot store two weights. A list can store both. Integer node ids with gaps (1 and 1_000_000) waste a huge list. I compress ids with a map, or I store `Map<Integer, List<Integer>>` and I accept a higher constant.

</details>

### S2. How do you detect a cycle, and why is undirected different from directed?

<details>
<summary>Show Answer</summary>

**Answer:**

Undirected: DFS or BFS with a parent. A visited neighbor that is not the parent is a cycle. Directed: a visited mark is not enough, because a node can be reached twice in a DAG. I use three colors. Gray means "on the current path." A gray neighbor is a back edge, which is a cycle. Black means finished. A black neighbor is fine.

**Corner cases:** The edge back to the parent in an undirected graph is not a cycle. Forgetting the parent reports a cycle on every edge. A self-loop is a cycle in both kinds. A disconnected graph: I run the check from every unvisited node. Directed "course schedule" is this question plus topo sort. Using the undirected parent rule on a directed graph misses a cycle that is not a two-node bounce, or invents one.

</details>

### S3. Clone a graph with a cycle. What must the map store?

<details>
<summary>Show Answer</summary>

**Answer:**

A map from the original node to its copy. When I see a neighbor, if the copy already exists I link to it. I do not call clone again. If I only keep a visited set and always allocate, the cycle allocates until the stack dies, and even a DAG with two parents would copy one node twice and split the graph. The map is both the visited set and the new identity.

**Corner cases:** Node equality. If I key the map by a node whose `equals` is value-based and two nodes share a value, I merge them by mistake. I key by identity (`IdentityHashMap`) or by a stable id field. A null neighbor list. The start node null returns null. Copying values that are mutable and then sharing them between original and copy. I copy the value if mutation must not leak. The second clone of the same graph needs a new map. A leftover map returns the old copy.

</details>

### S4. A grid problem is a graph problem. Which bugs show up only on the grid?

<details>
<summary>Show Answer</summary>

**Answer:**

Bounds: row - 1 and col + 1 must be inside the rectangle before I read the cell. Diagonals: the problem says four directions or eight, and I must not invent the extra four. Marking a cell visited after exploring both directions can let two paths enter it and double-count an island. I mark when I enter. A `boolean[][]` of the right size, not a set of `int[]`, because two `int[]` with the same numbers are not equal.

**Corner cases:** A 1 by 1 grid. An empty grid (`rows = 0`) before I read `grid[0].length`. A jagged grid. Walls versus visited: I need both, or I treat the wall as already unusable. Integer overflow when I pack `(row, col)` into one int if the grid is wider than I assumed. I use `row * cols + col` in a `long` check, or two variables. Mutation of the input grid as the visited mark saves memory and destroys the caller's board. I copy or I restore if the contract says the grid stays.

</details>

**Senior recap:** sparse graphs use lists, undirected cycles need a parent, directed cycles need a color for "on this path," and a clone map must remember the copy before you follow a cycle.

Next: [02_BFS_DFS.md](02_BFS_DFS.md)
