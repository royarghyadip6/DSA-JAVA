package SORTING;

import java.util.Arrays;

public class VIII_RadixSort {
    public static void main(String[] args) {
        int [] arr = {1,2,493,7,356,47,8,2,44,63,895,97,82,666,2,44,1000};
        int maxNumber = findMaxNumber(arr);
        System.out.println("Maximum value is : "+maxNumber);
        for (int i = 1; 0 < maxNumber/i; i*=10) {
            radixSort(arr,i);
        }

    }

    private static int findMaxNumber(int[] arr) {
        int temp = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > temp) {
                temp = arr[i];
            }
        }
        return temp;
    }

    private static void radixSort(int[] arr, int pos) {
        int n = arr.length;
        int[] count = new int[10];
        for (int i = 0; i < n; i++) {

        }
        System.out.println("Final Array is : "+Arrays.toString(arr));
    }



}
