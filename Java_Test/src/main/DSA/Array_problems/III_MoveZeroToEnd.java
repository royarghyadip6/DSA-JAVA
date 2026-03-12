package src.main.DSA.Array_problems;

import java.util.Arrays;

public class III_MoveZeroToEnd {
    public static void main(String[] args) {
        int []arr = {0, 1, 0, 0, 0, 0, 0, 3, 12, 0, 20};
        System.out.println("Array is : "+ Arrays.toString(arr));

        int k = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                arr[k] = arr[i];
                k++;
            }
        }
        System.out.print("Final array: ");
        for (int i = 0; i < arr.length; i++) {
            if (i >k){
                arr[i] = 0;
            }
            System.out.print(arr[i]+" ");
        }
    }
}

/**
 Given an array of integers, move all zeros to the end while maintaining the relative order of non-zero elements.
 Input:  [1, 2, 0, 0, 3, 0, 4]
 Output: [1, 2, 3, 4, 0, 0, 0]
 * */