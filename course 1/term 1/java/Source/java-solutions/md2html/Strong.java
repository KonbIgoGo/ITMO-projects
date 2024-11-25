package md2html;

import java.util.List;

public class Strong extends AbstractTag{
    public Strong(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "strong";
    }
}
