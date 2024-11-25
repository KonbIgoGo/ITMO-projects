package search;


public class BinarySearch {
    //a.length > 0 && x is int && x in a
    private static int iterativeBinSearch(int[] a, int x) {
        int l = -1;
        int r = a.length;


        // pred: I = l < r && r <= a.length && ind(x) < l && ind(x) >= r && a != null && immutable(a)
        while (l < r-1) {
            //I && l < r-1 -> ind(x) in (l, ... , r]

            //I
            int m = (l + r) / 2;
            // m > l && m < r && a[m] < a[l] && a[m] > a[r];

            //P: m in a
            if (a[m] <= x) {
                //P && a[m] <= x -> x in (l,..,m]
                r = m;
                //Q1 : I && r' == m && l < r' -> Q
            } else {
                //P && a[m] > x -> x in (m, .. , r]
                l = m;
                //Q2 : I && l' == m && x > l' -> Q
            }
            //post: Q: x in (l, ..., r]

            //Q' -> I
        }

        //post:I && l >= r-1 && x == a[r']
        return r;
    }


    //a.length > 0 && x is int && x in a
    private static int recursiveBinSearch(int[] a, int x, int l, int r) {
        // I : ind(x) in (l, ..., r] && r <= a.length && l >= -1 && a != null && x != null
        if (l == r-1) {
            //I && l == r-1 && ind(x) == r
            return r;
        }
        //I && l != r-1
        int m = (l + r) / 2;
        // m > l && m < r && a[m] < a[l] && a[m] > a[r];

        if (a[m] <= x) {
            //I && a[m] <= x
            return recursiveBinSearch(a, x, l , m);
        } else {
            //I && a[m] > x
            return recursiveBinSearch(a, x, m, r);
        }
    }


    //args length >= 2 && args[0 .... n] !contains letters
    public static void main(String[] args) {

        //args length >= 2 $&& args[0] !contains letters $$ args[0] is int as String
        int x = Integer.parseInt(args[0]);
        //Integer.MinValue <= x <= Integer.MaxValue



        int[] nums = new int[args.length-1];
        int sum = 0;

        //args.length >= 2 && args[1, .... n] is sorted in non-ascending order array of ints as String
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Integer.parseInt(args[i+1]);
            sum += nums[i];
        }
        //nums is sorted in non-ascending order array of ints
        //nums.length = args.length-1
        //x in nums

        //sum is defined
        if (sum % 2 == 0) {
            //sum % 2 == 0
            System.out.println(recursiveBinSearch(nums, x, -1, nums.length));
            // immutable(nums) && id in [0, .. , nums.length-1] && nums[id] == x
        } else {
            //sum % 2 != 0
            System.out.println(iterativeBinSearch(nums, x));
            // immutable(nums) && id in [0, .. , nums.length-1] && nums[id] == x
        }

    }
}
