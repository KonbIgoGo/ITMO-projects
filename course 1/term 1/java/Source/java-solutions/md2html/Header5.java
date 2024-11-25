package md2html;

import java.util.List;

public class Header5 extends AbstractTag{
    public Header5(List<Tag> content) {
        super(content);
    }

    @Override
    protected String tagHTML() {
        return "h5";
    }
}
