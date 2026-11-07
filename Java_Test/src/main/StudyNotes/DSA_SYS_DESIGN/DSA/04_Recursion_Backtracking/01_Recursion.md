# Recursion

## Simple idea

A method calls itself on a smaller input, and it stops at a base case. Each call waits on the stack until the deeper call returns. If you cannot see the base case, the method will run until the stack breaks.

## Picture

```text
fact(3)
  fact(2)
    fact(1)
      fact(0) = 1      base case, no more calls
    fact(1) = 1 * 1
  fact(2) = 2 * 1
fact(3) = 3 * 2 = 6

The stack is deepest at fact(0). Then it unwinds.
```

The work happens on the way down (before the recursive call) or on the way up (after it returns). Tree traversals use both.

## Java example

```java
long fact(int n) {
    if (n < 0) throw new IllegalArgumentException("negative");
    if (n == 0) return 1;          // base case
    return n * fact(n - 1);        // smaller input
}
```

`fact(3)` is 6. `fact(21)` does not fit in a `long` (20! is about 2.4e18, 21! overflows). The stack depth is n. Java will not turn this into a loop for you.

An iterative version keeps one local variable and no extra frames. Use it when n can be large. Use recursion when the structure is a tree and the depth is about log n, or when the problem is "all combinations" and you will look at backtracking next.

## When to use it

Use recursion when the problem breaks into the same problem on a smaller piece: a folder of folders, a linked list, a tree, divide-and-conquer. Write the base case first. Then write the one step that gets closer to it.

**Corner case:** no base case, or a step that does not shrink (`fact(n)` calling `fact(n)`). Deep linked lists and skewed trees at n = 10^5 throw `StackOverflowError` even when the logic is right. Recursion on a graph without a visited set never ends if there is a cycle.

## Practice

- Factorial and Fibonacci — then rewrite Fibonacci with a loop or a cache, because the plain two-call version is exponential.
- Sum of a linked list — base case is a null node.
- Binary search, recursive — low and high must move, or the range never shrinks.
- Tower of a tree: max depth — 1 + max(left, right), null is 0.

## Interview Q&A — 6 to 8 years

### S1. The recursive method is correct and dies at n = 20,000. What do you change?

<details>
<summary>Show Answer</summary>

**Answer:**

The algorithm may be O(n) and the stack is still O(n) frames. Java stacks are far smaller than the heap. I rewrite a linear recursion as a loop, or I use an explicit `ArrayDeque` as a stack on the heap. For a tree I can also balance it, but a coding problem will not let me reshape the judge's tree. I do not raise `-Xss` as the design.

**Corner cases:** A recursive method that is tail-shaped still allocates a frame in Java. There is no tail-call optimization to rely on. Mutual recursion (A calls B calls A) has the same depth problem. A bug that shrinks the input very slowly (subtract 1 on a huge n) overflows the stack before it overflows the time limit, so the failure looks like a crash, not a timeout.

</details>

### S2. Fibonacci with two recursive calls is the trap. What is the cost, and what is the fix?

<details>
<summary>Show Answer</summary>

**Answer:**

`fib(n)` calls `fib(n-1)` and `fib(n-2)` and they repeat the same work. The call tree is about O(2^n). A memo array makes each n computed once: O(n) time and O(n) memory. A loop with two rolling variables is O(n) time and O(1) extra memory, and it does not use the call stack. I say which one I am writing.

**Corner cases:** A memo map that is static survives the next call, which can be fine, and it is wrong if the definition of fib changes or tests run in parallel and share it. `fib` of a negative. Integer overflow around fib(47) for `int` and later for `long`. I use `long` and I name the limit. A memo that forgets to store the result before returning still explodes.

</details>

### S3. How do you explain preorder versus postorder on the way down and the way up?

<details>
<summary>Show Answer</summary>

**Answer:**

Code before the recursive calls runs on the way down. That is preorder: see the node, then the children. Code after both calls return runs on the way up. That is postorder: children first, then the node. Inorder on a binary tree is left call, then the node, then the right call. The stack picture is the same. Only the moment you record the node changes.

**Corner cases:** Recording a node both before and after, and printing it twice. A base case that returns after recording a null. I return immediately on null and I do not record it. Passing the same mutable list into every call is what I want for one result list. Passing a new list each time and forgetting to add the children back to the parent loses the result. I will use that on purpose in backtracking, and I will show the undo.

</details>

### S4. A graph recursion worked on a tree and hung on real data. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

A tree has no cycle. A graph can. The recursive DFS must mark a node visited before it walks the neighbors, or a back edge calls the same node again. I mark it when I enter, not when I leave, unless I am doing a specific algorithm that needs three colors (white, gray, black) to detect a back edge. For "visit each node once," one boolean is enough if I set it before the recursive calls.

**Corner cases:** Marking visited after the recursive call is too late. An undirected edge `{u, v}` appears as u -> v and v -> u. If I forbid returning to the parent only, I still need visited for a real cycle elsewhere. A visited set keyed by a mutable node that changes `hashCode`. I key by a stable id. Stack overflow on a long chain that is technically a DAG. Recursion depth is the length of the chain, not the number of branches.

</details>

**Senior recap:** write the base case, count stack frames, memoize a branching recursion, and mark a graph node visited before you recurse.

Next: [02_Backtracking.md](02_Backtracking.md)
