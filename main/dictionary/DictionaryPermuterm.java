package main.dictionary;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DictionaryPermuterm extends DictionaryList implements Serializable {

    private Map<String, Integer> permutermIndex;

    public DictionaryPermuterm(File[] files) {
        super(files);
        permutermIndex = new TreeMap<String, Integer>();
        buildPermuterm();
    }

    private void buildPermuterm() {
        for (int i = 0; i < nUniqueWords; i++) {
            String word = words[i].getWord();
            word += "$";
            permutermIndex.put(word, i);
            for (int j = 0, len = word.length(); j < len; j++) {
                word = word.substring(1) + word.charAt(0);
                permutermIndex.put(word, i);
            }
        }
    }

    private Iterable<String> getWordsStartingWithPermuterm(String permuterm) {
        ArrayList<String> res = new ArrayList<String>();
        boolean found = false;
        for (Map.Entry<String, Integer> entry : permutermIndex.entrySet()) {
            if (entry.getKey().startsWith(permuterm)) {
                found = true;
                res.add(words[entry.getValue()].getWord());
            } else if (found) {
                // if matching permuterm was already found and current key doesn't match it
                // then no next key will (because Map is sorted)
                break;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        File[] files = {
                new File("files\\docs\\0.txt"),
                new File("files\\docs\\1.txt"),
                new File("files\\docs\\2.txt"),
                new File("files\\docs\\3.txt"),
                new File("files\\docs\\4.txt"),
                new File("files\\docs\\5.txt"),
                new File("files\\docs\\6.txt"),
                new File("files\\docs\\7.txt")
        };
        DictionaryPermuterm dic = new DictionaryPermuterm(files);

        // to search for h*e search for permuterms starting with e$h
        System.out.println("h*e: ");
        Iterable<String> match = dic.getWordsStartingWithPermuterm("e$h");
        for (String str : match) {
            System.out.println(str);
        }
        System.out.println();

        // to search for s*, permuterm starts with $s
        System.out.println("s*: ");
        match = dic.getWordsStartingWithPermuterm("$s");
        for (String str : match) {
            System.out.println(str);
        }
        System.out.println();

        for (Map.Entry<String, Integer> entry : dic.permutermIndex.entrySet()) {
            System.out.println(entry.getKey() + " -> " + dic.words[entry.getValue()].getWord());
        }
    }
}
