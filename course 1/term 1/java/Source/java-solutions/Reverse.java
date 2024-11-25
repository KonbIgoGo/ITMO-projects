import scanner.Sc;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Reverse {
    public static void main(String[] args) {
        Sc sc = new Sc(System.in);
        int[][] arr = new int[100][];
        int count2d = 0;

        while (sc.hasNextLine()) {
            count2d += 1;
            int count1d = 0;
            List<String> nums = sc.nextToTheEndOfLine();
//            System.err.println(nums);
//            Sc scNums = new Sc(sc.nextLine());
            int[] subArr = new int[100];
            
            while (count1d < nums.size()) {
                //incr 1d
                count1d += 1;
                if (count1d > subArr.length) {
                    int[] incSubArr = new int[subArr.length * 2];
                    System.arraycopy(subArr, 0, incSubArr, 0, subArr.length);
                    subArr = incSubArr;
                }

                if (nums.get(0).isEmpty()) {
                    subArr = new int[0];
                    count1d = 0;
                    break;
                }
                subArr[count1d - 1] = Integer.parseInt(nums.get(count1d-1));
            }

            if (subArr.length > count1d) {
                int[] copyArr = new int[count1d];
                System.arraycopy(subArr, 0, copyArr, 0, count1d);
                subArr = copyArr;
            }
            //reverse 1d
            for(int i = 0; i < subArr.length/2; i++) {
                int temp = subArr[i];
                subArr[i] = subArr[subArr.length-i-1];
                subArr[subArr.length-i-1] = temp;
            }

            //incr 2d
            if (count2d > arr.length) {
                int[][] incArr = new int[arr.length * 2][];
                System.arraycopy(arr, 0, incArr, 0, arr.length);
                arr = incArr;
            }
            arr[count2d - 1] = subArr;
        }

        if (count2d < arr.length) {
            int[][] copyArr = new int[count2d][];
            System.arraycopy(arr, 0, copyArr, 0, count2d);
            arr = copyArr;
        }
        //reverse 2d
        for(int i = 0; i < arr.length/2; i++) {
            int[] temp = new int[arr[i].length];
            System.arraycopy(arr[i], 0, temp, 0, arr[i].length);
            arr[i] = arr[arr.length-i-1];
            arr[arr.length-i-1] = temp;
        }
        //out
        for (int i = 0; i < count2d; i++) {
            StringBuilder strArr = new StringBuilder();
            if (arr[i] != null) {
                for (int j : arr[i]) {
                    strArr.append(j);
                    strArr.append(" ");
                }
            }
            System.out.println(strArr);
        }
        sc.close();
    }
}
