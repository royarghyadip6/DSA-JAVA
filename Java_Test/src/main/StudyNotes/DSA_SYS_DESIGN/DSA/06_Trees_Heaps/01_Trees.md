# Trees

## Simple idea

A tree is nodes and children, with no cycle and exactly one path between two nodes. A binary tree has at most two children, left and right. Traversal means the order in which you visit nodes. The shape decides the stack depth.

## Picture

```text
        1
       / \
      2   3
     / \
    4   5

preorder:  1, 2, 4, 5, 3     node, then children
inorder:   4, 2, 5, 1, 3     left, node, right
postorder: 4, 5, 2, 3, 1     children, then node
level:     1 | 2, 3 | 4, 5   queue, one level at a time
```

## Java example

```java
class TreeNode {
    int value;
    TreeNode left, right;
    TreeNode(int value) { this.value = value; }
}

java.util.List<Integer> inorder(TreeNode root) {
    java.util.List<Integer> out = new java.util.ArrayList<>();
    walk(root, out);
    return out;
}

void walk(TreeNode node, java.util.List<Integer> out) {
    if (node == null) return;
    walk(node.left, out);
    out.add(node.value);
    walk(node.right, out);
}

java.util.List<java.util.List<Integer>> levels(TreeNode root) {
    java.util.List<java.util.List<Integer>> out = new java.util.ArrayList<>();
    if (root == null) return out;
    java.util.ArrayDeque<TreeNode> queue = new java.util.ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
        int size = queue.size(); // freeze this level
        java.util.List<Integer> level = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.removeFirst();
            level.add(node.value);
            if (node.left != null) queue.addLast(node.left);
            if (node.right != null) queue.addLast(node.right);
        }
        out.add(level);
    }
    return out;
}
```

A full walk is O(n) time. Recursion uses O(height) stack. A skewed tree has height n.

## When to use it

Use preorder to copy a tree or write a prefix form. Use inorder on a binary search tree when you want sorted order. Use postorder when a parent needs the children's answers first (height, evaluate an expression). Use a queue for level order. Use a stack if you must avoid recursion.

**Corner case:** null root. A missing child is null, not a node with value 0. Recording nulls in the output invents extra zeros. Height of null is -1 or 0 depending on the definition. I say which: "edges on the longest path" makes a single node height 0 and null height -1. "nodes on the path" makes a single node 1 and null 0. Mixing them is an off-by-one. A skewed tree of 10^5 nodes can overflow the Java stack. I mention an explicit stack.

## Practice

- Inorder, preorder, postorder — recursive, then one of them with a stack.
- Level order — the queue above. Level order II is the same list reversed.
- Maximum depth — 1 + max(left, right), null returns 0 if depth counts nodes.
- Same tree — both null is true, one null is false, then values and both sides.
- Symmetric tree — mirror check: left of one with right of the other.
- Path sum — pass the remaining sum, or the sum so far. A leaf is the only place a path can succeed.

## Interview Q&A — 6 to 8 years

### S1. Why did level order merge two levels into one list?

<details>
<summary>Show Answer</summary>

**Answer:**

I read `queue.size()` once, before the inner loop, and I only dequeue that many nodes. If I loop `while (!queue.isEmpty())` and also enqueue children in that same loop without a frozen size, the children's children join the same list. The snapshot of `size` is the whole bugfix.

**Corner cases:** A null child enqueued by mistake becomes a null dequeue and a NPE, or a fake level. I check before `add`. An empty tree returns an empty list, not a list containing an empty level. A single node is one level. I do not reverse levels unless the problem asks for bottom-up. Reusing one `List` and adding it to `out` on every level aliases every row to the last level. I create a new list per level.

</details>

### S2. Height and diameter are both "walk the tree." What is the difference?

<details>
<summary>Show Answer</summary>

**Answer:**

Height is the longest path from this node down to a leaf. Diameter is the longest path between any two nodes, and it may pass through the current node as `leftHeight + rightHeight`. I compute both in one postorder. Each node returns its height to its parent and updates a global best diameter. Time stays O(n). If I recompute height from scratch at every node, time becomes O(n^2) on a skewed tree.

**Corner cases:** The diameter does not have to pass through the root. A path entirely in the left subtree can win. The best must be updated at every node, not only at the root. Units: if height of null is 0, the path length in edges through a node is `left + right`, and a single node is diameter 0. I state edges versus nodes. An `int` best stored as a field and not reset makes the second call return the first tree's diameter.

</details>

### S3. When do you refuse recursion for a tree walk in Java?

<details>
<summary>Show Answer</summary>

**Answer:**

When the tree can be skewed and n is large (10^5 is a common limit). The logic is fine and `StackOverflowError` is still the result. I use an explicit `ArrayDeque` of nodes and a visited or "go back" state, or I write Morris traversal if I need inorder with O(1) extra memory and I am willing to temporarily edit right pointers and restore them. For a balanced tree, recursion is the clear answer and I say the height is log n.

**Corner cases:** A recursive method that also allocates a list per call can blow the heap before the stack. Morris is easy to get wrong: if I do not restore the thread pointers, I leave a cycle and the next walk never ends. An explicit stack that pushes left and right and processes in the wrong order is still a traversal, just not the one I named. I trace three nodes on the picture before I claim preorder.

</details>

### S4. How do you know a structure is not a tree?

<details>
<summary>Show Answer</summary>

**Answer:**

A tree with n nodes has n - 1 links, every node is reachable from the root, and there is no cycle. In a binary tree interview the input is usually already a tree. If the input is edges, I check those three. A node with two parents is not a tree. A "tree" walk without a visited set on that graph will loop. I do not apply inorder rules to a general graph.

**Corner cases:** A disconnected node: n - 1 edges can still form a cycle plus an isolated node. I need both the edge count and a reachability count. Parent pointers plus child pointers that disagree. I trust one direction. Integer values that repeat are fine in a plain binary tree. They are not fine as a search key in a BST. That is the next note.

</details>

**Senior recap:** freeze the queue size for each level, compute diameter in the same walk as height, recursion depth follows the shape, and a tree has no cycle and no second parent.

Next: [02_BST.md](02_BST.md)
