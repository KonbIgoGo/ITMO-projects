package md2html;

import scanner.Sc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Md2Html {
    public static void main(String[] args) {
        try {
            Sc sc = new Sc(new FileInputStream(args[0]));
            List<String> rawParagraphs = new ArrayList<>();
            StringBuilder paragraph = new StringBuilder();
            List<String> convertedParagraphs = new ArrayList<>();
            while (sc.hasNextLine()) {
                String str = sc.nextLine();
                if (str.isEmpty() && !paragraph.isEmpty()) {
                    rawParagraphs.add(paragraph.substring(0, paragraph.length() - System.lineSeparator().length()));
                    paragraph.setLength(0);
                } else if (!str.isEmpty()) {
                    paragraph.append(str).append(System.lineSeparator());
                }
            }
            sc.close();
            if (!paragraph.isEmpty()) {
                rawParagraphs.add(paragraph.substring(0, paragraph.length() - System.lineSeparator().length()));
            }

            for (String par : rawParagraphs) {
                int countParLvl = 0;
                for (char c : par.toCharArray()) {
                    if (c == '#' && countParLvl < 6) {
                        countParLvl++;
                    } else if (!Character.isWhitespace(c)) {
                        countParLvl = 0;
                        break;
                    } else {
                        break;
                    }
                }

                Tag convertedPar = null;
                List<Tag> content;
                if (countParLvl == 0) {
                    content = new Converter().convert(par);
                } else {
                    content = new Converter().convert(par.substring(countParLvl+1));
                }

                switch (countParLvl) {
                    case 0 -> convertedPar = new Paragraph(content);
                    case 1 -> convertedPar = new Header1(content);
                    case 2 -> convertedPar = new Header2(content);
                    case 3 -> convertedPar = new Header3(content);
                    case 4 -> convertedPar = new Header4(content);
                    case 5 -> convertedPar = new Header5(content);
                    case 6 -> convertedPar = new Header6(content);
                }
                assert convertedPar != null;
                convertedParagraphs.add(convertedPar.toHTML());
            }

            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8), 1024)) {
                for (String par : convertedParagraphs) {
                    w.write(par);
                    w.newLine();
                }

            } catch (IOException e) {
                System.err.println("Output exception: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }


    }
}
