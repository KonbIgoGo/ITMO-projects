import java.util.Scanner;

public class B {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int amount = sc.nextInt();
        int counter = 0;
        for (int i = -710 * 25000; i < 710 * 25000; i+=710) {
            counter++;
            System.out.println(i);
            if (counter == amount) {
                break;
            }
        }
    }
}
