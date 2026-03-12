package DSA.SORTING;

import java.util.Arrays;

public class I_BubbleSort {
    public static void main(String[] args) {
        int[] ipArr = {80,20,50,100,1};
        int n = ipArr.length;
        for (int i = 0; i < n ; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (ipArr[j] > ipArr[j+1]) {
                    int temp = ipArr[j];
                    ipArr[j] = ipArr[j+1];
                    ipArr[j+1] = temp;
                }
            }
            System.out.println(Arrays.toString(ipArr));
        }
        System.out.println("Final Array is : "+ Arrays.toString(ipArr));
    }
}

/*
========================
Bubble Sort – Simple Notes
========================

Overview:
- Comparison-based sorting algorithm
- Compares adjacent elements
- Swaps if order is wrong
- Largest element moves to the end
- Simple but slow for large data

Time Complexity:
- Best: O(n)
- Average: O(n^2)
- Worst: O(n^2)

Space Complexity:
- O(1)

Very Simple Example:

Array:
[3, 1, 2]

Step 1:
Compare 3 and 1 → swap
[1, 3, 2]

Step 2:
Compare 3 and 2 → swap
[1, 2, 3]

Final Sorted Array:
[1, 2, 3]

*/
