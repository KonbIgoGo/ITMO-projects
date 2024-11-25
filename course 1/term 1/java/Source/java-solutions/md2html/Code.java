package md2html;

import java.util.List;

public class Code extends AbstractTag{
    public Code(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "code";
    }
}
