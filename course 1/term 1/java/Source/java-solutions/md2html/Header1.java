package md2html;

import java.util.List;

public class Header1 extends AbstractTag{
    public Header1(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h1";
    }
}
