
---

https://docs.oracle.com/javase/8/docs/api/java/lang/String.html

---

# 🔹 Java String Cheat Sheet (Top 20 Methods)

| Method                               | Description              | Example                           |
|--------------------------------------|--------------------------|-----------------------------------|
| length()                             | Returns length of string | "abc".length() → 3                |
| charAt(int i)                        | Returns char at index    | "abc".charAt(1) → 'b'             |
| substring(int begin, int end)        | Extracts substring       | "hello".substring(1,4) → "ell"    |
| substring(int begin)                 | Substring from index     | "hello".substring(2) → "llo"      |
| equals(Object o)                     | Compares values          | "a".equals("a") → true            |
| equalsIgnoreCase(String s)           | Compare ignoring case    | "A".equalsIgnoreCase("a") → true  |
| compareTo(String s)                  | Lexicographic compare    | "a".compareTo("b") → -1           |
| contains(CharSequence s)             | Checks substring         | "hello".contains("ell") → true    |
| startsWith(String prefix)            | Checks prefix            | "hello".startsWith("he") → true   |
| endsWith(String suffix)              | Checks suffix            | "hello".endsWith("lo") → true     |
| indexOf(String s)                    | First occurrence index   | "hello".indexOf("l") → 2          |
| lastIndexOf(String s)                | Last occurrence index    | "hello".lastIndexOf("l") → 3      |
| isEmpty()                            | Checks empty string      | "".isEmpty() → true               |
| trim()                               | Removes spaces           | " hi ".trim() → "hi"              |
| toLowerCase()                        | Converts to lowercase    | "ABC".toLowerCase() → "abc"       |
| toUpperCase()                        | Converts to uppercase    | "abc".toUpperCase() → "ABC"       |
| replace(char old, char new)          | Replace characters       | "aab".replace('a','x') → "xxb"    |
| replaceAll(String regex, String rep) | Replace using regex      | "a1b".replaceAll("\\d","") → "ab" |
| split(String regex)                  | Splits string            | "a,b".split(",") → ["a","b"]      |
| concat(String s)                     | Concatenates strings     | "Hi".concat(" All") → "Hi All"    |

---

## 🔥 Interview Tips
- **equals() vs ==** → always use `equals()` for content comparison
- **String is immutable** → methods return new object
- **indexOf()** returns `-1` if not found
- **replace() vs replaceAll()** → regex vs normal char/sequence

---

# 🔹 Java String – Remaining Methods (with Examples)

## 🔸 Unicode & Code Point Methods
| Method                                    | Description          | Example                           |
|-------------------------------------------|----------------------|-----------------------------------|
| codePointAt(int index)                    | Unicode at index     | "A".codePointAt(0) → 65           |
| codePointBefore(int index)                | Unicode before index | "ABC".codePointBefore(1) → 65     |
| codePointCount(int begin, int end)        | Count Unicode points | "ABC".codePointCount(0,3) → 3     |
| offsetByCodePoints(int index, int offset) | Move index           | "ABC".offsetByCodePoints(0,2) → 2 |

---

## 🔸 Comparison & Matching
| Method                         | Description          | Example                                             |
|--------------------------------|----------------------|-----------------------------------------------------|
| contentEquals(CharSequence cs) | Compare CharSequence | "abc".contentEquals("abc") → true                   |
| contentEquals(StringBuffer sb) | Compare StringBuffer | "abc".contentEquals(new StringBuffer("abc")) → true |
| matches(String regex)          | Full regex match     | "abc123".matches("[a-z]+\\d+") → true               |
| regionMatches(...)             | Compare regions      | "hello".regionMatches(0,"he",0,2) → true            |

---

## 🔸 Searching Variants
| Method                                 | Description          | Example                        |
|----------------------------------------|----------------------|--------------------------------|
| indexOf(int ch)                        | Find char            | "hello".indexOf('e') → 1       |
| indexOf(int ch, int fromIndex)         | From index           | "hello".indexOf('l',2) → 2     |
| indexOf(String str, int fromIndex)     | Substring from index | "hello".indexOf("l",3) → 3     |
| lastIndexOf(int ch)                    | Last char            | "hello".lastIndexOf('l') → 3   |
| lastIndexOf(int ch, int fromIndex)     | Backward search      | "hello".lastIndexOf('l',2) → 2 |
| lastIndexOf(String str, int fromIndex) | Substring backward   | "hello".lastIndexOf("l",2) → 2 |

