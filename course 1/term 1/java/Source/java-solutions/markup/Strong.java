package markup;

import java.util.List;

public class Strong extends AbstractModifiedText implements PlainText {
    public Strong(List<PlainText> content) {
        super(content);
    }

    @Override
    protected String markdownTag() {
        return "__";
    }

    @Override
    protected String bbCodeTag() {
        return "b";
    }
}
