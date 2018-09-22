package main;

import java.io.*;
import main.indexes.PositionalIndex;

public class Term implements Comparable<Term>, Serializable {
    private String word;
    private int frequency;
    private PositionalIndex positionalIndex;

    public Term(String word, int frequency, PositionalIndex positionalIndex) {
        this.word = word;
        this.frequency = frequency;
        this.positionalIndex = positionalIndex;
    }

    public Term(String word, int frequency) {
        this(word, frequency, new PositionalIndex());
    }

    public Term(String word, int frequency, int ID) {
        this(word, frequency, new PositionalIndex());
        addID(ID);
    }

    public Term(String word) {
        this(word, 1, new PositionalIndex());
    }

    public String getWord() {
        return word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incFrequency() {
        this.frequency++;
    }

    public void addID(int val) {
        if (positionalIndex != null) {
            positionalIndex.addLast(val);
        }
    }

    public void addPos(int ID, int pos) {
        positionalIndex.addPos(ID, pos);
    }

    public void addSkips() {
        positionalIndex.addSkips();
    }

    public PositionalIndex getPositionalIndex(String dir, int pos) throws IOException, ClassNotFoundException {
        if (positionalIndex != null) {
            return positionalIndex;
        }
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(dir + "\\" + pos + ".out"));
        positionalIndex = (PositionalIndex) oin.readObject();
        return positionalIndex;
    }

    public void movePositionalIndexOnDisk(String filename) throws IOException {
        if (positionalIndex == null) {
            return;
        }
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
        oos.writeObject(positionalIndex);
        oos.flush();
        oos.close();
        positionalIndex = null;
    }

    /**
     * Compare this term to the other by the word (lexicographically)
     * @param that term to compare to
     * @return -1, 0 or 1
     */
    public int compareTo(Term that) {
        return this.getWord().compareTo(that.getWord());
    }

    public String toString() {
        String res = "";
        res += word + ", " + frequency + ", IDs: \n" + positionalIndex;
        return res;
    }

    public String toStringSimple() {
        String res = "";
        res += word + ", " + frequency + ", IDs: " + positionalIndex.toStringSimple();
        return res;
    }

    /**
     * Merges array of terms arr1 with array of terms arr2,
     * which contains terms from only 1 document.
     * @param arr1 array of terms
     * @param arr2 array of terms from only 1 document
     * @return array with unique-worded terms
     */
    public static Term[] merge(Term[] arr1, Term[] arr2) {
        int length1 = arr1.length;
        int length2 = arr2.length;
        if (length1 == 0) {
            return arr2;
        } else if (length2 == 0) {
            return arr1;
        }

        Term[] result = new Term[length1 + length2];
        // index of current element in result array
        int i = 0;
        // index of current element in arr1
        int j = 0;
        // index of current element in arr2
        int k = 0;

        // put first element in merged array to compare the next to (for duplicates)
        if (arr1[j].compareTo(arr2[k]) < 0) {
            // the first element of the arr1 is smaller
            result[i++] = arr1[j++];
        } else {
            result[i++] = arr2[k++];
        }

        Term smaller;
        // merge half-arrays
        while (j <= length1 || k <= length2) {
            // determine smaller term
            if (j == length1) {
                // both arrays reached their end
                if (k == length2) {
                    break;
                }
                // arr1 reached its end, so take term from arr2
                smaller = arr2[k++];
            } else if(k == length2) {
                // arr2 reached its end, so take term from arr1
                smaller = arr1[j++];
            } else {
                smaller = (arr1[j].compareTo(arr2[k])) <= 0 ? arr1[j++] : arr2[k++];
            }

            if (result[i - 1].compareTo(smaller) != 0) {
                // not a duplicate, so addLastNode smaller term to the resulting array
                result[i++] = smaller;
            } else {
                // merge same words with different ids
                // by adding their frequencies and
                // adding first (and only) id of the second term to the first
                assert(smaller.positionalIndex.size() == 1);
                result[i - 1].frequency += smaller.frequency;
                result[i - 1].positionalIndex.addLast(smaller.positionalIndex.getFirst());
            }
        }

        Term[] copy = new Term[i];
        System.arraycopy(result, 0, copy, 0, i);
        return copy;
    }
}
