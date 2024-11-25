package md2html;

import java.util.List;

public class Crossed extends AbstractTag{
    public Crossed(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "s";
    }
}
