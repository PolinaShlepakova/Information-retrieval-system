package main.dictionary;

import java.io.File;
import java.io.Serializable;

/**
 * Creates and saves an array of unique words, contained in given files.
 * Also has a collection of processed files,
 * number of unique words and words overall.
 *
 * @author Polina Shlepakova
 */
public abstract class Dictionary implements Serializable {

    protected File[] files;
    protected int nFiles;
    protected int nWords;
    protected int nUniqueWords;

    /**
     * Creates main by processing given files.
     * Counts the overall amount of words in all files
     * and amount of unique words
     *
     * @param files files to build main from
     */
    public Dictionary(File[] files) {
        this.files = files;
        this.nFiles = files.length;
        this.nWords = 0;
        this.nUniqueWords = 0;
    }

    protected Dictionary() {
        this.files = null;
        this.nFiles = 0;
        this.nWords = 0;
        this.nUniqueWords = 0;
    }

    /**
     * @return the number of files processed
     * (files, which could not be opened, are not counted)
     */
    public int getnFiles() {
        return nFiles;
    }

    /**
     * @return the number of words overall in all processed files
     */
    public int getnWords() {
        return nWords;
    }

    /**
     * @return the number of unique words in all files
     */
    public int getnUniqueWords() {
        return nUniqueWords;
    }

    /**
     * @return Collection of processed files
     * (files, which could not be opened, are not included)
     */
    public File[] getFiles() {
        return files;
    }

    /**for (int i  = 0; i < files.length; i++) {
     walkFile(files[i], i);
     }
     * Saves unique words in file, one word on each line.
     *
     * @param file file to save words in
     */
    public abstract void saveWordsIn(File file);

    /**
     * Saves the values of object's attributes to the file,
     * so that the object can be recreated from it later
     * using constructor.
     *
     * @param file file to save object in
     */
    //protected abstract void saveObjectIn(File file);
}