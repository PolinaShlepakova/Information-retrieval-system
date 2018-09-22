package main;

import main.indexes.PositionalIndex;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Efficient information retrieval data structure.
 * Allows to addLastNode and delete Strings
 * and do either exact or prefix search.
 * <p>
 * All operations are case insensitive.
 * <p>
 * Works as a set, so there are no identical Strings stored.
 *
 * @author Polina Shlepakova
 */
public class Trie implements Serializable {

    Node root;
    int nUniqueWords;

    /**
     * Node of a Trie
     */
    class Node implements Serializable {
        private Map<Character, Node> children;
        // if null, than not the end of the word
        private WordInfo wordInfo;

        Node() {
            this.children = new TreeMap<Character, Trie.Node>();
            this.wordInfo = null;
        }

        public String toString() {
            return "" + children + (wordInfo != null ? wordInfo : "");
        }
    }

    public class WordInfo implements Serializable {
        private int frequency;
        private PositionalIndex positionalIndex;

        private WordInfo() {
            this(1, new PositionalIndex());
        }

        private WordInfo(int frequency, PositionalIndex positionalIndex) {
            this.frequency = frequency;
            this.positionalIndex = positionalIndex;
        }

        public int getFrequency() {
            return frequency;
        }

        public PositionalIndex getPositionalIndex() {
            return positionalIndex;
        }

        public String toString() {
            return "freq: " + frequency + ", index:\n" + positionalIndex;
        }
    }

    public Trie() {
        root = new Node();
        this.nUniqueWords = 0;
    }

    /**
     * Adds word, converted to lowercase.
     *
     * @param word - word to addLastNode
     */
    public void addWord(String word, int ID, int pos) {
        word = word.toLowerCase();
        Node current = root;
        int i = 0;
        int length = word.length();
        // traverse trie until the path matches the word
        for (; i < length; i++) {
            char chr = word.charAt(i);
            if (current.children.containsKey(chr)) {
                current = current.children.get(chr);
            } else {
                // path no longer matches the word
                break;
            }
        }

        if (i == length) {
            markEndOfTheWord(current, ID, pos);
        } else {
            addNewWord(current, word.substring(i), ID, pos);
        }
        nUniqueWords++;
    }

    /**
     * Marks current node as the end of the word by adding wordInfo to it.
     * WordInfo will contain frequency of 1, and its positional index
     * will have 1 node with specified id and position
     *
     * @param current
     * @param ID
     * @param pos
     */
    private void markEndOfTheWord(Node current, int ID, int pos) {
        if (current.wordInfo != null) {
            addExistingWord(current.wordInfo, ID, pos);
        } else {
            current.wordInfo = new WordInfo();
            current.wordInfo.positionalIndex.addLast(ID, pos);
        }
    }

    /**
     * Increments the frequency of already existing word, represented by wordInfo.
     * Adds pos to node with id in positional index of the word to add.
     *
     * @param wordInfo wordInfo of the word to add
     * @param ID       id of the doc containing the word to add
     * @param pos      word's position in the doc
     */
    private void addExistingWord(WordInfo wordInfo, int ID, int pos) {
        wordInfo.positionalIndex.addLast(ID, pos);
        wordInfo.frequency++;
    }

    /**
     * Puts the word in dictionary by creating new Nodes for each character
     * and linking them to the current Node
     *
     * @param current - Node to start creating new Nodes from
     * @param word    - word to put
     */
    private void addNewWord(Node current, String word, int ID, int pos) {
        int length = word.length();
        for (int i = 0; i < length; i++) {
            Node node = new Node();
            current.children.put(word.charAt(i), node);
            current = node;
        }
        markEndOfTheWord(current, ID, pos);
    }

