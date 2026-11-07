# Backtracking

## Simple idea

Backtracking builds a candidate one choice at a time. If the candidate can still become a valid answer, you go deeper. If not, you undo the last choice and try the next one. The undo is the whole trick. Without it, every branch shares a dirty path.

## Picture

Subsets of `{1, 2}`:

```text
start []
  choose 1          [1]
    choose 2        [1, 2]   record
    undo 2          [1]      record
  undo 1            []
  choose 2          [2]      record
  undo 2            []
answers: [], [1], [1, 2], [2]
```

Permutations do the same, but a used number cannot be chosen again in the same path. Subsets may skip ahead so they do not repeat `{1, 2}` as `{2, 1}`.

## Java example

```java
java.util.List<java.util.List<Integer>> subsets(int[] nums) {
    java.util.List<java.util.List<Integer>> out = new java.util.ArrayList<>();
    pick(nums, 0, new java.util.ArrayList<>(), out);
    return out;
}

void pick(int[] nums, int start, java.util.List<Integer> path,
          java.util.List<java.util.List<Integer>> out) {
    out.add(new java.util.ArrayList<>(path)); // copy, or later undo will erase it
    for (int i = start; i < nums.length; i++) {
        path.add(nums[i]);
        pick(nums, i + 1, path, out);
        path.remove(path.size() - 1);        // undo
    }
}
```

For `{1, 2}` you get `[]`, `[1]`, `[1, 2]`, `[2]`. Time is proportional to the number of subsets times the cost of copying each one, which is O(n * 2^n).

## When to use it

Use it when you must list combinations, permutations, or placements (N queens, a small sudoku). If you only need a count that has overlapping subproblems, dynamic programming is usually faster. If the input is large, 2^n will not pass. Say that before you code.

**Corner case:** adding `path` itself into `out` without copying. The undo then empties every saved answer, and the result is a list of empty lists. Duplicate numbers in the input produce duplicate subsets unless you sort and skip a duplicate at the same depth. Forgetting the undo makes the path only grow.

## Practice

- Subsets — the method above.
- Subsets with duplicates — sort, skip `nums[i] == nums[i - 1]` when `i` is not the start of this loop.
- Permutations — a `used` boolean array, or swap in place and swap back.
- Combination sum — you may reuse a number, so the recursive start index stays `i`, not `i + 1`. Still undo.
- N queens — place one queen per row, undo the column, check the diagonals.

## Interview Q&A — 6 to 8 years

### S1. Why are all the saved subsets empty at the end?

<details>
<summary>Show Answer</summary>

**Answer:**

`out.add(path)` stores the reference. The same list object is mutated by later `add` and `remove`. When the search finishes, that object is empty, and every slot in `out` points at it. I copy at the moment the candidate is valid: `out.add(new ArrayList<>(path))`. The copy is O(depth). I include that in the complexity.

**Corner cases:** Copying only at the leaves misses the partial subsets if the problem wants every prefix. Copying on the way down and also mutating the copy (because I passed the copy and kept editing it) is the same bug one level later. An array I reuse for permutations (`swap` in place) has the same aliasing issue if I save the array instead of `Arrays.copyOf`. A second call that reuses a shared `path` field starts dirty. I create the path in the public method.

</details>

### S2. Duplicates in the input. How do you skip them without skipping a real answer?

<details>
<summary>Show Answer</summary>

**Answer:**

I sort first. In the loop, if `i > start` and `nums[i] == nums[i - 1]`, I `continue`. That skips a duplicate choice at this level. The recursive call already explored that value from the earlier identical number. I do not skip when `i == start`, because that is the first time this value is chosen on this branch, including a second identical number deeper in the subset.

**Corner cases:** Skipping whenever `nums[i] == nums[i - 1]` with no `i > start` check drops `[1, 1]` from input `[1, 1]`. Not sorting first makes the skip rule miss duplicates that are not neighbors. The caller's array is now sorted. I copy if I must not modify the input. Permutations of duplicates use a different skip: at the same depth, skip a number equal to the previous one only when the previous one was not used.

</details>

### S3. What is the complexity you should say, honestly?

<details>
<summary>Show Answer</summary>

**Answer:**

Subsets: 2^n leaves in the recursion tree, and copying a path of length up to n makes O(n * 2^n) time and the same order of output memory. Permutations: O(n * n!). I do not say O(2^n) and forget the copy if the copy is in the code. I also say the stack depth is O(n). If n is 40, I refuse to enumerate and I ask whether a count or a DP answer is enough.

**Corner cases:** Pruning (stop when the remaining sum cannot reach the target) improves typical cases and does not change the worst-case sentence I give. An output so large it exceeds memory is a failed solution even if the algorithm is "optimal." Combination sum with unlimited reuse can recurse forever if I do not advance the index and a candidate is 0, or if I allow 0 to be picked without a depth cap. I forbid non-positive candidates or I move the index.

</details>

### S4. How is backtracking different from DFS on a graph?

<details>
<summary>Show Answer</summary>

**Answer:**

Both walk a tree of choices. Graph DFS marks a node so it is not visited again in any branch. Backtracking marks a choice only for the current path and clears it on the way back, because another branch is allowed to use that number. If I leave the `used` bit set, I generate only one permutation. If I forget to mark it, I reuse a number inside one permutation.

**Corner cases:** N queens: the column mark is path-local and must be undone. A diagonal mark too. Sharing one `boolean[] cols` across the search is correct only with undo. Cloning the board on every call is correct and allocates much more. I undo. A visited set for a word-search on a grid is the same pattern: mark the cell, recurse to four neighbors, unmark. Forgetting the unmark blocks later paths from a cell that is free again.

</details>

**Senior recap:** copy the path when you save it, undo every choice, skip duplicates only after sorting and only at the same depth, and say O(n * 2^n) or O(n * n!) out loud.

Next folder: [Linked list](../05_Linear_Structures/01_Linked_List.md)
