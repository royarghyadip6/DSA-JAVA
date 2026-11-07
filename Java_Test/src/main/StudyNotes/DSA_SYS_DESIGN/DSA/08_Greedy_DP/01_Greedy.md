# Greedy

## Simple idea

A greedy algorithm takes the best-looking choice right now and does not take it back. That is safe only when a proof says the choice can be part of some best answer. Activity selection is the teaching example: if you always pick the activity that ends first, you leave as much room as possible for the rest.

## Picture

```text
activities (start, end):
A 1----3
B   2------6
C     4--5
D        5----8

sort by end time: A ends 3, C ends 5, B ends 6, D ends 8
pick A (first end)
skip B (starts before 3)
pick C (starts at 4)
skip? D starts at 5, C ends at 5, if touching is allowed pick D
answer: A then C then D, if end == next start is ok
```

Sort by ending time, not by start time and not by shortest duration. Shortest duration can still sit in the middle and block two others.

## Java example

```java
int maxActivities(int[] start, int[] end) {
    int n = start.length;
    Integer[] order = new Integer[n];
    for (int i = 0; i < n; i++) order[i] = i;
    java.util.Arrays.sort(order, java.util.Comparator.comparingInt(i -> end[i]));
    int count = 0;
    int freeAt = Integer.MIN_VALUE;
    for (int i : order) {
        if (start[i] >= freeAt) { // >= if a meeting may start when the previous ends
            count++;
            freeAt = end[i];
        }
    }
    return count;
}
```

Sort is O(n log n). The scan is O(n). This counts how many you can attend. The same loop can collect the indexes.

## When to use it

Use greedy when you can say why the local choice does not block a better global answer. Classic safe cases: activity selection by earliest finish, interval scheduling, jump game (keep the farthest reach), and Huffman coding. If you cannot say the proof in a sentence, try dynamic programming or show a counterexample. "Take the largest coin" fails on coins 1, 3, 4 when the amount is 6: three 4s are impossible, greedy takes 4 then 1 then 1 (three coins) and the best is 3 + 3 (two coins).

**Corner case:** `start >= freeAt` versus `>`. If the problem says a meeting occupies the end instant, the next must start strictly later. Sort stability: two activities with the same end time, either can go next if both start after `freeAt`. A comparator that subtracts end times overflows. I use `comparingInt`. An empty list returns 0. Unsorted input is the real input. I do not assume it is sorted unless the problem says so.

## Practice

- Activity selection / non-overlapping intervals — the method above. "Erase the minimum number of intervals" is n minus this count, with the overlap rule written carefully.
- Jump game — walk left to right, track the farthest index you can reach. If the current index is past that farthest, you are stuck.
- Jump game II — minimum jumps. Also greedy: within the current jump's range, pick the farthest next range.
- Gas station — if the total gas is at least the total cost, one start works. The start is just after the lowest running balance. If the total is short, return -1.
- Assign cookies — sort children and cookies, give the smallest cookie that works.

## Interview Q&A — 6 to 8 years

### S1. Why earliest finish, and what is a counterexample for "shortest activity"?

<details>
<summary>Show Answer</summary>

**Answer:**

The activity that finishes first leaves the largest remaining time. Any other first choice ends later or at the same time, so it cannot allow an extra activity that this one blocked. Shortest duration fails: a short activity from time 1 to 4 blocks two activities, 0–2 and 3–5, that do not overlap each other. Earliest finish picks 0–2, then 3–5. I say that picture if someone proposes shortest or earliest start. Earliest start fails when a long activity starts slightly earlier and covers the whole day.

**Corner cases:** Touching endpoints. I state whether end is exclusive. Two activities with the same end: both are valid greedy picks relative to the proof, and the count is the same if I then continue correctly. The set of activities may differ. A problem that asks for the actual set, not the count, still accepts any optimal set unless it asks for a specific one. Negative times. I do not special-case them if the sort and the comparison are consistent.

</details>

### S2. Jump game says you sometimes cannot reach the end. Where does the greedy check go?

<details>
<summary>Show Answer</summary>

**Answer:**

I keep `farthest`, the maximum index I can reach from any index I have already processed. At index i, if `i > farthest`, this index is unreachable and I return false. Otherwise I update `farthest` with `i + a[i]`. If `farthest` ever reaches n - 1, I can stop early and return true. This is O(n). A BFS of every jump is also correct and usually slower.

**Corner cases:** A zero in the middle. It does not always fail. A previous jump may have sailed over it. It fails only when `farthest` cannot move past it. The last index: you do not need to jump from it. An array of one element is already there, even if the value is 0. `i + a[i]` overflows `int` on a large jump value. I add in `long` or I cap at n - 1. Minimum jumps (jump game II) is a different greedy. Reaching the end is not the same as counting the jumps. I keep a current end and a next end, and I increment the jump when i hits the current end.

</details>

### S3. When do you tell the interviewer that greedy is the wrong first tool?

<details>
<summary>Show Answer</summary>

**Answer:**

When choices interact in a way a local score cannot see: coin systems that are not canonical, knapsack where an item fills capacity that two smaller items would use better, or edit distance. I look for a counterexample before I code. If I find one, I switch to DP and I say the state. If the constraint is n <= 20, I may list subsets instead. Greedy is not "faster DP." It is a different claim.

**Corner cases:** A greedy that passes the sample and fails a hidden case of three items. I volunteer the coin counterexample so they know I know the limit. A proof I cannot quite finish: I say "I believe earliest finish works because it leaves the most time, and I would like to stick with that" rather than pretending I sorted at random. Interval problems that ask for the maximum overlap (meeting rooms) are not activity selection. That one is a sweep line: +1 at start, -1 at end. Using activity selection there counts something else.

</details>

### S4. A Java sort makes the greedy pick unstable. What did the comparator do?

<details>
<summary>Show Answer</summary>

**Answer:**

`Arrays.sort` on objects is stable. `Arrays.sort` on an `int[]` of indexes is not the issue. The bug is a comparator that returns `end[a] - end[b]` and overflows, so the order is not actually by end time, and the greedy proof does not apply to the order I produced. I use `Integer.compare` or `comparingInt`. The second bug is sorting the start array and the end array separately, so start[i] no longer belongs to end[i]. I sort indexes, or I sort pairs.

**Corner cases:** A `record Interval(int s, int e)` sorted by `e` is clear and keeps the pair together. Mutating the input arrays when the caller needs them again. I copy indexes instead. Duplicate intervals. Stability decides which original one I pick first. The count does not change. A comparator that also sorts by start when ends are equal is fine and sometimes required if the problem wants a specific set. I do not claim the algorithm is wrong just because the set differs from the sample, if the sample is one of several optimal sets.

</details>

**Senior recap:** earliest finish leaves the most time, prove it or show a counterexample, jump game tracks the farthest reach, and sort pairs together with a comparator that cannot overflow.

Next: [02_Knapsack.md](02_Knapsack.md)
