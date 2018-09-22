package main.dictionary;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class DictionaryThreeGrams extends DictionaryList implements Serializable {

    private Map<String, LinkedList<Integer>> threeGrams;

    public DictionaryThreeGrams(File[] files) {
        super(files);
        threeGrams = new TreeMap<String, LinkedList<Integer>>();
        buildThreeGrams();
    }

    private void buildThreeGrams() {
        for (int i = 0; i < nUniqueWords; i++) {
            String word = words[i].getWord();
            if (word.length() < 3) {
                addThreeGram("$" + word, i);
                addThreeGram(word + "$", i);
            } else {
                char[] charArr = word.toCharArray();
                int len = charArr.length;
                addThreeGram("$" + charArr[0] + charArr[1], i);
                for (int j = 0; j < len - 2; j++) {
                    addThreeGram("" + charArr[j] + charArr[j + 1] + charArr[j + 2], i);
                }
                addThreeGram("" + charArr[len - 2] + charArr[len - 1] + "$", i);
            }
        }
    }

    private void addThreeGram(String threeGram, int srcIndex) {
        if (threeGrams.containsKey(threeGram)) {
            threeGrams.get(threeGram).add(srcIndex);
        } else {
            LinkedList<Integer> list = new LinkedList<Integer>();
            list.add(srcIndex);
            threeGrams.put(threeGram, list);
        }
    }

    public Iterable<String> getWordsWithThreeGram(String threeGram) {
        ArrayList<String> res = new ArrayList<String>();
        LinkedList<Integer> wordIDs = threeGrams.get(threeGram);
        for (int i : wordIDs) {
            res.add(words[i].getWord());
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
        DictionaryThreeGrams dic = new DictionaryThreeGrams(files);

        for (Map.Entry<String, LinkedList<Integer>> entry : dic.threeGrams.entrySet()) {
            System.out.print("\n" + entry.getKey() + ": ");
            Iterable<String> relatedWords = dic.getWordsWithThreeGram(entry.getKey());
            for (String w : relatedWords) {
                System.out.print(w + ", ");
            }
        }
    }
}
