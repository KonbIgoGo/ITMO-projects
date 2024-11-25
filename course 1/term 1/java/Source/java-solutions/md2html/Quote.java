package md2html;

import java.util.List;

public class Quote extends AbstractTag {
    public Quote(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "q";
    }
}
