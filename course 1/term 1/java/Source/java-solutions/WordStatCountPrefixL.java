import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordStatCountPrefixL {

    public static void countWord(Map <String, Integer> wordList, String word) {
        if (word.length() >= 3) {
            String wordToAdd = word.toString().substring(0, 3).toLowerCase();
            if (!word.isEmpty()) {
                wordList.put(wordToAdd, wordList.getOrDefault(wordToAdd, 0)+1);
            }
        }
    }
    public static void main (String[] args) {
        Map<String, Integer> wordList = new LinkedHashMap<String, Integer>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"), 1024);) {
            String line = reader.readLine();
            while(line != null) {
                StringBuilder word = new StringBuilder();
                for (Character i : line.toCharArray()) {
                    if (Character.isLetter(i) || i == '\'' || Character.DASH_PUNCTUATION == Character.getType(i)) {
                        word.append(i);
                    } else {
                        countWord(wordList, word.toString());
                        word.setLength(0);
                    }
                }

                countWord(wordList, word.toString());
                word.setLength(0);
                line = reader.readLine();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8"), 1024)) {
                String[] sortedKeys = new String[wordList.size()];
                Integer maxVal = 0;
                Integer idx = 0;
                
                for (Integer val : wordList.values()) {
                    if (val > maxVal) {
                        maxVal = val;
                    }
                }
                
                for (int i = 1; i <= maxVal; i++) {
                    for (String key : wordList.keySet()) {
                        if (wordList.get(key) == i) {
                            sortedKeys[idx] = key;
                            idx += 1;
                        }
                    }
                }

                for (String key : sortedKeys) {
                    try {
                        writer.write(key + " " + wordList.get(key));
                        writer.newLine();
                    } catch (IOException e) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.println("Output Exception: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Input Exception: " + e.getMessage());
        }
    }
}
