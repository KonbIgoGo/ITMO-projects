package md2html;

import java.util.List;

public class Header4 extends AbstractTag{
    public Header4(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h4";
    }
}
