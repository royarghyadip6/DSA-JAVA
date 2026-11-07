# Stack

## Simple idea

A stack is last in, first out. You push onto the top and you pop from the top. The nearest unmatched item is the one you just pushed. That is why stacks answer "what is the next greater element?" and "are these parentheses valid?"

In Java, use `ArrayDeque` as a stack. Do not use the old `java.util.Stack` class. It is synchronized and inherits from `Vector`.

## Picture

Next greater element to the right. The stack holds indexes whose values are still waiting for a greater value.

```text
values:  2  1  3
stack:   [index of 2]
see 1: 1 is smaller, push it. stack waits: 2, then 1
see 3: 3 is greater than 1, so 1's answer is 3
         3 is greater than 2, so 2's answer is 3
answers: 3, 3, -1
```

Parentheses:

```text
"( [ ] )"
push ( push [   then ] matches [   then ) matches (
stack ends empty -> valid
"( ]"  ] does not match (  -> invalid
```

## Java example

```java
int[] nextGreater(int[] a) {
    int[] ans = new int[a.length];
    java.util.Arrays.fill(ans, -1);
    java.util.ArrayDeque<Integer> stack = new java.util.ArrayDeque<>();
    for (int i = 0; i < a.length; i++) {
        while (!stack.isEmpty() && a[stack.peek()] < a[i]) {
            ans[stack.pop()] = a[i];
        }
        stack.push(i);
    }
    return ans;
}

boolean validParentheses(String s) {
    java.util.ArrayDeque<Character> stack = new java.util.ArrayDeque<>();
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else {
            if (stack.isEmpty()) return false;
            char open = stack.pop();
            if (c == ')' && open != '(') return false;
            if (c == ']' && open != '[') return false;
            if (c == '}' && open != '{') return false;
        }
    }
    return stack.isEmpty();
}
```

Both are O(n). Each index is pushed once and popped once.

## When to use it

Use a stack when the match is nested or "the nearest previous unfinished item": parentheses, next greater or smaller, a monotonic range, undo, or a DFS you write yourself. If the nearest item is the oldest, you want a queue.

**Corner case:** `peek` or `pop` on an empty `ArrayDeque` throws `NoSuchElementException`. A closing bracket on an empty stack is invalid, not a crash you should let escape as the answer. Strict `<` versus `<=` changes equal neighbors. For next greater, equals should not pop if you need a strictly greater value. Leftover opens mean the string is invalid. Next-greater leftovers stay -1.

## Practice

- Valid parentheses — the method above.
- Next greater element to the right — monotonic decreasing stack of indexes.
- Daily temperatures — same pattern, store the distance `i - index`.
- Largest rectangle in a histogram — nearest smaller on both sides. Sentinel bars of height 0 make the edges one formula.
- Min stack — a second stack of minimums, push the new min whenever you push a value.
- Evaluate reverse Polish notation — push numbers, pop two operands. Order of the two pops matters.

## Interview Q&A — 6 to 8 years

### S1. Why store indexes on the stack instead of values?

<details>
<summary>Show Answer</summary>

**Answer:**

The answer often needs a distance or a position, not only the greater value. An index can still read the value from the array. A value alone cannot tell two equal values apart or compute `i - j`. I store indexes for next-greater and temperatures. I store characters for parentheses because the position is not required.

**Corner cases:** Duplicate values. Index identity keeps them distinct. A circular next-greater walks the array twice and must not pop an index whose answer would wrap past itself forever. I stop after 2n steps and I never let an index answer with itself unless the problem allows it. A stack of `Integer` indexes boxes every push. For a hot path I mention it. For an interview it is the normal code.

</details>

### S2. `ArrayDeque` versus `Stack` versus an `int[]` you manage. What do you pick?

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayDeque` for interview and for service code that needs a stack of objects. It is not synchronized, and it does not allow a null element. `java.util.Stack` is a legacy synchronized `Vector`. I do not use it. A raw `int[]` plus a size is the fastest when the element is a primitive and the max depth is n. I use that in a tight histogram solution if I want to avoid boxing, and I still explain it as a stack.

**Corner cases:** `ArrayDeque` forbids null. A problem that uses null as a sentinel cannot push it. I use a dummy index instead. `push` can grow the deque. It will not throw a normal "stack overflow" on a large input. It will exhaust memory. `pop` on empty throws. I check `isEmpty` and turn that into the problem's false or -1. Sharing one deque across requests without clearing it makes the second call see the first call's brackets.

</details>

### S3. Largest rectangle in a histogram. What does the stack mean, and where is the off-by-one?

<details>
<summary>Show Answer</summary>

**Answer:**

The stack keeps indexes of increasing height. When a shorter bar arrives, the bar on top cannot extend any farther right. Its width runs from the index now under it (the previous smaller) up to but not including the current index. Height is `a[popped]`. I add a sentinel 0 at both ends, or I handle an empty stack as left bound -1, so the last bars get popped at the end.

**Corner cases:** Width is `right - left - 1`, not `right - left`, when left and right are the nearest strictly smaller indexes. Equal heights: I decide whether the left bound is strictly smaller. If I use `<=`, a later equal bar can start a new rectangle and I must not double-count in a way that drops a wider plateau. All bars equal: one rectangle of height h and width n. An empty histogram is 0. `int` area overflows. I compute height times width in `long`.

</details>

### S4. A min stack that stores the minimum only when it changes misses a pop. How do you keep it O(1)?

<details>
<summary>Show Answer</summary>

**Answer:**

I push the current minimum on every push, onto a parallel stack, even if it equals the old minimum. Then pop removes one value and one minimum together. If I push a minimum only when it is smaller, two equal minimums and one pop make me forget that the minimum is still that value. I can store pairs `(value, minSoFar)` on one stack. Both are O(1) time and O(n) extra memory.

**Corner cases:** Pop on empty. I define an exception. `getMin` on empty. Integer overflow is not the issue. A minimum of `Integer.MIN_VALUE`. I must not use that as an empty sentinel. The second call on a shared stack instance starts with the old minimum if I did not pop everything. The class owns the stacks. The caller does not pass one in and reuse it by accident unless the API says so.

</details>

**Senior recap:** `ArrayDeque` of indexes, each index pushed and popped at most once, width is `right - left - 1` in the histogram, and a min stack pushes a min on every push.

Next: [04_Queue_Deque.md](04_Queue_Deque.md)
