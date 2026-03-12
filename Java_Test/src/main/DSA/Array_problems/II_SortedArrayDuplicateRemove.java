package src.main.DSA.Array_problems;

import java.util.Arrays;

public class II_SortedArrayDuplicateRemove {
    public static void main(String[] args) {
        int []arr = {1,1,1,2,3,4,4,4,4,5,6,7,8,8,9};
        System.out.println("Array is : "+ Arrays.toString(arr));
        int i = 0;

        for (int j = 1; j < arr.length; j++) {
            if (arr[j] != arr[i]) {
                i++;
                arr[i] = arr[j]; // overwrite duplicate
            }
        }

        for (int j = 0; j <= i; j++) {
            System.out.print(arr[j]+" ");
        }
    }
}

/**
Remove duplicates from a sorted array
Hint: Use two pointers to overwrite duplicates.
* */