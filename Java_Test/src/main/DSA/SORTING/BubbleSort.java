package SORTING;

import java.util.Arrays;

public class BubbleSort {
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
*
*
* */