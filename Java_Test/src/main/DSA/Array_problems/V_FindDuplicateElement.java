package src.main.DSA.Array_problems;

public class V_FindDuplicateElement {
    public static void main(String[] args) {
        int [] arr = {23,2, 2, 3 ,4 ,2, 25,1,20,55, 33,55};

        findDuplicateUsingArray(arr);
    }

    private static void findDuplicateUsingArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = i+1; j < arr.length; j++) {

            }
        }
    }
}

/**
 Given an array of integers, find all elements that appear more than once.

 Input: [1, 2, 3, 2, 3, 4, 5, 5]
 Output: [2, 3, 5]

 Input: [1, 1, 1, 2, 3]
 Output: [1]
 */