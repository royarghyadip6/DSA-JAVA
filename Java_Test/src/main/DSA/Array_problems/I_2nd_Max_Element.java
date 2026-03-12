package src.main.DSA.Array_problems;

/**
Find the second largest element
* */

import java.util.Arrays;

public class I_2nd_Max_Element {
    public static void main(String[] args) {
        int []arr = {30,2,1,40,3,50,20,33,0};
        System.out.println("Array is : "+Arrays.toString(arr));
        int max = arr[0];
        int sMax = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                sMax = max;
                max = arr[i];
            }
        }
        System.out.println("Second largest element: "+sMax);
    }
}
