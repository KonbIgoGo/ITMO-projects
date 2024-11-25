package markup;

import java.util.List;

public class Strikeout extends AbstractModifiedText implements PlainText {
    public Strikeout(List<PlainText> content) {
        super(content);
    }

    @Override
    protected String markdownTag() {
        return "~";
    }

    @Override
    protected String bbCodeTag() {
        return "s";
    }

}
