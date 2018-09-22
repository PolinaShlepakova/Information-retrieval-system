package main.dictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import main.Term;
import main.Tokenizer;
import main.Trie;
import main.Utils;
import main.indexes.PositionalIndex;
import utils.TimeWatch;

/**
 * Creates and saves an array of unique words, contained in given files.
 * Also has a collection of processed files,
 * number of unique words and words overall.
 * <p>
 * Effectively uses storage space, but it's a bit harder to do binary search on.
 *
 * @author Polina Shlepakova
 */
public class DictionaryTrie extends Dictionary implements Serializable {
    private String dir;
    private Trie trie;
    private Trie reversedTrie;

    private static final int INITIAL_ARRAY_SIZE = 10;

    /**
     * Creates main by processing given files.
     * Counts the overall amount of words in all files
     * and amount of unique words
     *
     * @param files files to build main from
     */
    public DictionaryTrie(File[] files) {
        super(files);
        this.trie = new Trie();
        this.reversedTrie = new Trie();
        walkFiles();
        nUniqueWords = trie.getnUniqueWords();
    }

    public PositionalIndex getPositionalIndex(String word) {
        return trie.getPositionalIndex(word);
    }

    public PositionalIndex getReversedPositionalIndex(String word) {
        return reversedTrie.getPositionalIndex(word);
    }

    public Iterable<String> getPrefixedWords(String prefix) {
        return trie.prefixSearch(prefix);
    }

    public Iterable<String> getPostfixedWords(String postfix) {
        Iterable<String> words = reversedTrie.prefixSearch(reversed(postfix));
        ArrayList<String> result = new ArrayList<String>();
        for (String word : words) {
            result.add(reversed(word));
        }
        return result;
    }

    public Iterable<String> getAllPrefixedWords() {
        return trie.getAllWords();
    }

    public Iterable<String> getAllPostfixedWords() {
        Iterable<String> words = reversedTrie.getAllWords();
        ArrayList<String> result = new ArrayList<String>();
        for (String word : words) {
            result.add(reversed(word));
        }
        return result;
    }

    public File getFile(int ID) {
        return files[ID];
    }

    /**
     * Iterates through all the files and calls walkFile methos
     * Then trims array of words and matrix to the size of the content.
     */
    protected void walkFiles() {
        for (int id = 0, len = files.length; id < len; id++) {
            walkFile(files[id], id);
        }
    }

    /**
     * Adds all the words in file to the array of Terms words.
     * If file cannot be opened, shows appropriate message.
     *
     * @param file file to process
     * @param ID   id of the currently proccessed file
     */
    protected void walkFile(File file, int ID) {
        Tokenizer t = null;
        try {
            t = new Tokenizer(file);
            // index of currently processed term (for positional index)
            int pos = 0;
            String[] input;

            // read file line by line using Tokenizer and add all the terms to trie
            while ((input = t.getLine()) != null) {
                int inputLength = input.length;
                // fill trie with words from input
                for (int i = 0; i < inputLength; i++, pos++) {
                    trie.addWord(input[i], ID, pos);
                    reversedTrie.addWord(reversed(input[i]), ID, pos);
                }
                nWords += inputLength;
            }
        } catch (IOException e) {
            System.err.println("Could not open file " + file.getPath());
        } finally {
            // close resources, if they were opened (initialized)
            if (t != null) {
                try {
                    t.close();
                } catch (IOException e) {
                    System.err.println("Could not close resources\n" + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns reversed String.
     * @param str String to reverse
     * @return reversed String
     */
    private String reversed(String str) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(str);
        strBuilder = strBuilder.reverse();
        return new String(strBuilder);
    }

    /**
     * Saves unique words in file, one word on each line.
     *
     * @param file file to save words in
     */
    public void saveWordsIn(File file) {
        // todo: write immediately in file
        try {
            FileWriter fw = new FileWriter(file);
            Iterable<String> words = trie.getAllWords();
            for (String word : words) {
                fw.write(word + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.err.println("Could not open or create file");
        }
    }


    public static void main(String[] args) {
        /* CREATING COLLECTION OF FILES */
        File[] files = {
//                new File("files\\books\\the_adventures_of_sherlock_holmes.txt"),
//                new File("files\\books\\the_memoirs_of_sherlock_holmes.txt"),
//                new File("files\\books\\the_return_of_sherlock_holmes.txt"),
//                new File("files\\books\\his_last_bow.txt"),
//                new File("files\\books\\the_case-book_of_sherlock_holmes.txt"),
//                new File("files\\books\\the_picture_of_dorian_gray.txt"),
//                new File("files\\books\\fahrenheit_451.txt"),
//                new File("files\\books\\pride_and_prejudice.txt"),
//                new File("files\\books\\around_the_world_in_80_days.txt"),
//                new File("files\\books\\don_quixote.txt")
                // small files to check the correctness of numbers
                new File("files\\docs\\0.txt"),
                new File("files\\docs\\1.txt"),
                new File("files\\docs\\2.txt"),
                new File("files\\docs\\3.txt"),
                new File("files\\docs\\4.txt"),
                new File("files\\docs\\5.txt"),
                new File("files\\docs\\6.txt"),
                new File("files\\docs\\7.txt")
        };

        DictionaryTrie dic;
        System.out.println("Building dictionary...");
        TimeWatch timeWatch = TimeWatch.start();
        dic = new DictionaryTrie(files);
        System.out.println("Finished in " + timeWatch.getElapsedTime(TimeUnit.SECONDS) + " seconds");


        System.out.println("Number of processed files: " + dic.getnFiles());
        System.out.println("Number of words: " + dic.getnWords());
        System.out.println("Number of unique words: " + dic.getnUniqueWords());

        /* SAVING WORDS in file */
        //dic.saveWordsIn(new File("files\\savedWords.txt"));

        System.out.println("\njuly: \n" + dic.getPositionalIndex("july"));
        System.out.println("\nschizophrenia: \n" + dic.getPositionalIndex("schizophrenia"));
        System.out.println("\nsales: \n" + dic.getPositionalIndex("sales"));
        System.out.println("\nhome: \n" + dic.getPositionalIndex("home"));
        System.out.println("\njane: \n" + dic.getPositionalIndex("jane"));
        System.out.println("\nsherlock: \n" + dic.getPositionalIndex("sherlock"));
        System.out.println("\nelizabeth: \n" + dic.getPositionalIndex("elizabeth"));
    }
}