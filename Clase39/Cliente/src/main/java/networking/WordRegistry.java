package networking;

import java.io.Serializable;

public class WordRegistry implements Serializable {
    public int freq;
    public String word;
    public String book;

    public WordRegistry(String book, String word, int freq) {
        this.book = book;
        this.word = word;
        this.freq = freq;
    }
}