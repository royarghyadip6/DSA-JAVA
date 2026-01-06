package SORTING;

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
