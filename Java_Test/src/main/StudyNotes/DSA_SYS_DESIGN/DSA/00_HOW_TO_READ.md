# How to read these DSA notes

These notes are for a backend Java engineer. The words stay simple. Each file teaches one pattern you can use in an interview.

Read one file in this order:

1. The simple idea.
2. The picture.
3. The small Java example. Trace it with the sample numbers.
4. The corner case. That is where interviews get hard.
5. Close the file and say the idea out loud.
6. Only then open **Show Answer** on the questions at the bottom.

---

## How to read a picture

```text
index:  0   1   2   3
value: [2,  7,  3,  1]
             ^
             i
```

- The numbers in brackets are the array.
- `i` and `j` are fingers pointing at slots. They are not extra copies of the data.
- An arrow `->` means "this node points at the next node".
- A box under another box is a call stack: the top box is the call that is running now.

---

## Words that show up everywhere

| Word | Plain meaning |
|------|----------------|
| **n** | How many items you have |
| **O(1)** | The work stays about the same even if n grows |
| **O(n)** | One pass over the items |
| **O(n log n)** | The usual cost of a good sort |
| **O(n^2)** | A loop inside a loop over the same data |
| **In-place** | You reuse the given array or list. You do not build a second full copy |
| **Stable** | Equal items keep their original order |
| **Overflow** | The number is bigger than the type can hold. In Java, `int` wraps around |

---

## The interview block

The questions at the bottom are for someone with about 6 to 8 years of backend Java work. They ask what breaks: a second call, a duplicate, a null, a cycle, or a number that does not fit in `int`.

Try the answer first. Then open it.
