import java.util.Scanner;

class Segment {
    public double left;
    public double right;
}

public class a {
    private static Segment[] defineSegments(char[] letters, Double[] prob) {
        int m = letters.length;
        Segment[] segments = new Segment[m];
        double l = 0;
        for (int i = 0; i < m; i++) {
            Segment seg = new Segment();
            seg.left = l;
            seg.right = l + prob[i];
            l = seg.right;
            segments[i] = seg;
        }

        return segments;
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        String line = sc.next();
        
        sc.close();
        char[] alphabet = new char[n];

        StringBuilder freq = new StringBuilder();

        for (int i = 0; i < n; i++) {
            alphabet[i] = (char) ('a' + i);
        }

        Double[] prob = new Double[alphabet.length];
        
        for (int i = 0; i < alphabet.length; i++) {
            int countC = 0;
            for (char c : line.toCharArray()) {
                if (c == alphabet[i]) {
                    countC++;
                }
            }
            prob[i] = (countC / (double) line.length());
            freq.append(countC).append(" ");
        }


        Segment[] seg = defineSegments(alphabet, prob);

        double left = 0;
        double right = 1;

        for (int i = 0; i < line.length(); i++) {
            char sym = line.charAt(i);
            int index = 0;
            for (int j = 0; j < alphabet.length; j++) {
                if (alphabet[j] == sym) {
                    index = j;
                    break;
                }
            }
            double newRight = left + (right - left) * seg[index].right;

            left = left + (right - left) * seg[index].left;
            right = newRight;
        }

        String point = "";
        int q = 1;
        int p = 1;

        for (q = 1; q < Integer.MAX_VALUE; q++) {
            double pow = 2 >> q;
            p = (int) Math.ceil(left * pow);
            if (p / pow >= left && p / pow <= right) {
                point = Integer.toBinaryString(p);
                break;
            }
        }


        for (double i : prob) {
            if (i == 1) {
                point = "0";
                break;
            }
        }
        StringBuilder extPoint = new StringBuilder();
        while (point.length()+extPoint.length() < q) {
            extPoint.append("0");
        }
        extPoint.append(point);


        System.out.println(n);  
        System.out.println(freq.toString());
        System.out.println(extPoint.toString());



    }
}