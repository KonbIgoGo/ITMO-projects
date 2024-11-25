package markup;

import java.util.List;

public class Paragraph {
    private final List<PlainText> content;

    public Paragraph(List<PlainText> content) {
        this.content = content;
    }

    public void toMarkdown(StringBuilder bank) {
        StringBuilder out = new StringBuilder();
        for (PlainText i : content) {
            out.append(i.toMarkdown());
        }
        bank.append(out);
    }

    public void toBBCode(StringBuilder bank) {
        StringBuilder out = new StringBuilder();
        for (PlainText i : content) {
            out.append(i.toBBCode());
        }
        bank.append(out);
    }
}
