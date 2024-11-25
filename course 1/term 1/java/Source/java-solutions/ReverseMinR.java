import scanner.Sc;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ReverseMinR {
    public static void main(String[] args) {
        Sc sc = new Sc(System.in);
        int[][] arr = new int[100][];
        int count2d = 0;

        while (sc.hasNextLine()) {
            count2d += 1;
            int count1d = 0;
//            Sc scNums = new Sc(sc.nextLine());
            List<String> nums = sc.nextToTheEndOfLine();
            int[] minNumsArr = new int[100];
            int minn = Integer.MAX_VALUE;
            
            while (nums.size() > count1d) {
                //incr 1d
                count1d += 1;
                if (count1d > minNumsArr.length) {
                    int[] incMinNumsArr = new int[minNumsArr.length * 2];
                    System.arraycopy(minNumsArr, 0, incMinNumsArr, 0, minNumsArr.length);
                    minNumsArr = incMinNumsArr;
                }
                if (!nums.get(count1d-1).isEmpty()) {
                    minn = Math.min(minn, Integer.parseInt(nums.get(count1d-1)));
                    minNumsArr[count1d - 1] = minn;
                } else {
                    minNumsArr = new int[0];
                }
            }

            if (minNumsArr.length > count1d) {
                int[] cutMinNumsArr = new int[minNumsArr.length];
                cutMinNumsArr = Arrays.copyOfRange(minNumsArr, 0, count1d);
                minNumsArr = cutMinNumsArr;
            }

            //incr 2d
            if (count2d > arr.length) {
                int[][] incArr = new int[arr.length * 2][];
                System.arraycopy(arr, 0, incArr, 0, arr.length);
                arr = incArr;
            }
            arr[count2d - 1] = minNumsArr;
//            scNums.close();
            count1d = 0;
        }
        //out
        for (int i = 0; i < count2d; i++) {
            StringBuilder strArr = new StringBuilder();
            for (int j : arr[i]) {
                strArr.append(Integer.toString(j)).append(" ");
            }
            System.out.println(strArr.toString());
        }
        sc.close();
    }
}