    /**
     * Traverses main.Trie from the start node to find the specified word.
     * If word exists in main.Trie, returns its end Node. If not, returns null.
     *
     * @param word  word to find
     * @param start node to start traversing from
     * @return word's ending Node if it exists, otherwise null
     */
    private Node traverse(String word, Node start) {
        word = word.toLowerCase();
        Node current = start;
        int length = word.length();
        // traverse trie, if at some point it doesn't have i-th char, word doesn't exist
        for (int i = 0; i < length; i++) {
            char chr = word.charAt(i);
            if (current.children.containsKey(chr)) {
                current = current.children.get(chr);
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * @param word - word to find
     * @return true if and only if the whole word is in dictionary
     */
    public boolean hasWord(String word) {
        return getWordInfo(word) != null;
    }

    /**
     * Returns wordInfo of a specified word. If null, then word is not in the trie
     *
     * @param word - word to get wordInfo for
     * @return true if and only if the whole word is in dictionary
     */
    public WordInfo getWordInfo(String word) {
        Node current = traverse(word, root);
        return current == null ? null : current.wordInfo;
    }

    public PositionalIndex getPositionalIndex(String word) {
        Node current = traverse(word, root);
        if (current == null) {
            return null;
        } else {
            if (current.wordInfo == null) {
                return null;
            } else {
                return current.wordInfo.positionalIndex;
            }
        }
    }

    /**
     * Returns the words, which match the one given:
     * - 	if there is a * at the end of the word,
     * returns all the words, which start as the word given (without *)
     * - 	otherwise returns the given word
     * if is in dictionary
     *
     * @param word - word to find
     * @return Iterable with all words that match the stated one.
     */
    public Iterable<String> search(String word) {
        word = word.toLowerCase();
        int length = word.length();
        if (word.charAt(length - 1) == '*') {
            return prefixSearch(word.substring(0, length - 1));
        } else if (hasWord(word)) {
            LinkedList<String> res = new LinkedList<String>();
            res.add(word);
            return res;
        }
        return new LinkedList<String>();
    }

    /**
     * Returns words, which start from word.
     *
     * @param word - the String found words should start from
     * @return words, which start from word.
     */
    public Iterable<String> prefixSearch(String word) {
        Node current = traverse(word, root);
        if (current == null) {
            return new LinkedList<String>();
        }
        LinkedList<String> results = getAllWords(current, word);
        if (current.wordInfo != null) {
            // the word itself is in the trie
            results.addFirst(word);
        }
        return results;
    }

    /**
     * Returns all words, which are children of start node,
     * (those words start from word String and the chars from child nodes
     * should be concatenated to the word String to get a full word)
     *
     * @param start - node to start getting words from
     * @param word  - String to concatenate words to
     * @return all words, which are children of start node
     */
    private LinkedList<String> getAllWords(Node start, String word) {
        LinkedList<String> words = new LinkedList<String>();
        for (Entry<Character, Node> entry : start.children.entrySet()) {
            Node next = entry.getValue();
            String foundWord = word + entry.getKey();
            if (next.wordInfo != null) {
                // end of word
                words.add(foundWord);
            }
            words.addAll(getAllWords(next, foundWord));
        }

        return words;
    }

    public LinkedList<String> getAllWords() {
        return getAllWords(root, "");
    }

    /**
     * @return number of words in dictionary
     */
    public int getnUniqueWords() {
        return nUniqueWords;
    }

//    /**
//     * Deletes the word from dictionary. Case insensitive.
//     *
//     * @param word - word to delete
//     */
//    public void delWord(String word) {
//        word = word.toLowerCase();
//        delete(root, word, 0);
//    }
//
//    /**
//     * Returns true if parent should delete the element of map with
//     * key, which is located on i-th position in a word
//     */
//    private boolean delete(Node current, String word, int i) {
//        if (i == word.length()) {
//            // when the end of the word is reached
//            // delete only if currrent.endOfWord is true
//            // (otherwise means that dictionary doesn't contain this word)
//            if (!(current.endOfWord)) {
//                return false;
//            }
//            // word is only a prefix of other word, so unmark the end of it
//            current.endOfWord = false;
//            // if current node has no other children return true
//            return current.children.size() == 0;
//        }
//        char chr = word.charAt(i);
//        Node node = current.children.get(chr);
//        if (node == null) {
//            return false;
//        }
//        boolean shouldDeleteCurrentNode = delete(node, word, i + 1);
//
//        // if true is returned then delete node with chr as key
//        if (shouldDeleteCurrentNode) {
//            current.children.remove(chr);
//            // return true if no children are left
//            return current.children.size() == 0;
//        }
//        return false;
//    }

    public static void main(String[] args) {
        Trie trie = new Trie();

        for (int id = 0; id < 5; id++) {
            int pos = 0;
            for (; pos < 3; pos++) {
                trie.addWord("apple", id, pos);
            }
            for (; pos < 6; pos++) {
                trie.addWord("app", id, pos);
            }
            for (; pos < 10; pos++) {
                trie.addWord("appear", id, pos);
            }
            for (; pos < 15; pos++) {
                trie.addWord("pear", id, pos);
            }
            for (; pos < 17; pos++) {
                trie.addWord("lemon", id, pos);
            }
            for (; pos < 21; pos++) {
                trie.addWord("lens", id, pos);
            }
            for (; pos < 25; pos++) {
                trie.addWord("lemonade", id, pos);
            }
        }

        System.out.println("apple: " + trie.getWordInfo("apple"));
        System.out.println("app: " + trie.getWordInfo("app"));
        System.out.println("appear: " + trie.getWordInfo("appear"));
        System.out.println("pear: " + trie.getWordInfo("pear"));
        System.out.println("lemon: " + trie.getWordInfo("lemon"));
        System.out.println("lens: " + trie.getWordInfo("lens"));
        System.out.println("lemonade: " + trie.getWordInfo("lemonade"));

        Iterable<String> words = trie.getAllWords();
        for (String str : words) {
            System.out.println(str);
        }
    }
}