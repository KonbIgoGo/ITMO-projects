package markup;

import java.util.List;

public abstract class AbstractModifiedText implements PlainText {
    private final List<PlainText> content;

    public AbstractModifiedText(List<PlainText> content) {
        this.content = content;
    }

    @Override
    public StringBuilder toMarkdown() {
        StringBuilder out = new StringBuilder(markdownTag());
        for (PlainText i : content) {
            out.append(i.toMarkdown());
        }
        out.append(markdownTag());
        return out;
    }

    protected abstract String markdownTag();

    @Override
    public StringBuilder toBBCode() {
        StringBuilder out = new StringBuilder("[" + bbCodeTag() + "]");
        for (PlainText i : content) {
            out.append(i.toBBCode());
        }
        out.append("[/").append(bbCodeTag()).append("]");
        return out;
    }

    protected abstract String bbCodeTag();

}
