package md2html;

import java.util.List;

public class Header6 extends AbstractTag{
    public Header6(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h6";
    }
}
