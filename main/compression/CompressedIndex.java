package main.compression;

import main.Tokenizer;
import main.Utils;

import javax.rmi.CORBA.Util;
import java.io.*;

/**
 * Compresses the index using variable byte encoding
 * and provides methods to work with compressed index.
 *
 * @author Polina Shlepakova
 */
public class CompressedIndex {

    private File file;
    private int[] indexPointers;

    private static final int BYTE_CAPACITY = 7;

    /**
     * @param uncompFile index file to compress
     * @param nTerms amount of terms in the index
     * @param filepath filepath to a new compressed index
     */
    public CompressedIndex(File uncompFile, int nTerms, String filepath) {
        this.file = new File(filepath);
        this.indexPointers = new int[nTerms];
        compressIndex(uncompFile);
    }

    /**
     * Gets posting list of a particular term in this index.
     * @param termID id of the term to get posting list for
     * @return an array of ints that represent postings
     */
    public int[] getPostingList(int termID) {
        int pointer = indexPointers[termID];
        FileInputStream in = null;
        int[] res = null;
        try {
            in = new FileInputStream(this.file);
            // skip previous bytes
            skipPrevBytes(pointer, in);
            res = readIDsDifferences(in, pointer, termID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return toIDList(res);
    }

    /**
     * Converts an array of id differences to an array of IDs
     * IDs are stored as a difference between current and previous to save space.
     * For example, instead of storing 0 30 100 500 550, the compressed index stores 0 30 70 400 50
     * @param idDiff an array of differences between IDs
     * @return
     */
    private int[] toIDList(int[] idDiff) {
        for (int i = 1, len = idDiff.length; i < len; i++) {
            idDiff[i] = idDiff[i - 1] + idDiff[i];
        }
        return idDiff;
    }

    /**
     * Reads an array of ID differences from index file
     * @param in stream for an index file
     * @param pointer
     * @param termID id of a term to read array for
     * @return
     */
    private int[] readIDsDifferences(FileInputStream in, int pointer, int termID) {
        int[] IDs = new int[10];
        int IDsIndex = 0;
        if (termID < (indexPointers.length - 1)) {
            int nextPointer = indexPointers[termID + 1];
            while (pointer < nextPointer) {
                if (IDsIndex >= IDs.length) {
                    IDs = Utils.resize(IDs, IDs.length * 2);
                }
                int ID = readID(in);
                IDs[IDsIndex++] = ID;
                pointer += getNBytesToStore(ID);
            }
        } else {
            // last ID list, terminates when the file ends
            int ID;
            while ((ID = readID(in)) >= 0) {
                if (IDsIndex >= IDs.length) {
                    IDs = Utils.resize(IDs, IDs.length * 2);
                }
                IDs[IDsIndex++] = ID;
            }
        }
        IDs = Utils.resize(IDs, IDsIndex);
        return IDs;
    }

    private int readID(FileInputStream in) {
        String idStr = "";
        try {
            int ID;
            while ((ID = in.read()) >= 0) {
                String byteStr = Integer.toBinaryString(ID);
                idStr += byteStr.substring(1);
                // terminating byte
                if (byteStr.length() == BYTE_CAPACITY + 1 && byteStr.charAt(0) == '1') {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (idStr.equals("")) {
            return -1;
        } else{
            return Integer.parseInt(idStr, 2);
        }
    }

    /**
     * Counts number of bytes needed to store specified integer
     * @param num integer to count storage bytes for
     * @return number of bytes needed to store num
     */
    private int getNBytesToStore(int num) {
        String binNum = Integer.toBinaryString(num);
        return (int) Math.ceil((double) binNum.length() / (double) BYTE_CAPACITY);
    }

    private void skipPrevBytes(int pointer, FileInputStream in) {
        byte[] skipped = new byte[pointer];
        try {
            in.read(skipped);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compressIndex(File uncompFile) {
        BufferedReader br = null;
        FileOutputStream out = null;
        try {
            br = new BufferedReader(new FileReader(uncompFile));
            out = new FileOutputStream(this.file);
            String line;
            String[] input;
            int pointer = 0;
            int byteNum = 0;
            int difference = 0;
            // get posting list as array of Strings, which represent IDs
            while ((line = br.readLine()) != null) {
                indexPointers[pointer++] = byteNum;
                input = line.split(" ");
                // iterate over IDs
                for (String str : input) {
                    // determine the difference between curr and prev IDs
                    difference = Integer.parseInt(str) - difference;
                    assert(difference >= 0);
                    // create byte array to represent difference
                    int[] byteArr = toByteArray(difference);
                    // iterate over bytes and write them in file
                    for (int b : byteArr) {
                        out.write(b);
                        byteNum++;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int[] toByteArray(int num) {
        String binNum = Integer.toBinaryString(num);
        // number of bytes needed to store this number
        int nBytes = (int) Math.ceil((double) binNum.length() / (double) BYTE_CAPACITY);
        int[] byteArr = new int[nBytes];
        for (int i = nBytes - 1, j = 0; i >= 0; i--) {
            byteArr[j++] = toByte(binNum, i);
        }
        return byteArr;
    }

    private int toByte(String binNum, int bytePos) {
        int endPos = binNum.length() - bytePos * BYTE_CAPACITY;
        int startPos = endPos - BYTE_CAPACITY;
        if (startPos < 0) {
            startPos = 0;
        }
        String res = binNum.substring(startPos, endPos);
        char[] prefix = new char[8 - res.length()];
        for (int i = 0; i < prefix.length; i++) {
            prefix[i] = '0';
        }
        if (bytePos == 0) {
            prefix[0] = '1';
        }
        res = new String(prefix) + res;
        return Integer.parseInt(res, 2);
    }

    public static void main(String[] args) throws IOException {
        CompressedIndex compIndex = new CompressedIndex(new File("file.txt"), 6, "file.bin");
        for (int i = 0; i < 6; i++) {
            int[] IDs = compIndex.getPostingList(i);
            for (int id : IDs) {
                System.out.println(id + " ");
            }
            System.out.println();
        }
    }
}
