package src.main.DSA.Array_problems;

import java.util.Arrays;

public class IV_MissingNumber {
    public static void main(String[] args) {

//        Approach 1 – Using Sum Formula
        int []arr = {7, 1, 2, 4, 5, 6};
        System.out.println("Array is : "+ Arrays.toString(arr));

        int n = arr.length +1 ;
        int totalSum = n*(n+1)/2;
        int arrSum =0;
        for (int i = 0; i < arr.length; i++) {
            arrSum+=arr[i];
        }
        System.out.println("Missing number is "+ (totalSum-arrSum));
    }
}

/**
 You are given an array of size n-1 containing distinct numbers from 1 to n. Find the missing number.
 Note: The array may or may not be sorted.
 Input: [7, 1, 2, 4, 5, 6]
 Output: 3

 Input: [1, 2, 4, 5, 6]
 Output: 3
 * */
