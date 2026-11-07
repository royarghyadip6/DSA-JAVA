# Binary search tree

## Simple idea

A binary search tree (BST) keeps an order. Every value in the left subtree is less than the node. Every value in the right subtree is greater. Inorder traversal then visits values from small to large. Search, insert, and delete follow one path from the root. On a balanced tree that path is about log n. On a skewed tree it is n.

## Picture

```text
        8
       / \
      3   10
     / \    \
    1   6    14

search 6: 6 < 8 go left, 6 > 3 go right, found
insert 4: same path as search, attach under 6 on the left
inorder: 1, 3, 4, 6, 8, 10, 14
```

A valid BST is stronger than "left child < node < right child." The whole subtree must obey the bound. A node 9 placed as the left child of 10 and the right child of 8's left side can break an ancestor.

## Java example

```java
TreeNode search(TreeNode root, int target) {
    TreeNode cur = root;
    while (cur != null) {
        if (target == cur.value) return cur;
        cur = target < cur.value ? cur.left : cur.right;
    }
    return null;
}

TreeNode insert(TreeNode root, int value) {
    if (root == null) return new TreeNode(value);
    if (value < root.value) root.left = insert(root.left, value);
    else if (value > root.value) root.right = insert(root.right, value);
    // equal: ignore, or define a policy. Do not insert a silent duplicate.
    return root;
}
```

Delete has three cases. A leaf is removed. A node with one child is replaced by that child. A node with two children is replaced by the next inorder value (the smallest in the right subtree), and then that successor node is deleted from the right subtree.

I used `TreeNode` from the trees note: `value`, `left`, `right`.

## When to use it

Use a BST when you need ordered dynamic data and you compare keys. In Java, `TreeMap` and `TreeSet` are red-black trees, so the library version stays O(log n). A hand-rolled BST in an interview is not balanced unless you say so. If you only need "have I seen this key?", a `HashSet` is O(1) average and does not keep order.

**Corner case:** duplicates. The picture above rejects them. If the problem allows equals on the right, every search and delete must use the same rule. Validating a BST with only the parent comparison accepts this illegal tree:

```text
      5
     / \
    1   6
       /
      4        4 is in the right subtree of 5, but 4 < 5
```

I pass a low and high bound down the recursion. I use `long` bounds so `Integer.MIN_VALUE` and `MAX_VALUE` are legal node values.

## Practice

- Search and insert — the methods above.
- Validate BST — bounds, not only the two children.
- Delete a node — the three cases.
- Kth smallest — inorder, stop at k. On a `TreeMap` you would not walk n nodes if you had size hints. Here the simple walk is O(n).
- Lowest common ancestor in a BST — the first node where the two targets split, or the node that is one of the targets.

## Interview Q&A — 6 to 8 years

### S1. Why does a local left/right check accept an invalid BST?

<details>
<summary>Show Answer</summary>

**Answer:**

The BST rule is about every ancestor, not only the parent. A value can be a legal child of its parent and still sit on the wrong side of the grandparent. I carry `(low, high)` exclusive bounds. The left call gets a tighter high, the right call gets a tighter low. A node outside its bounds fails the tree. This is O(n) and visits each node once.

**Corner cases:** Using `int` bounds initialized to `MIN_VALUE` and `MAX_VALUE`, then writing `value <= low` when `value` is `MIN_VALUE`. The root looks illegal, or the bound cannot move. I start with `long` low and high at `Long.MIN_VALUE` and `Long.MAX_VALUE`. Duplicates: if equals are forbidden, `value == low` or a duplicate fails. If equals go right, the comparison must allow that side and the bound must too. An inorder list that is sorted is a valid check. Duplicates make "non-decreasing" versus "strictly increasing" the whole question. Building the list uses O(n) memory. The bound walk uses O(height) stack.

</details>

### S2. Delete the node that has two children. What do you copy, and what do you remove?

<details>
<summary>Show Answer</summary>

**Answer:**

I find the inorder successor: go right once, then left until there is no left child. I copy that successor's value into the node I wanted to delete. Then I delete the successor from the right subtree. The successor has no left child, so that second delete is the easy case (leaf or one right child). I do not try to rewire both subtrees of the original node by hand if this copy works.

**Corner cases:** The successor is the immediate right child. The second delete must still run, or I leave a duplicate value. Equal values: successor search must follow the same duplicate policy as insert. Deleting the root returns the new root. If the caller ignores the return value, the tree still points at a removed root. I always `root = delete(root, value)`. A skewed tree makes delete O(n). I say the tree is not balanced.

</details>

### S3. `TreeMap` versus a hand-rolled BST versus a hash map. What do you say in a Java interview?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashMap` when order does not matter and I want average O(1). `TreeMap` when I need successor, range queries, or sorted keys, at O(log n), with a comparator that matches `equals`. A hand-rolled BST when the question is "show insert and delete." I mention that a plain BST can degenerate to a list if I insert sorted data. `TreeMap` will not, because it rebalances. Keys must be immutable and the comparator must be consistent. A comparator that returns 0 for unequal keys drops entries.

**Corner cases:** `TreeMap` does not allow a null key if the comparator is natural order. `HashMap` allows one null key. A comparator that subtracts two ints overflows (`a - b` when a is negative and b is large). I use `Integer.compare`. Iteration of `TreeMap` is sorted. Iteration during a structural update throws `ConcurrentModificationException`. It is not a concurrent map. `ConcurrentSkipListMap` is the concurrent sorted map, at a higher cost. I name it only if the service is multi-threaded.

</details>

### S4. Lowest common ancestor in a BST is a walk, not a full search of both sides. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

If both targets are smaller than the node, the ancestor is in the left subtree. If both are larger, it is in the right. If they split, or one equals the node, this node is the ancestor. I follow one path, O(height). The general binary-tree LCA cannot do that, because there is no order, so it must look at both sides. Using the BST shortcut on a plain binary tree returns a wrong node.

**Corner cases:** A target that is not in the tree. This walk can still return a node. I search both targets first if the problem says they might be absent. One target is the ancestor of the other: the walk stops on the target, which is correct. Duplicate values make "smaller" and "larger" ambiguous. I need the problem's rule. An unbalanced tree makes this O(n) time. I do not claim log n unless the tree is balanced.

</details>

**Senior recap:** validate with ancestor bounds in `long`, delete by copying the successor and deleting that node, use `TreeMap` when you need a balanced order, and only use the BST ancestor shortcut when the tree is actually a BST.

Next: [03_Heap.md](03_Heap.md)
