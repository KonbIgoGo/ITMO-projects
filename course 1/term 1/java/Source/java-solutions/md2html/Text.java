package md2html;
public class Text implements Tag {

    private String content;

    public Text(String content) {
        this.content = content;
    }

    @Override
    public String toHTML() {
        content = content.replace("&", "&amp;");
        content = content.replace("<", "&lt;");
        content = content.replace(">", "&gt;");
        content = content.replace("\\", "");

        return content;
    }
}
