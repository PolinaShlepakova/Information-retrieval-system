package main.compression;

import main.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Compresses an array of strings to one String, using <b>blocked compression</b>.<br>
 * Every block is additionally compressed using <b>front coding</b>,
 * if words in a block have the same prefix (which they probably have).
 * For example, block has 4 words: <code>automata, automate, automatic, automation</code>.
 * In compressed String they are written like this:<br>
 * <code>#7automat1a1e2ic3ion</code><br>
 * where numbers say how long is the word (or a part of it), written after them
 * and # means that this block is compressed using front coding.<br>
 * If words in a block don't have the same prefix (for example, if the letter changes),
 * they are written like this (for words <code>reason, retrieval, sauce, shiver</code>):<br>
 * <code>6reason9retrieval5sauce6shiver</code>
 *
 * @author Polina Shlepakova
 */
public class CompressedDictionary {

    private String dictionary;
    private int[] blockIndexes;
    private int nTerms;

    private static final int BLOCK_SIZE = 4;
    private static final char FRONT_CODING_MARKER = '#';
    private static final int DICTIONARY_ARRAY_SIZE = 10000000;

    public CompressedDictionary(String[] terms) {
        this.nTerms = terms.length;
        int nBlocks = (int) Math.ceil((double) nTerms / (double) BLOCK_SIZE);
        this.blockIndexes = new int[nBlocks];
        this.dictionary = new String(toDictionaryCharArray(terms, nBlocks));
    }

    /**
     * Convert array of Strings (terms) to dictionary char array, which contains all the terms,
     * compressed using blocked compression and front coding.
     * @param terms array of terms present in dictionary and sorted alphabetically
     * @param nBlocks number of blocks used for blocked compression
     * @return resulting char array
     */
    private char[] toDictionaryCharArray(String[] terms, int nBlocks) {
        // temp array with compressed terms, which will be converted to dictionary String
        // (to avoid creating a lot of intermediary Strings and littering the heap)
//        char[] dicArr = new char[2147483647];
        char[] dicArr = new char[DICTIONARY_ARRAY_SIZE];
        int dicArrIndex = 0;
        // iterate over blocks
        for (int i = 0; i < nBlocks; i++) {
            // collect terms, which will be in this block
            if ((i * BLOCK_SIZE + BLOCK_SIZE) > nTerms) {
                int a = 9;
            }
            String[] blockTerms = new String[(i * BLOCK_SIZE + BLOCK_SIZE) <= nTerms ? BLOCK_SIZE :
                    nTerms % BLOCK_SIZE]; // the last incomplete block
            for (int j = 0; j < blockTerms.length; j++) {
                blockTerms[j] = terms[i * BLOCK_SIZE + j];
            }
            // write block to temp char array
            // save index of block's start
            this.blockIndexes[i] = dicArrIndex;
            // get common prefix for terms in a block
            char[] prefix = commonPrefix(blockTerms);
            if (prefix.length > 0) {
                dicArrIndex = doFrontCoding(blockTerms, dicArr, dicArrIndex, prefix);
            } else {
                // no common prefix, so use just blocked compression
                dicArrIndex = doBlockedCompression(blockTerms, dicArr, dicArrIndex);
            }
        }
        char[] res = new char[dicArrIndex];
        System.arraycopy(dicArr, 0, res, 0, dicArrIndex);
        return res;
    }

