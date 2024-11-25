package md2html;

import java.util.List;

public class Header3 extends AbstractTag{
    public Header3(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h3";
    }
}
