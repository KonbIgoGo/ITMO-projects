package markup;

import java.util.List;

public class Emphasis extends AbstractModifiedText implements PlainText {
    public Emphasis(List<PlainText> content) {
        super(content);
    }

    @Override
    protected String markdownTag() {
        return "*";
    }

    @Override
    protected String bbCodeTag() {
        return "i";
    }
}
