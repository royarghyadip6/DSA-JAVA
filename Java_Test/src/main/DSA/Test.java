import java.util.*;



public class Test {

    public static void main(String[] args) {
        System.out.println("Hello...");
//        long summationNumber = 9999999L;
//        System.out.println("Sum 1 : "+ sum1(summationNumber));
//        System.out.println("Sum 2 : "+ sum2(summationNumber));

        int [] arr = new int[6];
        arr[0] = 5;
        arr[1] = 7;
        arr[2] = 13;
        arr[3] = 35;
        int position = 2;
        int element = 50;
        System.out.println(Arrays.toString(arr)+" Length:"+arr.length);
        System.out.println(insertArrayElement(arr,position,element));
    }

    private static boolean insertArrayElement(int[] arr, int position, int element) {

        return false;
    }


    private static long sum1(long n) {
        long startTime = System.currentTimeMillis();
        long sum =0;
        for (int i = 1; i <= n; i++) {
            sum +=i;
        }
        System.out.println("Time taken: "+ (System.currentTimeMillis() - startTime) );
        return sum;
    }

    private static long sum2(long n) {
        long startTime = System.currentTimeMillis();
        long sum = n*(n+1)/2;
        System.out.println("Time taken: "+ (System.currentTimeMillis() - startTime) );
        return sum;
    }
}

/*
import java.util.List;

public class Solution {
    // DO NOT MODIFY THE LIST. IT IS READ ONLY
    public int findCount(final List<Integer> A, int B) {
        // Binary search to find the first occurrence of B
        int first = binarySearch(A, B, true);
        // If no occurrence of B, return 0
        if (first == -1) {
            return 0;
        }

        // Binary search to find the last occurrence of B
        int last = binarySearch(A, B, false);

        // The number of occurrences is the difference between the last and first indices plus 1
        return last - first + 1;
    }

    // Helper function for binary search
    private int binarySearch(final List<Integer> A, int target, boolean findFirst) {
        int low = 0, high = A.size() - 1, result = -1;

        */
/*
        *  [1,2,3,3,3,4,4,4,4,4,5,5,6,7,8,9,10,11,12,13]
        *   4
        *
        * *//*

        while (low <= high) { // 0,19 / 0,8
            int mid = low + (high - low) / 2; // 9 / 4

            if (A.get(mid) == target) {
                result = mid;  // 9
                if (findFirst) {
                    high = mid - 1;  // 8 Continue searching in the left half for the first occurrence
                } else {
                    low = mid + 1;   // Continue searching in the right half for the last occurrence
                }
            } else if (A.get(mid) < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return result;
    }
}
*/

