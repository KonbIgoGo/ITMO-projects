package markup;

public class Text implements PlainText {
    public final String text;

    public Text(String text) {
        this.text = text;
    }

    public StringBuilder toMarkdown() {
        return new StringBuilder(text);
    }

    @Override
    public StringBuilder toBBCode() {
        return new StringBuilder(text);
    }

}