---

## 🔸 Replacement Variants
| Method                                  | Description         | Example                                 |
|-----------------------------------------|---------------------|-----------------------------------------|
| replace(CharSequence t, CharSequence r) | Replace substring   | "abc".replace("a","x") → "xbc"          |
| replaceFirst(String regex, String rep)  | Replace first match | "a1b2".replaceFirst("\\d","X") → "aXb2" |

---

## 🔸 Split & Join Variants
| Method                                  | Description      | Example                                    |
|-----------------------------------------|------------------|--------------------------------------------|
| split(String regex, int limit)          | Split with limit | "a,b,c".split(",",2) → ["a","b,c"]         |
| join(CharSequence d, CharSequence... e) | Join elements    | String.join("-","a","b") → "a-b"           |
| join(CharSequence d, Iterable e)        | Join iterable    | String.join(",", List.of("a","b")) → "a,b" |

---

## 🔸 Case Conversion (Locale)
| Method                | Description      | Example                              |
|-----------------------|------------------|--------------------------------------|
| toLowerCase(Locale l) | Lowercase locale | "ABC".toLowerCase(Locale.US) → "abc" |
| toUpperCase(Locale l) | Uppercase locale | "abc".toUpperCase(Locale.US) → "ABC" |

---

## 🔸 Subsequence
| Method                    | Description | Example                          |
|---------------------------|-------------|----------------------------------|
| subSequence(int b, int e) | Subsequence | "hello".subSequence(1,4) → "ell" |

---

## 🔸 Conversion Methods
| Method                | Description   | Example                                                      |
|-----------------------|---------------|--------------------------------------------------------------|
| toCharArray()         | To char[]     | Arrays.toString("ab".toCharArray()) → [a, b]                 |
| getBytes()            | To byte[]     | Arrays.toString("A".getBytes()) → [65]                       |
| getBytes(Charset cs)  | Charset bytes | Arrays.toString("A".getBytes(StandardCharsets.UTF_8)) → [65] |
| getBytes(String name) | Charset name  | Arrays.toString("A".getBytes("UTF-8")) → [65]                |
| getChars(...)         | Copy chars    | char[] c=new char[2];"ab".getChars(0,2,c,0) → [a,b]          |

---

## 🔸 Utility Methods
| Method     | Description    | Example                  |
|------------|----------------|--------------------------|
| intern()   | From pool      | "abc".intern() → "abc"   |
| hashCode() | Hash value     | "abc".hashCode() → 96354 |
| toString() | Returns itself | "abc".toString() → "abc" |

---

## 🔸 Static Factory / Value Methods
| Method                        | Description       | Example                                            |
|-------------------------------|-------------------|----------------------------------------------------|
| valueOf(boolean b)            | Boolean to string | String.valueOf(true) → "true"                      |
| valueOf(char c)               | Char to string    | String.valueOf('a') → "a"                          |
| valueOf(char[] data)          | Char array        | String.valueOf(new char[]{'a','b'}) → "ab"         |
| valueOf(char[] d,int o,int c) | Subarray          | String.valueOf(new char[]{'a','b','c'},1,2) → "bc" |
| valueOf(int i)                | Int to string     | String.valueOf(10) → "10"                          |
| valueOf(long l)               | Long to string    | String.valueOf(10L) → "10"                         |
| valueOf(float f)              | Float to string   | String.valueOf(1.5f) → "1.5"                       |
| valueOf(double d)             | Double to string  | String.valueOf(2.5) → "2.5"                        |
| valueOf(Object obj)           | Object to string  | String.valueOf(123) → "123"                        |

---

