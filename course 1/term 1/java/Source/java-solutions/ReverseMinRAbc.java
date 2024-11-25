import scanner.Sc;

import java.util.ArrayList;
import java.util.List;

public class ReverseMinRAbc {
    public static void main(String[] args) {
        char[] transform = new char[] {'a','b','c','d','e','f','g','h','i','j'};
        Sc sc = new Sc(System.in);
        List<List<String>> arr = new ArrayList<>();

        while (sc.hasNextLine()) {
            int count = 0;
//            Sc scNums = new Sc(sc.nextLine());
            List<String> nums = sc.nextToTheEndOfLine();
            List<String> minNumsArr = new ArrayList<>();
            int minn = Integer.MAX_VALUE;
            
            while (count < nums.size()) {
                //incr 1d
                StringBuilder num = new StringBuilder();
                if (!nums.get(count).isEmpty()) {
                    String unprocNum = nums.get(count);
                    if (unprocNum.toCharArray()[0] == '-') {
                        num.append('-');
                    }
                    for (char c : unprocNum.toCharArray()) {
                        for (int i = 0; i < transform.length; i++) {
                            if (c == transform[i]) {
                                num.append(i);

                            }
                        }
                    }

                    minn = Math.min(Integer.parseInt(num.toString()), minn);
                    num.setLength(0);

                    for (char c : Integer.toString(minn).toCharArray()) {
                        if (c == '-') {
                            num.append('-');
                        } else {
                            num.append(transform[Character.digit(c, 10)]);
                        }
                    }

                    minNumsArr.add(num.toString());
                }
                count++;
            }

            //incr 2d

            arr.add(minNumsArr);
//            scNums.close();
        }
        // out
        for (List<String> a : arr) {
            StringBuilder out = new StringBuilder();
            for (String b : a) {
                out.append(b).append(" ");
            }
            System.out.println(out.toString());
        }
        sc.close();
    }
}
