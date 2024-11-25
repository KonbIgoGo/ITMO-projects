package game;
import java.util.*;

public class Main {
    private static int defineVar(Scanner sc, String errMsg, String msg) {
        while (true) {
            System.out.println(msg);
            try {
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.err.println(errMsg);
                sc.nextLine();
            }
        }
    }

    private static void putIntoLeadersTable(Map<Integer, List<String>> table, int place, String name) {
        List<String> names = new ArrayList<>();
        if (table.containsKey(place)) {
            names = table.get(place);
        }
        names.add(name);
        table.put(place, names);
    }
    public static void main(String[] args) {
        final Scanner rulesSc = new Scanner(System.in);
        int botPlayersAmount = 0;
        int humanPlayersAmount = 0;
        int m = 0,n = 0,k = 0;
        try {
            while (true) {
                String msg;
                if (botPlayersAmount == 0) {
                    msg = "Введите количество игроков-ботов: ";
                    botPlayersAmount = defineVar(rulesSc, "Неверное количество игроков-ботов", msg);
                }

                if (humanPlayersAmount == 0) {
                    msg = "Введите количество игроков-людей: ";
                    humanPlayersAmount = defineVar(rulesSc, "Неверное количество игроков-людей", msg);
                }

                System.out.println("Введите размер доски и кол-во элементов в ряд: ");
                msg = "Введите высоту доски: ";
                m = defineVar(rulesSc, "m is NaN", msg);
                msg = "Введите ширину доски: ";
                n = defineVar(rulesSc, "n is NaN", msg);
                msg = "Введите кол-во элементов в ряд: ";
                k = defineVar(rulesSc, "k is NaN", msg);

                if (m < 1 || n < 1 || k < 1 || (m < k && n < k) || (botPlayersAmount + humanPlayersAmount == 0)) {
                    m = -1;
                    n = -1;
                    k = -1;

                    System.err.println("Неверный ввод.");
                    rulesSc.nextLine();
                } else {
                    break;
                }
            }
            int max = Math.max(m, n);
            Map<String, Player> players = new HashMap<>();

            int totalPlayers = botPlayersAmount + humanPlayersAmount;
            int idealAmount = 0;
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                idealAmount = 2 << i - 1;
                if (idealAmount >= totalPlayers) {
                    break;
                }
            }
            PlayerGenerator.generatePlayers(players, botPlayersAmount, new RandomPlayer(max));
            PlayerGenerator.generatePlayers(players, humanPlayersAmount, new HumanPlayer());
            PlayerGenerator.generatePlayers(players, idealAmount - totalPlayers, new PassPlayer());

            String[] names = players.keySet().toArray(new String[0]);
            int roundAmount = (int) (Math.log(idealAmount) / Math.log(2));
            Map<Integer, List<String>> leadersTable = new LinkedHashMap<>();
            for (int i = roundAmount; i > 0; i--) {
                int roundPlace = (2 << i - 1) - (2 << (i - 2)) + 1;
                for (int idx = 0; idx < names.length; idx += 2) {

                    String player1Name = names[idx];
                    String player2Name = names[idx + 1];
                    Player player1 = players.get(player1Name);
                    Player player2 = players.get(player2Name);

                    final Game game = new Game(true, player1, player2);
                    int result;
                    do {

                        result = game.play(new TicTacToeBoard(m, n, k));
                    } while (result == 0);

                    if (result == 1) {

                        System.out.println(player1Name + " won in round " + (roundAmount - i + 1));
                        if (!(player2 instanceof PassPlayer)) {
                            putIntoLeadersTable(leadersTable, roundPlace, player2Name);
                        }
                        players.remove(player2Name);

                    } else if (result == 2) {

                        System.out.println(player2Name + " won in round " + (roundAmount - i + 1));
                        if (!(player1 instanceof PassPlayer)) {
                            putIntoLeadersTable(leadersTable, roundPlace, player1Name);
                        }
                        players.remove(player1Name);
                    }
                }
                names = players.keySet().toArray(new String[0]);
            }

//        leadersTable.forEach((place, name) -> {
//            System.out.println("Players " + name + " took " + place + " place");
//        });
            players.forEach((name, v) -> {
                System.out.println("PLAYER " + name + " WON THE CHAMPIONSHIP");
            });
        } catch (NoSuchElementException e ) {
            System.out.println("GAME OVER");
        }


    }
}
