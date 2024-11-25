package md2html;

public class Borders {
    private int startTag = -1;
    private int finishTag = -1;

    public Borders(int start) {
        this.startTag = start;
    }

    public void setFinish(int finish) {
        finishTag = finish;
    }

    public void setStart(int start) {
        startTag = start;
    }

    public int getStartTag() {
        return startTag;
    }

    public int getFinishTag() {
        return finishTag;
    }

    public boolean isValid() {
        return startTag != -1 && finishTag != -1 && finishTag - startTag > 1;
    }


}
