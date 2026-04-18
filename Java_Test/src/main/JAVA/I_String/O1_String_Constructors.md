
# Constructor Detail

---

Ref: https://docs.oracle.com/javase/8/docs/api/java/lang/String.html

## public String()
> Initializes a newly created String object so that it represents an empty character sequence. Note that use of this constructor is unnecessary since Strings are immutable.

```java
String str = new String();
```

---

## public String(String original)
* Initializes a newly created String object, the newly created string is a copy of the argument string. 
> Note: Unless an explicit copy of original is needed, use of this constructor is unnecessary since Strings are immutable.

> Parameters:
original - A ## 
```java
String str1 = new String("Arghyadip");
System.out.println(str1);
```
---

## public String(char[] value)
* Allocates a new String so that it represents the sequence of characters currently contained in the character array argument. 
* The contents of the character array are copied; Subsequent modification of the character array does not affect the newly created string.

> Parameters:
value - The initial value of the ## 
```java
char [] chars2 = {'a', 'b', 'c', 'd', 'e'};
String str2 = new String(chars2);
System.out.println("Character array to String : "+str2);

```

---

# public String(char[] value,int offset,int count)

* Allocates a new String that contains characters from a subarray of the character array argument. 
* The offset argument is the index of the first character of the subarray and the count argument specifies the length of the subarray.
* The contents of the subarray are copied; subsequent modification of the character array does not affect the newly created string.


> Parameters:
> 
> * value - Array that is the source of characters
> * offset - The initial offset
> * count - The length

> Throws:
> 
> IndexOutOfBoundsException - If the offset and count arguments index characters outside the bounds of the value array

```java
char [] chars3 = {'a', 'b', 'c', 'd', 'e'};
String str3 = new String(chars3,2,3);
System.out.println("Character Array with offset and count: "+str3);
```

---

## public String(int[] codePoints,int offset,int count)

* Allocates a new String that contains characters from a subarray of the Unicode code point array argument.
* The offset argument is the index of the first code point of the subarray and the count argument specifies the length of the subarray. 
* The contents of the subarray are converted to chars; subsequent modification of the int array does not affect the newly created string.
> Parameters:
> * codePoints - Array that is the source of Unicode code points
> * offset - The initial offset
> * count - The length

> Throws:
> * IllegalArgumentException - If any invalid Unicode code point is found in codePoints
> * IndexOutOfBoundsException - If the offset and count arguments index characters outside the bounds of the codePoints array 

> Since: 1.5

```java
int[] codePoints = {72, 101, 108, 108, 111}; // Unicode for "Hello"
String str4 = new String(codePoints, 0, 5);
System.out.println(str4); // Output: Hello
```

---

## public String(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException

👉 It creates a String by decoding a portion of a byte array using a specific character encoding (charset).

Parameters:
* bytes → input byte array. The bytes to be decoded into characters
* offset → starting index. The index of the first byte to decode
* length → number of bytes to read
* charsetName → The name of a supported charset. encoding (e.g., "UTF-8", "UTF-16", "ISO-8859-1")
> Throws:
> * UnsupportedEncodingException - If the named charset is not supported
> * IndexOutOfBoundsException - If the offset and length arguments index characters outside the bounds of the bytes array

> Since: JDK1.1

---

## public String(byte[] bytes, int offset, int length, Charset charset)

> Constructs a new String by decoding the specified subarray of bytes using the specified charset. The length of the new String is a function of the charset, and hence may not be equal to the length of the subarray.
This method always replaces malformed-input and unmappable-character sequences with this charset's default replacement string. The CharsetDecoder class should be used when more control over the decoding process is required.

Parameters:
* bytes - The bytes to be decoded into characters
* offset - The index of the first byte to decode
* length - The number of bytes to decode
* charset - The charset to be used to decode the bytes 

Throws:
* IndexOutOfBoundsException - If the offset and length arguments index characters outside the bounds of the bytes array

> Since: 1.6

---

## public String(byte[] bytes,String charsetName) throws UnsupportedEncodingException

> Constructs a new String by decoding the specified array of bytes using the specified charset. The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.
The behavior of this constructor when the given bytes are not valid in the given charset is unspecified. The CharsetDecoder class should be used when more control over the decoding process is required.

Parameters:
* bytes - The bytes to be decoded into characters
* charsetName - The name of a supported charset

Throws:
* UnsupportedEncodingException - If the named charset is not supported

> Since: JDK1.1

---

## public String(byte[] bytes,Charset charset)

> Constructs a new String by decoding the specified array of bytes using the specified charset. The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.

> This method always replaces malformed-input and unmappable-character sequences with this charset's default replacement string. The CharsetDecoder class should be used when more control over the decoding process is required.

Parameters:
* bytes - The bytes to be decoded into characters
* charset - The charset to be used to decode the bytes

> Since: 1.6

---

## public String(byte[] bytes,int offset,int length)

> Constructs a new String by decoding the specified subarray of bytes using the platform's default charset. The length of the new String is a function of the charset, and hence may not be equal to the length of the subarray.

> The behavior of this constructor when the given bytes are not valid in the default charset is unspecified. The CharsetDecoder class should be used when more control over the decoding process is required.

Parameters:
* bytes - The bytes to be decoded into characters
* offset - The index of the first byte to decode
* length - The number of bytes to decode

Throws:
* IndexOutOfBoundsException - If the offset and the length arguments index characters outside the bounds of the bytes array

> Since: JDK1.1

---

## public String(byte[] bytes)

> Constructs a new String by decoding the specified array of bytes using the platform's default charset. The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.

> The behavior of this constructor when the given bytes are not valid in the default charset is unspecified. The CharsetDecoder class should be used when more control over the decoding process is required.

Parameters:
* bytes - The bytes to be decoded into characters

> Since: JDK1.1

---

## public String(StringBuffer buffer)

> Allocates a new string that contains the sequence of characters currently contained in the string buffer argument. The contents of the string buffer are copied; subsequent modification of the string buffer does not affect the newly created string.

Parameters:
* buffer - A StringBuffer

---

## public String(StringBuilder builder)

>Allocates a new string that contains the sequence of characters currently contained in the string builder argument. The contents of the string builder are copied; subsequent modification of the string builder does not affect the newly created string.
 
>This constructor is provided to ease migration to StringBuilder. Obtaining a string from a string builder via the toString method is likely to run faster and is generally preferred.

Parameters:
* builder - A StringBuilder

> Since: 1.5