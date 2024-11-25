import java.util.Scanner;

public class I {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in); // slow scanner
        int n = sc.nextInt();

        int[][] obelisks = new int[n][3];

        for (int i = 0; i < n; i++) {
            int[] obelisk = new int[3];
            for (int j = 0; j < 3; j++) {
                obelisk[j] = sc.nextInt();
            }
            obelisks[i] = obelisk;
        }

        int xr = Integer.MIN_VALUE, yr = Integer.MIN_VALUE;
        int xl = Integer.MAX_VALUE, yl = Integer.MAX_VALUE;


        for (int[] o : obelisks) {
            xl = Math.min(xl, (o[0] - o[2]));
            yl = Math.min(yl, (o[1] - o[2]));
            xr = Math.max(xr, (o[0] + o[2]));
            yr = Math.max(yr, (o[1] + o[2]));
        }

        int totalH = (int) Math.ceil(((double) Math.max((xr - xl), (yr - yl))) / 2);

        int totalX = (xl + xr) / 2;
        int totalY = (yl + yr) / 2;

        System.out.println(totalX + " " + totalY + " " + totalH);
    }
}