## 🔸 Copy Methods
| Method                            | Description     | Example                                                |
|-----------------------------------|-----------------|--------------------------------------------------------|
| copyValueOf(char[] data)          | Same as valueOf | String.copyValueOf(new char[]{'x','y'}) → "xy"         |
| copyValueOf(char[] d,int o,int c) | Subarray        | String.copyValueOf(new char[]{'a','b','c'},1,2) → "bc" |

---

## 🔸 Formatting
| Method                                | Description   | Example                                       |
|---------------------------------------|---------------|-----------------------------------------------|
| format(String f,Object... a)          | Format string | String.format("Hi %s","A") → "Hi A"           |
| format(Locale l,String f,Object... a) | Locale format | String.format(Locale.US,"%.2f",1.23) → "1.23" |

---

## ⚠️ Deprecated
| Method                       | Description | Example     |
|------------------------------|-------------|-------------|
| getBytes(int,int,byte[],int) | Deprecated  | Avoid using |

---

# 🔹 Java String – Important Interview Questions & Answers

---

## 1. What is String in Java?
**Answer:**  
String is an object in Java that represents a sequence of characters. It is immutable.

---

## 2. What is immutability of String?
**Answer:**  
Once a String object is created, its value cannot be changed. Any modification creates a new object.

```java
String s = "hello";
s.concat(" world"); // new object created
````

---

## 3. What is SCP (String Constant Pool)?

**Answer:**
A special memory area in heap where string literals are stored to avoid duplication and save memory.

---

## 4. Difference between `==` and `equals()`?

**Answer:**

* `==` → compares reference (memory address)
* `equals()` → compares actual content

```java
String a = "hi";
String b = new String("hi");

System.out.println(a == b);       // false
System.out.println(a.equals(b));  // true
```

---

## 5. Difference between String literal and new keyword?

**Answer:**

* Literal → stored in SCP
* new → creates new object in heap

---

## 6. What does `intern()` do?

**Answer:**
Returns reference from String Constant Pool.

```java
String s = new String("abc");
String s2 = s.intern();
```

---

## 7. Why String is immutable?

**Answer:**

* Security (used in URL, DB, file paths)
* Thread safety
* Performance (SCP reuse)

---

## 8. How many ways to create String?

**Answer:**

1. String literal
2. new keyword
3. char array
4. StringBuilder / StringBuffer

---

## 9. Difference between String, StringBuilder, StringBuffer?

| Feature     | String    | StringBuilder | StringBuffer |
|-------------|-----------|---------------|--------------|
| Mutability  | Immutable | Mutable       | Mutable      |
| Thread-safe | Yes       | No            | Yes          |
| Performance | Slow      | Fast          | Medium       |

---

## 10. What is String pool memory location?

**Answer:**
Stored inside Heap memory (since Java 7).

---

## 11. What happens when we concatenate strings?

**Answer:**
Creates new object (because String is immutable).

```java
String s = "a" + "b"; // optimized at compile time
```

---

## 12. What is difference between `concat()` and `+`?

**Answer:**

* `+` can handle different types
* `concat()` only works with String

---

## 13. What is `compareTo()`?

**Answer:**
Compares strings lexicographically (ASCII/Unicode based).

```java
"a".compareTo("b") → -1
```

---

## 14. What is `hashCode()` of String?

**Answer:**
It is calculated based on character values. Used in hashing (HashMap).

---

## 15. What is `substring()`?

**Answer:**
Returns part of string.

```java
"hello".substring(1,4) → "ell"
```

---

## 16. What is `replace()` vs `replaceAll()`?

**Answer:**

* `replace()` → normal text
* `replaceAll()` → regex

---

## 17. What is `split()`?

**Answer:**
Splits string using regex delimiter.

```java
"a,b,c".split(",") → ["a","b","c"]
```

---

## 18. What is `trim()`?

**Answer:**
Removes leading and trailing spaces.

---

## 19. What is `isEmpty()` vs `isBlank()`?

**Answer:**

* `isEmpty()` → checks length == 0
* `isBlank()` → checks whitespace (Java 11)

---

## 20. Why String is final class?

**Answer:**
To prevent modification and ensure immutability/security.

---

## 21. Can we make String mutable?

**Answer:**
No. Use `StringBuilder` or `StringBuffer`.

---

## 22. What is difference between `StringBuilder` and `StringBuffer`?

**Answer:**

* StringBuilder → fast, not thread-safe
* StringBuffer → thread-safe, slower

---

## 23. What happens if we create many strings?

**Answer:**

* SCP helps reduce duplicates
* But excessive creation may increase memory usage

---

## 24. Can String be null?

**Answer:**
Yes.

```java
String s = null;
```

---

## 25. What is best practice for String comparison?

**Answer:**
Always use `equals()` instead of `==`

---

# 🔹 Java String – Top 30 Coding Interview Questions

---

## 1. Reverse a String
```java
String s = "hello";
System.out.println(new StringBuilder(s).reverse()); // olleh
````

