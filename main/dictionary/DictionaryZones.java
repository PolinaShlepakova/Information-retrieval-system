package main.dictionary;

import main.PostingZones;
import main.Tokenizer;
import main.Utils;
import main.compression.CompressedDictionary;
import main.compression.CompressedIndex;
import main.indexes.ZonesIDList;
import utils.TimeWatch;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Polina Shlepakova
 */
public class DictionaryZones extends Dictionary implements Serializable {
    private String[] terms;
    private int nBlocks;
    private int blockSize;

    private static final String[] METADATA_PREFIXES = {
            "Title: ", "Author: ", "Language: ", "Character set encoding", "Translator"
    };
    private static final String END_OF_METADATA = "*** START OF THIS PROJECT GUTENBERG";

    /**
     * max block size in bytes
     */
    private static final int MAX_BLOCK_SIZE = 100000000;
    //    private static final int MAX_BLOCK_SIZE = 1073741824;
    private static final String TEMP_FILES_DIR = "D:\\IR\\zones\\temp\\";
    private static final String INDEX_FILE = "D:\\IR\\zones\\index\\index.txt";


    /**
     * Creates main by processing given files.
     * Counts the overall amount of words in all files
     * and amount of unique words
     *
     * @param files files to build main from
     */
    public DictionaryZones(File[] files) {
        super(files);
        Arrays.sort(files, new FilesComparator());
        this.nBlocks = 0;
        this.blockSize = 0;

        this.terms = new String[10];
        walkFiles();
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

    public ZonesIDList getZonesIDList(String word) throws IOException, NullPointerException {
        int wordID = Utils.binarySearch(terms, word.toLowerCase());
        if (wordID >= 0) {
            BufferedReader br = new BufferedReader(new FileReader(INDEX_FILE));
            // skip all previous id lists
            for (int i = 0; i < wordID; i++) {
                br.readLine();
            }
            return new ZonesIDList(br.readLine());
        } else {
            return null;
        }
    }

    private void saveMetadata(BufferedReader br, HashMap<String, ZonesIDList> dictionary, int ID) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(END_OF_METADATA)) {
                // all metadata is saved, exit
                break;
            }
            // iterate over possible metadata prefixes
            for (int i = 0; i < METADATA_PREFIXES.length; i++) {
                if (line.startsWith(METADATA_PREFIXES[i])) {
                    // get line after metadata prefix and tokenize it into String array
                    String[] data = Tokenizer.tokenize(line.substring(METADATA_PREFIXES[i].length()));
                    // add all terms to appropriate zone of posting with specified id
                    for (String term : data) {
                        addTempTerm(dictionary, term, new PostingZones(ID, PostingZones.VALUES[i]));
                    }
                }
            }
        }
    }

    /**
     * Iterates through all the files and adds terms and doc ids to dictionary.
     * When size of the block reaches its max, saves the dictionary to the file
     * and moves on to the next one (creates new dictionary)
     */
    protected void walkFiles() {
        System.out.println("Files: " + nFiles);
        HashMap<String, ZonesIDList> dictionary = new HashMap<String, ZonesIDList>(2000000);
        for (int id = 0; id < files.length; id++) {
            System.out.println("File #" + id + ", size: " + files[id].length());
            Tokenizer t = null;
            try {
                BufferedReader br = new BufferedReader(new FileReader(files[id]));
                saveMetadata(br, dictionary, id);
                t = new Tokenizer(br);
                String[] input;
                while ((input = t.getLine()) != null) {
                    for (String anInput : input) {
                        // metadata is already saved, so all other terms belong to content
                        addTempTerm(dictionary, anInput, new PostingZones(id, PostingZones.Zone.CONTENT));
                        nWords++;
                        if (blockSize > MAX_BLOCK_SIZE) {
                            saveBlock(dictionary);
                            dictionary.clear();
                            blockSize = 0;
                            nBlocks++;
                            System.out.println("New block");
                            System.gc();
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
        System.out.println("Blocks: " + nBlocks);
    }

    private void addTempTerm(HashMap<String, ZonesIDList> dictionary, String term, PostingZones ID) {
        // avg word has 5.1 chars (lets assume 6)
        // String of this length takes up 24 + (12 + 6 * 2 + padding) = 48 bytes
        // ZonesIDList takes up 24 bytes
        // each Node of ZonesIDList takes 24 bytes
        // so for every unique term add 48 + 24 + 24 = 96 to blockSize
        // and for every already-existing term add 24 to blockSize
        term = term.toLowerCase();
        if (dictionary.containsKey(term)) {
            dictionary.get(term).addNoRepeat(ID);
            blockSize += 24;
        } else {
            ZonesIDList list = new ZonesIDList(ID);
            dictionary.put(term, list);
            blockSize += 96;
        }
    }

    private void saveBlock(HashMap<String, ZonesIDList> dictionary) {
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
            } else if (this.fileID > that.fileID) {
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
            int lastTermID;
            while (currPair != null) {
                currTerm = currPair.term;
                currFileID = currPair.fileID;
                // add current term and posting list pair to dictionary
                addTerm(currTerm);
                lastTermID = writeArray(fw, input[currFileID], 1, -1);
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
                        lastTermID = writeArray(fw, input[nextFileID], 1, lastTermID);
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
        if (PostingZones.getID(arr[start]) != lastID) {
            fw.write(arr[start] + " ");
        }
        for (int i = start + 1; i < len; i++) {
            fw.write(arr[i] + " ");
        }
        System.gc();
        return PostingZones.getID(arr[len - 1]);
    }

    private int merge = 0;
    private void moveToNextLine(BufferedReader[] readers, PriorityQueue<Pair> pq, String[][] input, int fileID) throws IOException {
        String line = readers[fileID].readLine();
        input[fileID] = (line != null) ? line.split(" ") : null;
        if (input[fileID] != null) {
            pq.add(new Pair(input[fileID][0], fileID));
        }
        if (merge++ % 1000 == 0) {
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
//        File dir = new File("D:\\gutenberg\\1\\0\\0\\0");
//        LinkedList<File> filesList = Utils.listFilesFor(dir);
//        File[] files = new File[filesList.size()];
//        int fileIndex = 0;
//        for (File f : filesList) {
//            files[fileIndex++] = f;
//        }

        File[] files = {
                new File("D:\\gutenberg\\1\\0\\0\\0\\10001\\10001.txt"),
                new File("D:\\gutenberg\\1\\0\\0\\0\\10002\\10002.txt"),
                new File("D:\\gutenberg\\1\\0\\0\\0\\10003\\10003.txt")
        };

        DictionaryZones dic;
        System.out.println("Building dictionary...");
        TimeWatch timeWatch = TimeWatch.start();
        dic = new DictionaryZones(files);
        System.out.println("Finished in " + timeWatch.getElapsedTime(TimeUnit.MINUTES) + " minutes");

        System.out.println("Number of processed files: " + dic.getnFiles());
        System.out.println("Number of words: " + dic.getnWords());
        System.out.println("Number of unique words: " + dic.getnUniqueWords());

        /* SAVING WORDS in file */
        dic.saveWordsIn(new File("files\\zonesSavedWords.txt"));

        System.out.println("\nZonesIDLists:");
        String[] queries = {
                "a",
                "frenchwoman",
                "mary",
                "king",
                "Apocolocyntosis",
                "Seneca",
                "Sherlock",
                "English"
        };

        for (String query : queries) {
            try {
                ZonesIDList list = dic.getZonesIDList(query);
                System.out.println(query + ": " + (list == null ? "no documents found" : list));
            } catch (IOException e) {
                System.err.println("Could not find index file");
            }
        }
    }
}