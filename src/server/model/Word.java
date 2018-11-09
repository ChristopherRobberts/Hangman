package server.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class Word {
    private final List<String> words = new ArrayList<>();
    private String word;
    private String hiddenWord;

    Word() {
        try {
            loadWords();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Random rand = new Random();
        this.word = words.get(rand.nextInt(words.size() - 1) + 1).toLowerCase();
        this.hiddenWord = hideWord(this.word);
    }

    private void loadWords() throws Exception {
        File path = new File("/home/chris/Documents/Nätverksprogrammering/Hangman/words.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        String word;

        while ((word = bufferedReader.readLine()) != null) {
            words.add(word);
        }
    }

    private String hideWord(String word) {
        return word.replaceAll("[a-zA-Z]", "_ ").trim();
    }

    public List<String> getWords() {
        return this.words;
    }

    String getWord() {
        return this.word;
    }

    String getHiddenWord() {
        return this.hiddenWord;
    }
}

