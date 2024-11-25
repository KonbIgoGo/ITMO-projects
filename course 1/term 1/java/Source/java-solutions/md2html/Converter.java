package md2html;

import java.util.*;

public class Converter {
    private void removeInternalTags(Borders b, Map<String, List<Borders>> tagList) {
        String[] keys = tagList.keySet().toArray(new String[0]);
        for (String key : keys) {
            List<Borders> bs = tagList.get(key);
            bs.removeIf(borders -> borders.getStartTag() > b.getStartTag() && borders.getFinishTag() < b.getFinishTag());
            tagList.put(key, bs);
            deleteEmptyKeys(tagList);
        }
    }

    private int findMinStart(Map<String, List<Borders>> tagList) {
        int minStart = Integer.MAX_VALUE;
        for (String k : tagList.keySet()) {
            for (Borders b : tagList.get(k)) {
                minStart = Math.min(minStart, b.getStartTag());
            }
        }
        return minStart;
    }

    private void deleteEmptyKeys(Map<String, List<Borders>> tagList) {
        String[] keys = tagList.keySet().toArray(new String[0]);
        for (String key : keys) {
            if (tagList.get(key).isEmpty()) {
                tagList.remove(key);
            }
        }
    }

    public List<Tag> convert(String paragraph) {
        String[] doubleTags = new String[]{"**", "__", "--", "''"};
        String[] singleTags = new String[]{"`", "*", "_"};
        List<Tag> content = new ArrayList<>();
        Map<String, List<Borders>> tagList = new HashMap<>();

        boolean doubleTagFlag = false;
        for (int i = 0; i < paragraph.length() - 1; i++) {

            String definedTag = "";

            if (paragraph.charAt(i) == '\\') {
                i += 1;
                continue;
            }


            for (String tag : doubleTags) {
                if (paragraph.substring(i, i + 2).equals(tag)) {
                    doubleTagFlag = true;
                    definedTag = tag;
                }
            }

            if (definedTag.isEmpty()) {
                if (doubleTagFlag) {
                    doubleTagFlag = false;
                    continue;
                }
                for (String tag : singleTags) {
                    if (paragraph.substring(i, i + 1).equals(tag)) {
                        definedTag = tag;
                    }
                    if (paragraph.length() - i <= 2) {
                        if (paragraph.substring(i + 1, i + 2).equals(tag)) {
                            definedTag = tag;
                        }
                    }
                }
            }


            if (!definedTag.isEmpty()) {
                List<Borders> tags;
                if (tagList.containsKey(definedTag)) {
                    tags = tagList.get(definedTag);

                    if (tags.get(tags.size() - 1).getFinishTag() != -1) {

                        tags.add(new Borders(i));


                    } else {
                        if (definedTag.length() == 2) {
                            tags.get(tags.size() - 1).setFinish(i + 2);
                        } else {
                            tags.get(tags.size() - 1).setFinish(i + 1);
                        }

                    }
                } else {
                    tags = new ArrayList<>();

                    tags.add(new Borders(i));

                }
                tagList.put(definedTag, tags);
            }
        }

        String[] keys = tagList.keySet().toArray(new String[0]);
        for (String k : keys) {
            List<Borders> bs = tagList.get(k);
            bs.removeIf(b -> !b.isValid());
            tagList.put(k, bs);
        }
        deleteEmptyKeys(tagList);
        keys = tagList.keySet().toArray(new String[0]);
        for (String k : keys) {
            List<Borders> bs = tagList.get(k);
            if (bs != null) {
                for (Borders b : bs) {
                    removeInternalTags(b, tagList);
                }
            }
        }

        if (tagList.isEmpty()) {
            content.add(new Text(paragraph));
            return content;
        }


        int finishTag = 0;
        int minStart = findMinStart(tagList);
        keys = tagList.keySet().toArray(new String[0]);
        key:
        for (int k = 0; k < keys.length; k++) {
            String key = keys[k];
            List<Borders> bs = tagList.get(key);
            if (bs == null) {
                minStart = findMinStart(tagList);
                continue;
            }
            for (int i = 0; i < bs.size(); i++) {
                Borders b = bs.get(i);
                if (b.getStartTag() == minStart) {
                    if (finishTag == 0 && minStart != 0) {
                        content.add(new Text(paragraph.substring(finishTag, minStart)));
                    }

                    if (finishTag != 0 && minStart - finishTag != 1) {
                        content.add(new Text(paragraph.substring(finishTag, minStart)));
                    }

                    bs.remove(b);
                    tagList.put(key, bs);
                    finishTag = b.getFinishTag();
                    deleteEmptyKeys(tagList);

                    switch (key) {
                        case "*", "_" -> content.add(new Emphasis(convert(paragraph.substring(b.getStartTag() + 1, b.getFinishTag() - 1))));
                        case "**", "__" -> content.add(new Strong(convert(paragraph.substring(b.getStartTag() + 2, b.getFinishTag() - 2))));
                        case "--" -> content.add(new Crossed(convert(paragraph.substring(b.getStartTag() + 2, b.getFinishTag() - 2))));
                        case "`" -> content.add(new Code(convert(paragraph.substring(b.getStartTag() + 1, b.getFinishTag() - 1))));
                        case "''" -> content.add(new Quote(convert(paragraph.substring(b.getStartTag() + 2, b.getFinishTag() - 2))));
                    }

                    minStart = findMinStart(tagList);
                    if (minStart == Integer.MAX_VALUE) {
                        content.add(new Text(paragraph.substring(finishTag)));
                    }

                    k = -1;
                    continue key;
                }
            }
        }
        return content;
    }
}
