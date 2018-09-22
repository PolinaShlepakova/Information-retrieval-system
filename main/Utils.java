package main;

import main.dictionary.DictionaryList;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class Utils {
    /**
     * Resizes the array of Strings to the newSize.
     *
     * @param arr     array ot Strings to resize
     * @param newSize size to resize to
     * @return resized array
     */
    public static String[] resize(String[] arr, int newSize) {
        String[] copy = new String[newSize];
        for (int i = 0; i < newSize && i < arr.length; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    /**
     * Resizes the array of Terms to the newSize.
     *
     * @param arr     array ot Terms to resize
     * @param newSize size to resize to
     * @return resized array
     */
    public static Term[] resize(Term[] arr, int newSize) {
        Term[] copy = new Term[newSize];
        for (int i = 0; i < newSize && i < arr.length; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    /**
     * Resizes the array of main.dictionary.DictionaryList.PositionedWords to the newSize.
     *
     * @param arr     array ot main.dictionary.DictionaryList.PositionedWord to resize
     * @param newSize size to resize to
     * @return resized array
     */
    public static DictionaryList.PositionedWord[] resize(DictionaryList.PositionedWord[] arr, int newSize) {
        DictionaryList.PositionedWord[] copy = new DictionaryList.PositionedWord[newSize];
        for (int i = 0; i < newSize && i < arr.length; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    public static int[] resize(int[] arr, int newSize) {
        int[] copy = new int[newSize];
        System.arraycopy(arr, 0, copy, 0, newSize);
        return copy;
    }

    public static boolean[] resize(boolean[] arr, int newSize) {
        boolean[] copy = new boolean[newSize];
        System.arraycopy(arr, 0, copy, 0, newSize);
        return copy;
    }

    public static File[] resize(File[] arr, int newSize) {
        File[] copy = new File[newSize];
        try {
            System.arraycopy(arr, 0, copy, 0, newSize);
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println(arr + "\n");
//            System.out.println(copy);
        }
        return copy;
    }

    /**
     * Resizes the 2-dimensional array of booleans to the newSize.
     *
     * @param arr     array ot booleans to resize
     * @param newRows amount of rows to resize to
     * @param newCols amount of cols to resize to
     * @return resized array
     */
    public static boolean[][] resize(boolean[][] arr, int newRows, int newCols) {
        boolean[][] copy = new boolean[newRows][newCols];
        for (int i = 0; i < newRows && i < arr.length; i++) {
            for (int j = 0; j < newCols && j < arr[0].length; j++) {
                copy[i][j] = arr[i][j];
            }
        }
        return copy;
    }

    /**
     * Resizes the 2-dimensional array of booleans to the newSize.
     *
     * @param arr     array ot booleans to resize
     * @param newRows amount of rows to resize to
     * @return resized array
     */
    public static boolean[][] resize(boolean[][] arr, int newRows) {
        boolean[][] copy = new boolean[newRows][arr[0].length];
        for (int i = 0; i < newRows && i < arr.length; i++) {
            copy[i] = arr[i];
        }
        return copy;
    }

    public static BufferedReader[] deleteElem(BufferedReader[] arr, int elem) {
        BufferedReader[] res = new BufferedReader[arr.length - 1];
        System.arraycopy(arr, 0, res, 0, elem);
        System.arraycopy(arr, elem + 1, res, elem, res.length - elem);
        return res;
    }

    public static String[][] deleteElem(String[][] arr, int elem) {
        String[][] res = new String[arr.length - 1][];
        System.arraycopy(arr, 0, res, 0, elem);
        System.arraycopy(arr, elem + 1, res, elem, res.length - elem);
        return res;
    }

    /**
     * Finds string in the array of Strings.
     * If the string is present in the array, returns its position.
     * Otherwise, returns the position it would have - arr.length - 1
     *
     * @param arr - array to search for the string in
     * @param str - string to search for
     * @return index where the string is in the array, or, if the string isn't in the array,
     * index where it would've been - arr.length - 1
     */
    public static int binarySearch(String[] arr, String str) {
        return binarySearch(arr, str, 0, arr.length);
    }

    /**
     * Finds string in the array of Strings.
     * If the string is present in the array, returns its position.
     * Otherwise, returns the position it would have - arr.length - 1
     *
     * @param arr    array to search for the string in
     * @param str    string to search for
     * @param start  index to start searching for
     * @param length of subarray to search in
     * @return index where the string is in the array, or, if the string isn't in the array,
     * index where it would've been - length - 1
     */
    public static int binarySearch(String[] arr, String str, int start, int length) {
        int lo = start;
        int hi = length - 1;
        int mid = (hi - lo) / 2 + lo;

        while (lo <= hi) {
            if (arr[mid].compareTo(str) < 0) {
                lo = mid + 1;
            } else if (arr[mid].compareTo(str) > 0) {
                hi = mid - 1;
            } else {
                return mid;
            }

            mid = (hi - lo) / 2 + lo;
        }
        return mid - length - 1;
    }

    /**
     * Finds string in the array of Terms.
     * If the string is present in the array, returns its position.
     * Otherwise, returns - 1
     *
     * @param arr    array to search for the string in
     * @param str    string to search for
     * @return index where the string is in the array, or, if the string isn't in the array, - 1
     */
    public static int binarySearch(Term[] arr, String str) {
        int lo = 0;
        int hi = arr.length - 1;
        int mid = (hi - lo) / 2 + lo;

        while (lo <= hi) {
            if (arr[mid].getWord().compareTo(str) < 0) {
                lo = mid + 1;
            } else if (arr[mid].getWord().compareTo(str) > 0) {
                hi = mid - 1;
            } else {
                return mid;
            }

            mid = (hi - lo) / 2 + lo;
        }
        return -1;
    }

    public static LinkedList<File> listFilesFor(final File dir) {
        LinkedList<File> files = new LinkedList<File>();
        for (final File fileEntry : dir.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesFor(fileEntry));
            } else {
                files.add(fileEntry);
            }
        }
        return files;
    }

    public static void main(String[] args) {
//        String[] arr = {"abs", "babs", "mom", "zeal"};
//        System.out.println("abs: " + binarySearch(arr, "abs"));
//        System.out.println("babs: " + binarySearch(arr, "babs"));
//        System.out.println("mom: " + binarySearch(arr, "mom"));
//        System.out.println("zeal: " + binarySearch(arr, "zeal"));
//        System.out.println("abail: " + (binarySearch(arr, "abail") + arr.length + 1));
//        System.out.println("bob: " + (binarySearch(arr, "bob") + arr.length + 1));
//        System.out.println("nope: " + (binarySearch(arr, "nope") + arr.length + 1));
//        System.out.println("zzz: " + (binarySearch(arr, "zzz") + arr.length + 1));

//        int[][] matrix = new int[5][3];
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 3; j++) {
//                matrix[i][j] = i + 1;
//            }
//        }
//
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 3; j++) {
//                System.out.print(matrix[i][j] + " ");
//            }
//            System.out.println();
//        }
//        System.out.println("\n=====\n");
//
//        matrix[4] = matrix[3];
//        matrix[3] = new int[3];
//        matrix[3][1] = 9;
//
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 3; j++) {
//                System.out.print(matrix[i][j] + " ");
//            }
//            System.out.println();
//        }

        File dir = new File("D:\\gutenberg\\1\\0");
        LinkedList<File> files = Utils.listFilesFor(dir);
        for (File f : files) {
            System.out.println(f.getAbsolutePath());
        }

    }
}
