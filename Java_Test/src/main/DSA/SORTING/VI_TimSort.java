package SORTING;

import java.util.Arrays;

public class VI_TimSort {
    static int MIN_RUN = 4; // small value for easy dry run


    public static void timSort(int[] arr) {
        int n = arr.length;


// Step 1: Sort small runs using insertion sort
        for (int i = 0; i < n; i += MIN_RUN) {
            insertionSort(arr, i, Math.min(i + MIN_RUN - 1, n - 1));
        }


// Step 2: Merge runs like merge sort
        for (int size = MIN_RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);


                if (mid < right) {
                    merge(arr, left, mid, right);
                }
            }
        }
    }


    private static void insertionSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= left && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }


    private static void merge(int[] arr, int l, int m, int r) {
        int len1 = m - l + 1;
        int len2 = r - m;


        int[] left = new int[len1];
        int[] right = new int[len2];


        for (int i = 0; i < len1; i++) left[i] = arr[l + i];
        for (int i = 0; i < len2; i++) right[i] = arr[m + 1 + i];


        int i = 0, j = 0, k = l;
        while (i < len1 && j < len2) {
            if (left[i] <= right[j]) arr[k++] = left[i++];
            else arr[k++] = right[j++];
        }
        while (i < len1) arr[k++] = left[i++];
        while (j < len2) arr[k++] = right[j++];
    }


    public static void main(String[] args) {
        int[] arr = {8, 3, 7, 4, 9, 2, 6, 5};
        timSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
