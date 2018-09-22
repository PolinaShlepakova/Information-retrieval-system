package main;

import java.io.*;

public class Tokenizer {
    private BufferedReader br;
    String[] input;

    public Tokenizer(File file) throws FileNotFoundException {
        this.br = new BufferedReader(new FileReader(file));
        this.input = null;
    }

    public Tokenizer(BufferedReader br) {
        this.br = br;
        this.input = null;
    }

    public static String[] tokenize(String line) {
        if (line.length() == 0) {
            return new String[0];
        }
        return line.split("([^a-zA-Zа-яА-Я']+)'*\\1*");
    }

    public String[] getLine() throws IOException {
        String line;
        if ((line = br.readLine()) != null) {
            input = tokenize(line);
        } else {
            input = null;
        }
        return input;
    }

    public void close() throws IOException {
        br.close();
    }
}