---

## 2. Check Palindrome

```java
String s = "madam";
System.out.println(s.equals(new StringBuilder(s).reverse().toString())); // true
```

---

## 3. Remove Duplicate Characters

```java
String s = "programming";
System.out.println(s.chars().distinct()
    .mapToObj(c -> String.valueOf((char)c))
    .reduce("", String::concat)); // progamin
```

---

## 4. Count Vowels

```java
String s = "hello";
long count = s.toLowerCase().chars()
    .filter(c -> "aeiou".indexOf(c) != -1).count();
System.out.println(count); // 2
```

---

## 5. Count Occurrence of Character

```java
String s = "banana";
long count = s.chars().filter(c -> c=='a').count();
System.out.println(count); // 3
```

---

## 6. First Non-Repeated Character

```java
String s = "swiss";
System.out.println(s.chars()
    .mapToObj(c -> (char)c)
    .filter(c -> s.indexOf(c)==s.lastIndexOf(c))
    .findFirst().get()); // w
```

---

## 7. Check Anagram

```java
String a="listen", b="silent";
char[] c1=a.toCharArray(), c2=b.toCharArray();
Arrays.sort(c1); Arrays.sort(c2);
System.out.println(Arrays.equals(c1,c2)); // true
```

---

## 8. Reverse Words in Sentence

```java
String s="I love Java";
System.out.println(String.join(" ",
    Arrays.stream(s.split(" "))
    .map(w->new StringBuilder(w).reverse())
    .toArray(String[]::new)));
```

---

## 9. Remove White Spaces

```java
String s=" a b c ";
System.out.println(s.replaceAll("\\s","")); // abc
```

---

## 10. Find Duplicate Characters

```java
String s="programming";
s.chars().mapToObj(c->(char)c)
    .filter(c->s.indexOf(c)!=s.lastIndexOf(c))
    .distinct().forEach(System.out::print); // r g m
```

---

## 11. Count Words

```java
String s="I love Java";
System.out.println(s.trim().split("\\s+").length); // 3
```

---

## 12. Convert String to Integer

```java
int num = Integer.parseInt("123");
System.out.println(num); // 123
```

---

## 13. Convert Integer to String

```java
String s = String.valueOf(123);
System.out.println(s); // "123"
```

---

## 14. Check Only Digits

```java
System.out.println("12345".matches("\\d+")); // true
```

---

## 15. Check Only Alphabets

```java
System.out.println("abc".matches("[a-zA-Z]+")); // true
```

---

## 16. Find Longest Word

```java
String s="I love Java programming";
System.out.println(Arrays.stream(s.split(" "))
    .max(Comparator.comparingInt(String::length)).get()); // programming
```

---

## 17. Remove Specific Character

```java
System.out.println("banana".replace("a","")); // bnn
```

---

## 18. Swap Two Strings (without temp)

```java
String a="A", b="B";
a=a+b; b=a.substring(0,a.length()-b.length());
a=a.substring(b.length());
System.out.println(a+" "+b); // B A
```

---

## 19. Check Rotation

```java
String s1="abc", s2="cab";
System.out.println((s1+s1).contains(s2)); // true
```

---

## 20. Compress String

```java
String s="aaabb";
StringBuilder res=new StringBuilder();
int count=1;
for(int i=1;i<=s.length();i++){
    if(i<s.length() && s.charAt(i)==s.charAt(i-1)) count++;
    else { res.append(s.charAt(i-1)).append(count); count=1; }
}
System.out.println(res); // a3b2
```

