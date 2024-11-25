package md2html;

import java.util.List;

public class Header2 extends AbstractTag{
    public Header2(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h2";
    }
}
