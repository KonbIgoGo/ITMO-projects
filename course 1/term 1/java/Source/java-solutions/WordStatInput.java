import java.io.*;
import java.util.LinkedHashMap;

public class WordStatInput {
    public static void main(String[] args) {
        LinkedHashMap<String, Integer> wordList = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"), 1024);) {
            String line = reader.readLine();
            while(line != null) {
                StringBuilder word = new StringBuilder();
                for (Character i : line.toCharArray()) {
                    if (Character.isLetter(i) || i == '\'' || Character.DASH_PUNCTUATION == Character.getType(i)) {
                        word.append(i);
                    } else {
                        String wordToAdd = word.toString().toLowerCase();
                        if (!word.toString().isEmpty()) {
                            if (wordList.get(wordToAdd) == null) {
                                wordList.put(wordToAdd, 1);
                            } else {
                                wordList.put(wordToAdd, wordList.get(wordToAdd) + 1);
                            }
                        }
                        word.setLength(0);
                    }
                }
                String wordToAdd = word.toString().toLowerCase();
                if (!word.toString().isEmpty()) {
                    if (wordList.get(wordToAdd) == null) {
                        wordList.put(wordToAdd, 1);
                    } else {
                        wordList.put(wordToAdd, wordList.get(wordToAdd) + 1);
                    }
                }
                word.setLength(0);
                line = reader.readLine();
            }
            reader.close();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8"), 1024)) {
                wordList.forEach((k, v) -> {
                    try {
                        writer.write(k + " " + v);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("Output Exception: " + e.getMessage());
                    }
                });
                writer.close();
            } catch (IOException e) {
                System.err.println("Output Exception: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Input Exception: " + e.getMessage());
        }
    }
}
