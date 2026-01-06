package SORTING;

import java.util.Arrays;
/*
*  two sorted array will be combined and create a final sorted array
* */
public class IV_MergeSort {
    public static void main(String[] args) {
        int[] arr1 = {2,4,5,7,9};
        int[] arr2 = {6,8,11,13};
        int i=0,j=0, idx =0, n = arr1.length, m = arr2.length;
        int[] ans = new int[m+n];
        
        while(i<n || j <m) {
            if (i<n && j<m) {
                if (arr1[i] <= arr2[j]) {
                    ans[idx] = arr1[i];
                    i++;
                } else {
                    ans[idx] = arr2[j];
                    j++;
                }

            } else if (i<n) {
                ans[idx] = arr1[i];
                i++;
            } else {
                ans[idx] = arr2[j];
                j++;
            }
            idx++;
        }
        System.out.println(Arrays.toString(ans));
    }
}