    /**
     * Write block to dictionary char array, using blocked compression and front coding.
     * For example, for block terms <code>automata, automate, automatic, automation</code>
     * the result would look like this:<br>
     * <code>#7automat1a1e2ic3ion</code><br>
     * where numbers say how long is the word (or a part of it), written after them
     * and # means that this block is compressed using front coding.<br>
     *
     * @param blockTerms terms, which belong to the block
     * @param dicArr dictionary char array to write block to
     * @param dicArrIndex position in char array to start writing from
     * @param prefix block terms' common prefix
     * @return the position of the last written char + 1
     */
    private int doFrontCoding(String[] blockTerms, char[] dicArr, int dicArrIndex, char[] prefix) {
        // mark that this block is compressed using front coding
        dicArr[dicArrIndex++] = FRONT_CODING_MARKER;
        // write prefix length to dicArr
        int prefixLength = prefix.length;
        dicArrIndex = writeNumberToCharArray(dicArr, prefixLength, dicArrIndex);
        // write prefix to dicArr
        for (char chr : prefix) {
            dicArr[dicArrIndex++] = chr;
        }

        // iterate over block terms
        for (int j = 0; j < blockTerms.length; j++) {
            int termLength = blockTerms[j].length();
            // write curr term's postfix length to dicArr
            dicArrIndex = writeNumberToCharArray(dicArr, termLength - prefixLength, dicArrIndex);
            // write curr term's postfix to dicArr
            for (int k = prefixLength; k < termLength; k++) {
                dicArr[dicArrIndex++] = blockTerms[j].charAt(k);
            }
        }
        return dicArrIndex;
    }

    /**
     * Write block to dictionary char array, using blocked compression.
     * For example, for block terms <code>reason, retrieval, sauce, shiver</code>
     * the result would look like this:<br>
     * <code>6reason9retrieval5sauce6shiver</code><br>
     *
     * @param blockTerms terms, which belong to the block
     * @param dicArr dictionary char array to write block to
     * @param dicArrIndex position in char array to start writing from
     * @return the position of the last written char + 1
     */
    private int doBlockedCompression(String[] blockTerms, char[] dicArr, int dicArrIndex) {
        // iterate over block terms
        for (int j = 0; j < blockTerms.length; j++) {
            int termLength = blockTerms[j].length();
            // write currTerm length to dicArr
            dicArrIndex = writeNumberToCharArray(dicArr, termLength, dicArrIndex);
            // write currTerm to dicArr
            for (int k = 0; k < termLength; k++) {
                dicArr[dicArrIndex++] = blockTerms[j].charAt(k);
            }
        }
        return dicArrIndex;
    }

    /**
     * Writes integer to char array starting from given position and returns the position of last written char + 1
     * @param charArray array to write to
     * @param number integer to write
     * @param charArrayIndex index to start writing in array from
     * @return the position of the last written char + 1
     */
    private int writeNumberToCharArray(char[] charArray, int number, int charArrayIndex) {
        char[] lengthInChr = toCharArray(number);
        // write prefix length to dicArr
        for (char chr : lengthInChr) {
            charArray[charArrayIndex++] = chr;
        }
        return charArrayIndex;
    }

    /**
     * Converts integer number to char array
     * @param number integer to convert
     * @return char array, which represents number
     */
    private char[] toCharArray(int number) {
        return Integer.toString(number).toCharArray();
    }

    /**
     * Writes common prefix of all strings in array in char array and returns it
     * @param strings array of strings to get common prefix for
     * @return strings' common prefix as char array
     */
    private char[] commonPrefix(String[] strings) {
        int len = strings.length;
        if (len <= 1) {
            return new char[0];
        }
        int minLength = minStringLength(strings);
        char[] prefix = new char[minLength];
        int prefixLength = 0;
        // iterate over i-th chars in all strings
        for (int i = 0; i < minLength; i++) {
            boolean currCharIsCommon = true;
            // define first string's i-th char as curr char to compare other chars to
            char currChar = strings[0].charAt(i);
            // iterate over other strings to compare their i-th chars
            for (int j = 1; j < len; j++) {
                if (strings[j].charAt(i) != currChar) {
                    currCharIsCommon = false;
                    break;
                }
            }
            // if curr char is common, add it to prefix, otherwise exit the loop (prefix is complete)
            if (currCharIsCommon) {
                prefix[i] = currChar;
                prefixLength = i + 1;
            } else {
                break;
            }
        }
        // cut prefix to the size
        char[] res = new char[prefixLength];
        System.arraycopy(prefix, 0, res, 0, prefixLength);
        return res;
    }

