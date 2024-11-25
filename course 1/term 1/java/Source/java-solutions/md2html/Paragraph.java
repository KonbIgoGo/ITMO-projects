package md2html;

import java.util.List;

public class Paragraph extends AbstractTag implements Tag{
    public Paragraph(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "p";
    }
}
