package md2html;

import java.util.List;

public abstract class AbstractTag implements Tag{
    private final List<Tag> content;

    public AbstractTag(List<Tag> content) {
        this.content = content;
    }

    public String toHTML() {
        StringBuilder converted = new StringBuilder();

        converted.append("<").append(tagHTML()).append(">");
        for (Tag t : content) {
            converted.append(t.toHTML());
        }
        converted.append("</").append(tagHTML()).append(">");

        return converted.toString();
    }

    protected abstract String tagHTML();
}
