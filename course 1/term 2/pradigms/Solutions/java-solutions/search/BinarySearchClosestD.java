package search;


import java.util.Arrays;

public class BinarySearchClosestD {

    //pred: a != null && x != null
    //post: a' == a
    private static int iterativeBinSearch(int[] a, int x) {
        int l = 0;
        int r = a.length;
        //I = immutable(n) && a.len > 0 && l <= 0 <= a.len-1 && 0 < r <= a.len && minDiff idx in [l, r) &&
        //      && a[0] >= a[1] >= a[2] >= ... >= a[n]


        if (r == 1 || a[0] <= x) {
            //I && r == 1 && a[0] <= x && minDiff idx == 0;
            return 0;
        }

        //I && !P1 = (r == 1 || a[0] <= x)

        if (a[a.length-1] >= x) {
            //I && !P1 && P2 = (a[a.length - 1] >= x) >> minDiff Idx == a.len-1
            return a.length-1;
        }

        //I && !P1 && !P2

        //I2 = l < r-1
        while (l < r-1) {
            //I && I2 && !P1 && !P2
            int m = (l + r) / 2;
            //I && I2 && !P1 && !P2 && m in [l, r];
            if (a[m] <= x) {
                //I && I2 && !P1 && !P2 && m in [l, r] && a[m] <= x;
                r = m;
                //I && I2 && !P1 && !P2 && m in [l, r] && a[m] <= x && r' == m && minDiff Idx <= r';
            } else {
                //I && I2 && !P1 && !P2 && m in [l, r] && a[m] > x;
                l = m;
                //I && I2 && !P1 && !P2 && m in [l, r] && a[m] > x && l' = m && minDiff Idx > l';
            }
        }
        // I && !I2 && !P1 && !P2 && minDiff Idx == l || minDiff Idx == r;
        if (a[l] > x && a[r] < x) {
            // I && !I2 && !P1 && !P2 && minDiff Idx == l || minDiff Idx == r &&
            // &&  a[l] > x && a[r] < x;
            if (Math.abs(a[l] - x) <= Math.abs(a[r] - x)) {
                // I && !I2 && !P1 && !P2 && minDiff Idx == l || minDiff Idx == r &&
                // &&  a[l] > x && a[r] < x && Math.abs(a[l] - x) <= Math.abs(a[r] - x) >> minDiff Idx == l;
                return l;
            }
        }

        // I && !I2 && !P1 && !P2 && minDiff Idx == l || minDiff Idx == r &&
        // &&  !(a[l] > x && a[r] < x) || !(Math.abs(a[l] - x) <= Math.abs(a[r] - x)) >> minDiff Idx == r
        return r;
    }

    // :NOTE: pred post условий нет
    //pred: a != null && a.len > 0 && x != null && l >= 0 && r <= a.len && r > 0 && l < r;
    //post: a' == a
    private static int recursiveBinSearch(int[] a, int x, int l, int r) {
        //I = immutable(n) && a.len > 0 && l <= 0 <= a.len-1 && 0 < r <= a.len && minDiff idx in [l, r) &&
        //      && a[0] >= a[1] >= a[2] >= ... >= a[n]

        if (r == a.length || a[0] <= x) {
            // I && P1 == (r == a.length || a[0] <= x) >> minDiff Idx == 0
            return 0;
        }

        //I && !P1

        if (a[a.length-1] > x) {
            //I && !P1 && P2 == (a[a.length-1] > x) >> minDiff Idx == a.len-1;
            return a.length-1;
        }

        //I && !P1 && !P2

        if (l == r-1) {
            //I && !P1 && !P2 && l == r-1
            if (a[l] > x && a[r] < x) {
                //I && !P1 && !P2 && l == r-1 && a[l] > x && a[r] < x
                if (Math.abs(a[l] - x) <= Math.abs(a[r] - x)) {
                    //I && !P1 && !P2 && l == r-1 && a[l] > x && a[r] < x &&
                    // && Math.abs(a[l] - x) <= Math.abs(a[r] - x) >> minDiff Idx == l;
                    return l;
                }
            }
            //I && !P1 && !P2 && l == r-1 && !(a[l] > x && a[r] < x) ||
            // || !(Math.abs(a[l] - x) <= Math.abs(a[r] - x)) >> minDiff Idx == r;
            return r;
        }
        //I && !P1 && !P2 && l != r-1
        int m = (l + r) / 2;
        //I && !P1 && !P2 && l != r-1 && m in [l, r]

        if (a[m] <= x) {
            //I && !P1 && !P2 && l != r-1 && m in [l, r] && a[m] <= x >> minDiff Idx in [l, m]
            return recursiveBinSearch(a, x, l , m);
        } else {
            //I && !P1 && !P2 && l != r-1 && m in [l, r] && a[m] > x >> minDiff Idx in [m, r)
            return recursiveBinSearch(a, x, m, r);
        }
    }

    public static void main(String[] args) {
        //args[0] != null && args[1, .... n] are ints && args[1] >= args[2] >= .. >= args[n]
        int x = Integer.parseInt(args[0]);
        //args[0] != null && args[1, .... n] are ints && args[1] >= args[2] >= .. >= args[n] && x == args[0]


        int[] nums = new int[args.length-1];
        int sum = 0;
        //sum != null && nums != null

        for (int i = 0; i < nums.length; i++) {
            nums[i] = Integer.parseInt(args[i+1]);
            sum += nums[i];
        }

        //args[0] != null && args[1, .... n] are ints && args[1] >= args[2] >= .. >= args[n] &&
        // && x == args[0] && sum != null && nums != null &&
        // && nums[i] == args[i+1] && sum == a[0] + a[1] +...+ a[n] && nums.len > 0 &&
        // && nums[0] >= nums[1] >= ... >= nums[n];



        if (sum % 2 == 0) {
            //args[0] != null && args[1, .... n] are ints && args[1] >= args[2] >= .. >= args[n] &&
            // && x == args[0] && sum != null && nums != null &&
            // && nums[i] == args[i+1] && sum == a[0] + a[1] +...+ a[n] && sum & 2 == 0  && nums.len > 0 &&
            // && nums[0] >= nums[1] >= ... >= nums[n];
            System.out.println(nums[recursiveBinSearch(nums, x, -1, nums.length-1)]);
            // out == minDiff Idx && immutable(n)
        } else {
            //args[0] != null && args[1, .... n] are ints && args[1] >= args[2] >= .. >= args[n] &&
            // && x == args[0] && sum != null && nums != null &&
            // && nums[i] == args[i+1] && sum == a[0] + a[1] +...+ a[n] && sum & 2 != 0  && nums.len > 0 &&
            // && nums[0] >= nums[1] >= ... >= nums[n];
            System.out.println(nums[iterativeBinSearch(nums, x)]);
            // out == minDiff Idx && immutable(n)
        }

    }
}
