package game;

import java.util.*;

public class PlayerGenerator {
    public static void generatePlayers(Map<String, Player> players, int amount, Player player) {
        for (int i = 0; i < amount; i++) {
            int nameSize = 20;
            StringBuilder name = new StringBuilder();
            for (int n = 0; n < nameSize; n++) {
                name.append((char) (1040 + Math.random() * 50));
            }
            players.put(name.toString(), player);
        }
    }
}
