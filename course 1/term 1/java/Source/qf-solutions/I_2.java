import java.util.Scanner;

public class I {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        long[][] obelisks = new long[n][3];

        for (int i = 0; i < n; i++) {
            long[] obelisk = new long[3];
            for (int j = 0; j < 3; j++) {
                obelisk[j] = sc.nextLong();
            }
            obelisks[i] = obelisk;
        }

        long xr = 0, yr = 0;
        long xl = Long.MAX_VALUE, yl = Long.MAX_VALUE;


        for (long[] o : obelisks) {
            xl = Math.min(xl, (o[0] - o[2]));
            yl = Math.min(yl, (o[1] - o[2]));
            xr = Math.max(xr, (o[0] + o[2]));
            yr = Math.max(yr, (o[1] + o[2]));
        }

        long totalH = (long) Math.ceil(((double) Math.max((xr - xl), (yr - yl))) / 2);

        long totalX = (xl + xr) / 2;
        long totalY = (yl + yr) / 2;

        System.out.println(totalX + " " + totalY + " " + totalH);
    }
}
