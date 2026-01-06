package SORTING;

import java.util.Arrays;

public class V_MergeSort {
    public static void main(String[] args) {
        int[] arr =  {35,22,42,67,71,91,24};
        int start=0,end=arr.length -1 ;
        mergeSort(arr,start,end);
        System.out.println(Arrays.toString(arr));
    }

    private static void mergeSort(int[] arr, int start, int end) {
        if (start < end) {
            int mid = (start+end)/2;
            mergeSort(arr, start, mid);
            mergeSort(arr, mid+1, end);
            combineArray(arr, start, mid, end);
        }

    }

    private static void combineArray(int[] arr, int start, int mid, int end) {
        int len = end - start + 1;
        int[] result = new int[len];
        int i = start, j = mid+1, k = 0;

        while (i<=mid && j<=end) {
            if (arr[i] < arr[j]) {
                result[k++] = arr[i++];
            } else {
                result[k++] = arr[j++];
            }
        }
        while (i<=mid) {
            result[k++] = arr[i++];
        }
        while (j<=end) {
            result[k++] = arr[j++];
        }

        for (int l = 0; l < len; l++) {
            arr[start + l] = result[l];
        }
    }
}
