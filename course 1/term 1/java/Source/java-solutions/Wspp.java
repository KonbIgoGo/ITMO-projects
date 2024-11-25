import scanner.Sc;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Wspp {

    public static void countWord(Map <String, List<Integer>> wordList, String word, int wordCounter) {
        String wordToAdd = word.toLowerCase();
        List<Integer> wordStat = wordList.getOrDefault(wordToAdd, new ArrayList<Integer>());
        if (wordStat.isEmpty()) {
            wordStat.add(1);
        } else {
            wordStat.set(0, wordStat.get(0) + 1);
        }
        wordStat.add(wordCounter);
        wordList.put(wordToAdd, wordStat);
    }
    public static void main (String[] args) {
        Map<String, List<Integer>> wordList = new LinkedHashMap<String, List<Integer>>();
        int wordCounter = 1;
            try {
                Sc sc = new Sc(new FileInputStream(args[0]));
                while (sc.hasNextLine()) {
                    String word = sc.next();
                    StringBuilder processedWord = new StringBuilder();
                    for (char c : word.toCharArray()) {
                        if (Character.isLetter(c) || c == '\'' || Character.DASH_PUNCTUATION == Character.getType(c)) {
                            processedWord.append(c);
                        } else {
                            if (!processedWord.isEmpty()) {
                                countWord(wordList, processedWord.toString(), wordCounter++);
                                processedWord.setLength(0);
                            }
                            
                        }
                    }
                    if (!processedWord.isEmpty()) {
                        countWord(wordList, processedWord.toString(), wordCounter++);
                    }
                }
                sc.close();
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8"), 1024)) {
                wordList.forEach((k, v) -> {
                    try {
                        StringBuilder value = new StringBuilder();
                        for (int i : v) {
                            value.append(i);
                            value.append(" ");
                        }

                        writer.write(k + " " + value.toString().substring(0, value.length()-1));
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("Output Exception: " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println("Output Exception: " + e.getMessage());
            }
        }
    }