---

## 21. Remove Non-Alphanumeric

```java
System.out.println("a@b#1!".replaceAll("[^a-zA-Z0-9]","")); // ab1
```

---

## 22. Capitalize Each Word

```java
String s="hello world";
System.out.println(Arrays.stream(s.split(" "))
    .map(w->Character.toUpperCase(w.charAt(0))+w.substring(1))
    .reduce((a,b)->a+" "+b).get()); // Hello World
```

---

## 23. Check Substring

```java
System.out.println("hello".contains("ell")); // true
```

---

## 24. Remove Leading Zeros

```java
System.out.println("00123".replaceFirst("^0+","")); // 123
```

---

## 25. Find All Substrings

```java
String s="abc";
for(int i=0;i<s.length();i++)
 for(int j=i+1;j<=s.length();j++)
  System.out.print(s.substring(i,j)+" "); // a ab abc b bc c
```

---

## 26. Check Balanced Parentheses

```java
String s="()()";
int count=0;
for(char c:s.toCharArray()){
    if(c=='(') count++;
    else count--;
    if(count<0) break;
}
System.out.println(count==0); // true
```

---

## 27. Reverse Without Using Library

```java
String s="hello", rev="";
for(char c:s.toCharArray()) rev=c+rev;
System.out.println(rev); // olleh
```

---

## 28. Find ASCII Value

```java
System.out.println((int)'A'); // 65
```

---

## 29. Count Uppercase Letters

```java
String s="HeLLo";
long count=s.chars().filter(Character::isUpperCase).count();
System.out.println(count); // 3
```

---

## 30. Remove Duplicate Words

```java
String s="java java code";
System.out.println(String.join(" ",
    new LinkedHashSet<>(Arrays.asList(s.split(" "))))); // java code
```

---

# 🔥 Top 10 Most Asked String Interview Questions (with Patterns)

---

## 1. Longest Substring Without Repeating Characters ⭐⭐⭐
### 👉 Pattern: Sliding Window

### Approach:
- Use HashSet
- Expand window, shrink when duplicate found

```java
String s = "abcabcbb";
Set<Character> set = new HashSet<>();
int l=0, max=0;

for(int r=0;r<s.length();r++){
    while(set.contains(s.charAt(r))){
        set.remove(s.charAt(l++));
    }
    set.add(s.charAt(r));
    max = Math.max(max, r-l+1);
}
System.out.println(max); // 3
````

### 🔥 Trap:

* Don’t use nested loops → O(n²)
* Sliding window → O(n)

---

## 2. Longest Palindromic Substring ⭐⭐⭐

### 👉 Pattern: Expand Around Center

```java
String s="babad";
String res="";

for(int i=0;i<s.length();i++){
    String odd = expand(s,i,i);
    String even = expand(s,i,i+1);
    String best = odd.length()>even.length()?odd:even;
    if(best.length()>res.length()) res=best;
}
System.out.println(res);

static String expand(String s,int l,int r){
    while(l>=0 && r<s.length() && s.charAt(l)==s.charAt(r)){
        l--; r++;
    }
    return s.substring(l+1,r);
}
```

### 🔥 Trap:

* Don’t try brute force all substrings → too slow

---

## 3. Group Anagrams ⭐⭐⭐

### 👉 Pattern: HashMap + Sorting

```java
String[] arr = {"eat","tea","tan","ate"};
Map<String,List<String>> map = new HashMap<>();

for(String word: arr){
    char[] ch = word.toCharArray();
    Arrays.sort(ch);
    String key = new String(ch);

    map.computeIfAbsent(key,k->new ArrayList<>()).add(word);
}
System.out.println(map.values());
```

### 🔥 Trap:

* Key must be sorted string

---

## 4. Valid Palindrome (Ignore special chars) ⭐⭐

### 👉 Pattern: Two Pointer

```java
String s="A man, a plan, a canal: Panama";
s = s.replaceAll("[^a-zA-Z0-9]","").toLowerCase();

