package SORTING;

import java.util.Arrays;

public class VII_CountSort {
    public static void main(String[] args) {
        int[] arr = {6,9,2,5,1,7,0,6,2,9,1};
        int maxValue = getMaxValue(arr);
        System.out.println(maxValue);
        int[] result = countSort(arr,maxValue+1);
        System.out.println("Result Array: "+Arrays.toString(result));
    }

    private static int[] countSort(int[] arr, int maxValue) {
        int[] countArr = new int[maxValue];
        int[] tempArr = new int[arr.length];

        // Counting frequency of each number in the array
        for (int i = 0; i < arr.length; i++) {
            ++countArr[arr[i]];
        }
        System.out.println("Count: "+Arrays.toString(countArr)); // [1, 2, 2, 0, 0, 1, 2, 1, 0, 2]

        //
        for (int i = 1; i < countArr.length; i++) {
            countArr[i] += countArr[i-1];
        }
        System.out.println("Updated Count: "+Arrays.toString(countArr)); // [1, 3, 5, 5, 5, 6, 8, 9, 9, 11]
        for (int i = arr.length-1; i >= 0 ; i--) {
            tempArr[--countArr[arr[i]]] = arr[i];
        }
        System.out.println("TempArr: "+Arrays.toString(tempArr)); // [0, 1, 1, 2, 2, 5, 6, 6, 7, 9, 9]
        for (int i = 0; i < arr.length; i++) {
            arr[i] = tempArr[i];
        }
        return arr;
    }

    private static int getMaxValue(int[] arr) {
        int res = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > res) {
                res = arr[i];
            }
        }
        return res;
    }
}
