package DSA.SORTING;

import java.util.Arrays;

public class II_SelectionSort {
    public static void main(String[] args) {
        int [] ipArr = {10,40,30,50,90,20,60,1};
        int n = ipArr.length;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if(ipArr[i]>ipArr[j]) {  // > will sort in ascending order, < sort in descending order
                    int temp = ipArr[i];
                    ipArr[i] = ipArr[j];
                    ipArr[j] = temp;
                }
            }
            System.out.println(Arrays.toString(ipArr));
        }
        System.out.println("Final Array is : "+ Arrays.toString(ipArr));
    }
}



/*
===========================
Selection Sort – Simple Notes
===========================

Overview:
- Comparison-based sorting algorithm
- Repeatedly selects the smallest element
- Places it at the correct position
- Number of swaps is minimum
- Not suitable for large data

Time Complexity:
- Best: O(n^2)
- Average: O(n^2)
- Worst: O(n^2)

Space Complexity:
- O(1)

Very Simple Example:

Array:
[3, 1, 2]

Step 1:
Smallest element is 1
Swap 3 and 1
[1, 3, 2]

Step 2:
Smallest element is 2
Swap 3 and 2
[1, 2, 3]

Final Sorted Array:
[1, 2, 3]
*/