int l=0,r=s.length()-1;
while(l<r){
    if(s.charAt(l++)!=s.charAt(r--)){
        System.out.println(false);
        return;
    }
}
System.out.println(true);
```

---

## 5. String Compression ⭐⭐

### 👉 Pattern: Two Pointer

```java
String s="aaabb";
StringBuilder res=new StringBuilder();

for(int i=0;i<s.length();){
    int j=i;
    while(j<s.length() && s.charAt(i)==s.charAt(j)) j++;
    res.append(s.charAt(i)).append(j-i);
    i=j;
}
System.out.println(res); // a3b2
```

---

## 6. Check Anagram (Optimized) ⭐⭐

### 👉 Pattern: Frequency Count

```java
String a="listen", b="silent";
int[] freq=new int[26];

for(char c:a.toCharArray()) freq[c-'a']++;
for(char c:b.toCharArray()) freq[c-'a']--;

System.out.println(Arrays.stream(freq).allMatch(x->x==0)); // true
```

### 🔥 Better than sorting → O(n)

---

## 7. Minimum Window Substring ⭐⭐⭐⭐

### 👉 Pattern: Sliding Window (Advanced)

👉 Asked in Amazon/Google

* Maintain required char count
* Expand + shrink window

(Complex → tell interviewer approach first)

---

## 8. First Non-Repeating Character ⭐⭐

### 👉 Pattern: HashMap + Order

```java
String s="swiss";
Map<Character,Integer> map=new LinkedHashMap<>();

for(char c:s.toCharArray())
    map.put(c,map.getOrDefault(c,0)+1);

for(char c:map.keySet()){
    if(map.get(c)==1){
        System.out.println(c); // w
        break;
    }
}
```

---

## 9. Check String Rotation ⭐⭐

### 👉 Pattern: Trick

```java
String s1="abc", s2="cab";
System.out.println((s1+s1).contains(s2)); // true
```

---

## 10. Longest Common Prefix ⭐⭐

### 👉 Pattern: Horizontal Scanning

```java
String[] arr={"flower","flow","flight"};
String prefix=arr[0];

for(int i=1;i<arr.length;i++){
    while(!arr[i].startsWith(prefix)){
        prefix=prefix.substring(0,prefix.length()-1);
    }
}
System.out.println(prefix); // fl
```

---

# 🔥 Final Interview Strategy

## ✅ Always Identify Pattern First:

* Sliding Window → substring problems
* Two Pointer → palindrome, compression
* HashMap → frequency, anagram
* Sorting → grouping

---

# 🔥 Tricky SCP Interview Questions (with Answers & Explanation)

---

## ❓ 1

```java
String s1 = "hello";
String s2 = "hello";

System.out.println(s1 == s2);
```

### ✅ Answer:

```
true
```

### 💡 Why?

* Both refer to same object in SCP

---

## ❓ 2

```java
String s1 = new String("hello");
String s2 = new String("hello");

System.out.println(s1 == s2);
```

### ✅ Answer:

```
false
```

### 💡 Why?

* `new` creates **separate objects in heap**

---

## ❓ 3

```java
String s1 = "hello";
String s2 = new String("hello");

System.out.println(s1 == s2);
```

### ✅ Answer:

```
false
```

### 💡 Why?

* SCP vs Heap object

---

## ❓ 4 (VERY IMPORTANT ⭐)

```java
String s1 = "hello";
String s2 = "he" + "llo";

System.out.println(s1 == s2);
```

### ✅ Answer:

```
true
```

### 💡 Why?

* Compile-time optimization
* JVM converts `"he" + "llo"` → `"hello"`

---

## ❓ 5 (TRICKY ⭐⭐⭐)

```java
String s1 = "hello";
String part = "he";
String s2 = part + "llo";

