package md2html;

import java.util.List;

public class Emphasis extends AbstractTag{
    public Emphasis(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "em";
    }
}
