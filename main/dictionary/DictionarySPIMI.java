package main.dictionary;

import main.Tokenizer;
import main.Utils;
import main.compression.CompressedDictionary;
import main.compression.CompressedIndex;
import main.indexes.IDList;
import utils.TimeWatch;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Creates and saves an array of unique words, contained in given files.
 * Also has a collection of processed files,
 * number of unique words and words overall.
 * <p>
 * Effectively uses storage space, but it's a bit harder to do binary search on.
 *
 * @author Polina Shlepakova
 */
public class DictionarySPIMI extends Dictionary implements Serializable {
    private String[] terms;
    private int nBlocks;
    private int blockSize;
    private int merged = 0;

    private CompressedDictionary dictionary;
    private CompressedIndex postings;

    /**
     * max block size in bytes
     */
    private static final int MAX_BLOCK_SIZE = 100000000;
    private static final String TEMP_FILES_DIR = "D:\\IR\\temp\\";
    private static final String INDEX_FILE = "D:\\IR\\index\\index.txt";


    /**
     * Creates main by processing given files.
     * Counts the overall amount of words in all files
     * and amount of unique words
     *
     * @param files files to build main from
     */
    public DictionarySPIMI(File[] files) {
        super(files);
        Arrays.sort(files, new FilesComparator());
        this.nBlocks = 0;
        this.blockSize = 0;

        this.terms = new String[10];
        walkFiles();
        mergeBlocks();

        this.dictionary = new CompressedDictionary(terms);
        terms = null;
        this.postings = new CompressedIndex(new File(INDEX_FILE), nUniqueWords, "D:\\IR\\index\\compIndex.bin");
    }

    public DictionarySPIMI(File[] files, int blocks) {
        super(files);
        Arrays.sort(files, new FilesComparator());
        this.nBlocks = blocks;

        this.terms = new String[10];
        mergeBlocks();
    }