System.out.println(s1 == s2);
```

### ✅ Answer:

```
false
```

### 💡 Why?

* Runtime concatenation → new object created

## 🔹 Step 1: `s1`

```java
String s1 = "hello";
```

👉 `"hello"` is stored in **SCP (String Constant Pool)**
👉 `s1` points to that object

---

## 🔹 Step 2: `part`

```java
String part = "he";
```

👉 `"he"` is also stored in SCP

---

## 🔹 Step 3: The IMPORTANT LINE

```java
String s2 = part + "llo";
```

👉 This is **NOT compile-time concatenation**

### ❗ Why?

Because:

```java
part = "he"; // stored in variable (not constant expression)
```

👉 JVM does **NOT know value at compile time**

---

## 🔹 What actually happens internally

```java
String s2 = new StringBuilder()
                .append(part)
                .append("llo")
                .toString();
```

👉 So:

* A **new object is created in Heap**
* NOT reused from SCP

---

## 🔹 Memory Diagram

```text
SCP:
   "hello"   ← s1
   "he"

Heap:
   "hello"   ← s2 (new object)
```

👉 Two different objects → different references

---

# 🔥 Key Concept (MOST IMPORTANT)

## ✅ Compile-Time vs Runtime

### ✔️ Compile-Time (goes to SCP)

```java
String s = "he" + "llo"; // optimized → "hello"
```

### ❌ Runtime (creates new object)

```java
String part = "he";
String s = part + "llo"; // new object
```

---

# 🧠 Easy Trick to Remember

👉 Ask yourself:

> “Is everything known at compile time?”

* YES → SCP → same reference
* NO → Heap → new object

---

# 🔥 Quick Comparison

| Code           | Result | Reason       |
|----------------|--------|--------------|
| `"he" + "llo"` | SCP    | Compile-time |
| `part + "llo"` | Heap   | Runtime      |

---

# 🎯 Interview Tip

Say this line:

> “If concatenation involves a variable, it happens at runtime using StringBuilder, creating a new object in heap.”


---

## ❓ 6

```java
final String part = "he";
String s1 = "hello";
String s2 = part + "llo";

System.out.println(s1 == s2);
```

### ✅ Answer:

```
true
```

### 💡 Why?

* `final` → treated as constant means Value cannot change
* Compile-time optimization happens means JVM treats it as a compile-time constant

---

## ❓ 7

```java
String s1 = new String("hello");
String s2 = s1.intern();

System.out.println(s1 == s2);
```

### ✅ Answer:

```
false
```

---

## ❓ 8

```java
String s1 = "hello";
String s2 = new String("hello").intern();

System.out.println(s1 == s2);
```

### ✅ Answer:

```
true
```

### 💡 Why?

* `intern()` returns SCP reference

---

## ❓ 9 (CONFUSING ⭐⭐⭐)

```java
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

System.out.println(s1 == s2); // ?
System.out.println(s1 == s3); // ?
```

### ✅ Answer:

```
true
false
```

---

## ❓ 10

```java
String s1 = "abc";
String s2 = "ab" + "c";
String s3 = new String("abc");

System.out.println(s1 == s2); // ?
System.out.println(s1 == s3); // ?
System.out.println(s1.equals(s3)); // ?
```

### ✅ Answer:

```
true
false
true
```

---

## ❓ 11 (VERY TRICKY ⭐⭐⭐⭐)

```java
String s1 = "Java";
String s2 = new String("Java");

System.out.println(s1 == s2.intern());
```

### ✅ Answer:

```
true
```

---

## ❓ 12 (EDGE CASE)

```java
String s1 = "Java";
String s2 = new String("Java");

s2 = s2.intern();

System.out.println(s1 == s2);
```

### ✅ Answer:

```
true
```

---

# 🔥 MOST CONFUSING CONCEPT

## Compile-Time vs Runtime

### ✅ Compile-Time (SCP)

```java
String s = "a" + "b"; // optimized
```

### ❌ Runtime (Heap)

```java
String a = "a";
String s = a + "b"; // new object
```

---

# 🧠 Golden Rules (Must Remember)

1. `"literal"` → goes to SCP
2. `new String()` → always new object
3. `intern()` → returns SCP reference
4. Compile-time concat → SCP
5. Runtime concat → Heap

---

# 🎯 Interview Killer Answer

If asked tricky question, say:

> “I’ll first check if the string is created at compile-time or runtime, and whether it resides in SCP or heap, then decide reference equality.”

---


