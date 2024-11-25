public class Stat {
    private int wordCounter = 0; 
    private final StringBuilder entries = new StringBuilder();
    

    public int getAmount() {
        return wordCounter;
    }

    public void incrAmount() {
        wordCounter++;
    }

    public void addEntry(int line, int entry) {
        entries.append(" ").append(line).append(":").append(entry);
    }

    public String getStat() {
        return wordCounter + entries.toString();
    }
}
