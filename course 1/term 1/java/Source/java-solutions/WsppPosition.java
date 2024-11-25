import scanner.Sc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WsppPosition {

	public static void countWord(Map<String, Stat> wordList, String word) {
		String wordToAdd = word.toLowerCase();
		Stat wordStat = wordList.getOrDefault(wordToAdd, new Stat());

		wordStat.incrAmount();
		wordList.put(wordToAdd, wordStat);
	}

	public static void main(String[] args) {
		Map<String, Stat> wordList = new LinkedHashMap<>();
		int lineCounter = 0;

		List<String> processedWords = new ArrayList<>();
		try {
			Sc sc = new Sc(new FileInputStream(args[0]));
			while (sc.hasNextLine()) {
				List<String> words = sc.nextToTheEndOfLine();
				if (!words.isEmpty()) {
					lineCounter++;
				}
				StringBuilder processedWord = new StringBuilder();
				for (String word : words) {
					if (!word.isEmpty()) {
						processedWord.setLength(0);
						for (char c : word.toCharArray()) {
							if (Character.isLetter(c) || c == '\'' || Character.DASH_PUNCTUATION == Character.getType(c)) {
								processedWord.append(c);
							} else {
								if (!processedWord.isEmpty()) {
									String processedWordAsString = processedWord.toString().toLowerCase();
									countWord(wordList, processedWordAsString);
									processedWords.add(processedWordAsString);
									processedWord.setLength(0);
								}

							}
						}
					}

					if (!processedWord.isEmpty()) {
						String processedWordAsString = processedWord.toString().toLowerCase();
						countWord(wordList, processedWordAsString);
						processedWords.add(processedWordAsString);
					}
				}

				for (int i = 0; i < processedWords.size(); i++) {
					wordList.get(processedWords.get(i)).addEntry(lineCounter, processedWords.size() - i);
				}
				processedWords.clear();

			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		}

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8), 1024)) {
			wordList.forEach((k, v) -> {
				StringBuilder wordStat = new StringBuilder();
				wordStat.append(k).append(" ");
				wordStat.append(v.getStat());
				try {
					writer.write(wordStat.toString());
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