    /**
     * Returns the length of shortest String in array of Strings
     * @param strings array of Strings
     * @return the length of shortest String in array of Strings
     */
    private int minStringLength(String[] strings) {
        int min = Integer.MAX_VALUE;
//        for (String str : strings) {
//            System.out.println(str + " ");
//        }
//        System.out.println();
        for (int i = 0, len = strings.length; i < len; i++) {
            if (strings[i].length() < min) {
                min = strings[i].length();
            }
        }
        return min;
    }

    private String getTerm(int ID) {
        if (ID >= this.nTerms) {
            return null;
        }

        // #6sincer 1e 21efriendshipfriendship 3est 3ity #
        int currPos = blockIndexes[ID / BLOCK_SIZE];    // access by block ID
        // get prefix of term
        String prefix;
        if (dictionary.charAt(currPos) == FRONT_CODING_MARKER) {
            currPos++;
            int prefixLength = getNumberFromPos(currPos);
            currPos += getNumberLength(prefixLength);
            prefix = dictionary.substring(currPos, currPos + prefixLength);
            currPos += prefixLength;
        } else {
            prefix = "";
        }
        // advance currPos until reach the position of term postfix
        for (int i = 0, termPosInBlock = ID % BLOCK_SIZE; i < termPosInBlock; i++) {
            // get length of term to read
            int currTermLength = getNumberFromPos(currPos);
            // advance current position after reading number
            // and, since we need to just skip this term, add the length of it to currPos
            currPos += getNumberLength(currTermLength) + currTermLength;
        }
        // get postfix and concatenate it to prefix
        int postfixLength = getNumberFromPos(currPos);
        currPos += getNumberLength(postfixLength);
        return prefix + dictionary.substring(currPos, currPos + postfixLength);
    }

    private int getNumberFromPos(int pos) {
        String number = "";
        // #6sincer 1e 21efriendshipfriendship 3est 3ity #
        // 2 - pos 1
        // 1 - pos 2
        // e - pos 3
        char chr = dictionary.charAt(pos++);
        if (chr == '0') {
            return 0;
        }
        while(Character.isDigit(chr)) {
            number += chr;
            chr = dictionary.charAt(pos++);
        }
        return Integer.parseInt(number);
    }

    private int getNumberLength(int number) {
        return String.valueOf(number).length();
    }

    public int getTermID(String term) {
        int lo = 0;
        int hi = nTerms - 1;
        int mid = (hi - lo) / 2 + lo;

        while (lo <= hi) {
            int cmp = getTerm(mid).compareTo(term);
            if (cmp < 0) {
                lo = mid + 1;
            } else if (cmp > 0) {
                hi = mid - 1;
            } else {
                return mid;
            }
            mid = (hi - lo) / 2 + lo;
        }
        return -1;
    }

    public static void main(String[] args) {
//        String[] terms = {
//                "abbe",
//                "abbess",
//                "abbey",
//                "abbr",
//                "abbreviate",
//                "abbreviation",
//                "abbreviations",
//                "abbreviature",
//                "abysm",
//                "abysmal",
//                "abyss",
//                "ac",
//                "azure",
//                "b",
//                "babble",
//                "babblement",
//                "babbler"
//        };

        String[] terms = new String[10];
        int termsIndex = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader("files\\savedWords.txt"));
            String line = br.readLine();
            while(line != null) {
                if (termsIndex >= terms.length) {
                    terms = Utils.resize(terms, terms.length * 2);
                }
                terms[termsIndex++] = line;
                line = br.readLine();
            }
            terms = Utils.resize(terms, termsIndex);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CompressedDictionary compDic = new CompressedDictionary(terms);
        System.out.println(compDic.dictionary);

        System.out.println();
        for (int i : compDic.blockIndexes) {
            System.out.print(i + " ");
        }

        System.out.println();
        System.out.println(compDic.getTerm(32273));

        System.out.println();
        for (int i = 0; i < terms.length; i += 1) {
            System.out.println(i + ": " + compDic.getTerm(i));
        }

        System.out.println();
        for (int i = 0; i < terms.length; i += 1) {
            System.out.println(compDic.getTermID(terms[i]) + ": " + terms[i]);
        }
    }
}
