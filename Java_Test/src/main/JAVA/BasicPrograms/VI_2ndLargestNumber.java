package BasicPrograms;

public class VI_2ndLargestNumber {
    public static void main(String[] args) {
        int [] arr = {50,3,6,5,7,8,2,9,10};

        int max = arr[0];
        int nxtMax = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if(arr[i] > max){
                nxtMax = max;
                max = arr[i];
            } else if(arr[i] < max && arr[i] > nxtMax) {
                nxtMax = arr[i];
            }
        }
        System.out.println(nxtMax);
    }
}
