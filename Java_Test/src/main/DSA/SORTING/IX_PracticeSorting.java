package DSA.SORTING;

import java.util.Arrays;

public class IX_PracticeSorting {
    public static void main(String[] args) {
        int []orgArr = {20,31,50,96,55,77,22,88,51,3,8,69,55,48,91,100};

/*        for (int i = 0; i < orgArr.length; i++) {
            for (int j = i+1 ; j < orgArr.length; j++) {
                if (orgArr[i] > orgArr[j]) {
                    int temp = orgArr[j];
                    orgArr[j] = orgArr[i];
                    orgArr[i] = temp;
                }
            }
        }
        System.out.println(Arrays.toString(orgArr));*/

/*        for (int i = 0; i < orgArr.length; i++) {
            for (int j = 0; j < orgArr.length -i -1 ; j++) {
                if (orgArr[j] > orgArr[j+1]) {
                    int temp = orgArr[j];
                    orgArr[j] = orgArr[j+1];
                    orgArr[j+1] = temp;
                }
            }
        }
        System.out.println(Arrays.toString(orgArr));*/

        for (int i = 0; i < orgArr.length; i++) {
            int max = orgArr[i];
            for (int j = 0; j < orgArr.length -i; j++) {
                if (orgArr[i] > max) {

                }
            }
        }
    }
}