    private class FilesComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            long len1 = f1.length();
            long len2 = f2.length();
            return (len1 == len2) ? 0 : ((len1 < len2) ? 1 : -1);
        }
    }

    public File getFile(int ID) {
        return files[ID];
    }

    public int[] getIDList(String word) throws IOException, NullPointerException {
        int termID = dictionary.getTermID(word);
        if (termID >= 0) {
            return postings.getPostingList(termID);
        } else {
            return null;
        }
    }

    /**
     * Iterates through all the files and adds terms and doc ids to dictionary.
     * When size of the block reaches its max, saves the dictionary to the file
     * and moves on to the next one (creates new dictionary)
     */
    protected void walkFiles() {
        System.out.println("Files: " + nFiles);
        HashMap<String, IDList> dictionary = new HashMap<String, IDList>(2000000);
        for (int id = 0; id < files.length; id++) {
            System.out.println("File #" + id + ", size: " + files[id].length());
            Tokenizer t = null;
            try {
                t = new Tokenizer(files[id]);
                String[] input;
                while ((input = t.getLine()) != null) {
                    //int inputLength = input.length;
                    for (String anInput : input) {
                        addTempTerm(dictionary, anInput, id);
                        nWords++;
                        if (blockSize > MAX_BLOCK_SIZE) {
                            saveBlock(dictionary);
                            dictionary.clear();
                            blockSize = 0;
                            nBlocks++;
                            System.gc();
                            System.out.println("New block");
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not open file " + files[id].getPath());
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
        saveBlock(dictionary);
        nBlocks++;
        //mergeBlocks();
        System.out.println("Blocks: " + nBlocks);
    }

    private void addTempTerm(HashMap<String, IDList> dictionary, String term, int ID) {
        // avg word has 5.1 chars (lets assume 6)
        // String of this length takes up 24 + (12 + 6 * 2 + padding) = 48 bytes
        // IDList takes up 24 bytes
        // each Node of IDList takes 24 bytes
        // so for every unique term add 48 + 24 + 24 = 96 to blockSize
        // and for every already-existing term add 24 to blockSize
        term = term.toLowerCase();
        if (dictionary.containsKey(term)) {
            dictionary.get(term).addNoRepeat(ID);
            blockSize += 24;
        } else {
            IDList list = new IDList(ID);
            dictionary.put(term, list);
            blockSize += 96;
        }
    }

    private void saveBlock(HashMap<String, IDList> dictionary) {
        ArrayList<String> blockTerms = new ArrayList<String>(dictionary.keySet());
        Collections.sort(blockTerms);
        try {
            FileWriter fw = new FileWriter(new File(TEMP_FILES_DIR + nBlocks + ".txt"));
            for (String blockTerm : blockTerms) {
                fw.write(blockTerm + " " + dictionary.get(blockTerm) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.err.println("Could not save block");
        }
    }

    private BufferedReader[] initReaders() throws IOException {
        BufferedReader[] readers = new BufferedReader[nBlocks];
        for (int id = 0; id < nBlocks; id++) {
            try {
                readers[id] = new BufferedReader(new FileReader(TEMP_FILES_DIR + id + ".txt"));
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Could not find file " + TEMP_FILES_DIR + id + ".txt");
            }
        }
        return readers;
    }

    private void closeFileIO(BufferedReader[] readers, FileWriter fw) throws IOException {
        for (BufferedReader br : readers) {
            br.close();
        }
        fw.close();
    }

    private void addTerm(String term) {
        if (terms.length <= nUniqueWords) {
            terms = Utils.resize(terms, nUniqueWords * 2);
        }
        terms[nUniqueWords++] = term;
    }

    class Pair implements Comparable<Pair> {
        String term;
        int fileID;

        Pair(String term, int fileID) {
            this.term = term;
            this.fileID = fileID;
        }

        @Override
        public int compareTo(Pair that) {
            int cmp = this.term.compareTo(that.term);
            if (cmp != 0) {
                return cmp;
            } else if (this.fileID < that.fileID) {
                return -1;
            } else if (this.fileID > that.fileID){
                return 1;
            } else {
                return 0;
            }
        }
    }

    public void mergeBlocks() {
        System.out.println("Merge\nBlocks: " + nBlocks);
        try {
            // opens BufferedReader for each file to be merged and FileWriter for the file to merge to
            BufferedReader[] readers = initReaders();
            FileWriter fw = new FileWriter(INDEX_FILE);
            // contains current line for each src file
            String[][] input = new String[nBlocks][];
            // contains current terms, connected with the id of their src file, in a sorted order
            PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
            // read first lines from all temp files
            for (int i = 0, len = readers.length; i < len; i++) {
                String line = readers[i].readLine();
                input[i] = (line != null) ? line.split(" ") : null;
                if (input[i] != null) {
                    // and put them in PriorityQueue
                    pq.add(new Pair(input[i][0], i));
                }
            }

            // the least term
            Pair currPair = pq.poll();
            String currTerm;
            int currFileID;
            int lastID;
            while (currPair != null) {
                currTerm = currPair.term;
                currFileID = currPair.fileID;
                // add current term and posting list pair to dictionary
                addTerm(currTerm);
                lastID = writeArray(fw, input[currFileID], 1, -1);
                // move respective file to the next line
                moveToNextLine(readers, pq, input, currFileID);
                // get next term
                Pair nextPair = pq.poll();
                // look through other terms to find equal to current
                if (nextPair != null) {
                    // add all equal terms to curr posting list and advance respective lines
                    while (nextPair != null && (nextPair.term).equals(currTerm)) {
                        int nextFileID = nextPair.fileID;
                        // add next's posting list to dest file
                        lastID = writeArray(fw, input[nextFileID], 1, lastID);
                        // advance respective line
                        moveToNextLine(readers, pq, input, nextFileID);
                        // get next term
                        nextPair = pq.poll();
                    }
                    // currTerm doesn't have any more equal terms, finish adding it
                    fw.write('\n');
                    // move on to the next one
                    currPair = nextPair;
                } else {
                    // currEntry was the last term and posting list pair to be added
                    break;
                }
            }
            closeFileIO(readers, fw);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        // cut to the size
        terms = Utils.resize(terms, nUniqueWords);
    }

    private int writeArray(FileWriter fw, String[] arr, int start, int lastID) throws IOException {
        int len = arr.length;
        if (Integer.parseInt(arr[start]) != lastID) {
            fw.write(arr[start] + " ");
        }
        for (int i = start + 1; i < len; i++) {
            fw.write(arr[i] + " ");
        }
        System.gc();
        return Integer.parseInt(arr[len - 1]);
    }

    private void moveToNextLine(BufferedReader[] readers, PriorityQueue<Pair> pq, String[][] input, int fileID) throws IOException {
        String line = readers[fileID].readLine();
        input[fileID] = (line != null) ? line.split(" ") : null;
        if (input[fileID] != null) {
            pq.add(new Pair(input[fileID][0], fileID));
        }
        merged++;
        if (merged % 1000 == 0) {
            System.out.println((input[fileID] != null ? input[fileID][0] : "null"));
        }
    }

    /**
     * Saves unique words in file, one word on each line.
     *
     * @param file file to save words in
     */
    public void saveWordsIn(File file) {
        try {
            FileWriter fw = new FileWriter(file);
            // save unique words
            for (int i = 0; i < nUniqueWords; i++) {
                fw.write(terms[i] + "\n");
            }
            fw.close();
        } catch (IOException e) {
            System.err.println("Could not open or create file");
        }
    }

    public static void main(String[] args) {
        File dir = new File("D:\\gutenberg\\1\\1");
        LinkedList<File> filesList = Utils.listFilesFor(dir);
        File[] files = new File[filesList.size()];
        int fileIndex = 0;
        for (File f : filesList) {
            files[fileIndex++] = f;
        }

        DictionarySPIMI dic;
        System.out.println("Building dictionary...");
        TimeWatch timeWatch = TimeWatch.start();
        dic = new DictionarySPIMI(files);
        System.out.println("Finished in " + timeWatch.getElapsedTime(TimeUnit.MINUTES) + " minutes");

        System.out.println("Number of processed files: " + dic.getnFiles());
        System.out.println("Number of words: " + dic.getnWords());
        System.out.println("Number of unique words: " + dic.getnUniqueWords());

        /* SAVING WORDS in file */
        dic.saveWordsIn(new File("files\\savedWords.txt"));

        System.out.println("\nIDLists:");
        String[] queries = {
                "a",
                "forecast",
                "home",
                "jane",
                "July",
                "in",
                "Sherlock",
                "elizabeth"
        };

        for (String query : queries) {
            try {
                int[] list = dic.getIDList(query);
                System.out.println(query + ": " + (list == null ? "no documents found" : list));
            } catch (IOException e) {
                System.err.println("Could not find index file");
            }
        }
    }
}