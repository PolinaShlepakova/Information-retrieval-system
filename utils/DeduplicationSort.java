package utils;

public class DeduplicationSort {

    public static void main(String[] args) {
//        Integer[] arr = {6, 7, 2, 5, 3, 8, 6, 5, 3, 10, 6, 4, 5, 5, 8};
        Integer[] arr = {6, 3, 6};
        int len = DeduplicationSort.sort(arr);
        System.out.println("length: " + len);
        for (int i = 0; i < len; i++) {
            System.out.print(arr[i] + " ");
        }
    }

    /**
     * Sorts array, eliminates duplicates and returns the length of sorted array.
     *
     * @param arr array to sort
     * @return the length of sorted array.
     */
    public static int sort(Comparable[] arr) {
        return sort(arr, 0, arr.length - 1);
    }

    /**
     * Sorts array, eliminates duplicates and returns the length of sorted array.
     *
     * @param arr array to sort
     * @param lo index to start sorting from
     * @param hi index to end sorting on (inclusively)
     * @return the length of sorted array.
     */
    public static int sort(Comparable[] arr, int lo, int hi) {
        Comparable[] aux = new Comparable[hi - lo + 1];
        return sort(arr, aux, lo, hi) + 1;
    }

    /**
     * Sorts array and eliminates duplicates
     * by dividing the array in halves, sorting and merging them.
     * Returns the index of the last element of the sorted array.
     *
     * @param arr array to sort
     * @param aux helper array
     * @param lo start of the array to sort
     * @param hi end of the array to sort
     * @return index of the last element of the sorted array without duplicates
     */
    private static int sort(Comparable[] arr, Comparable[] aux, int lo, int hi) {
        assert(lo < hi);
        if (hi - lo == 0) {
            // array has 1 element, which is always sorted
            return hi;
        }
        int mid = lo + (hi - lo) / 2;
        assert(mid >= 0);

        // sort halves and save end main.indexes
        // (because sort eliminates duplicates, so halves can become shorter)
        int end1 = sort(arr, aux, lo, mid);
        int end2 = sort(arr, aux, mid + 1, hi);

        assert(isSortedCorrectly(arr, lo, end1));
        assert(isSortedCorrectly(arr, mid + 1, end2));
        System.arraycopy(arr, lo, aux, lo, hi - lo + 1);

        return merge(arr, aux, lo, end1, mid + 1, end2);
    }

    /**
     * Merges two sorted halves of array into one sorted array without duplicates
     * and returns the index of the last element of the merged array.
     *
     * @param arr array to merge
     * @param aux helper array
     * @param start1 start of first half
     * @param end1 end of first half
     * @param start2 start of second half
     * @param end2 end of second half
     * @return the index of the last element of the merged array
     */
    private static int merge(Comparable[] arr, Comparable[] aux, int start1, int end1, int start2, int end2) {
        // iterator over merged array
        int arrIndex = start1;

        // put first element in merged array to compare the next to (for duplicates)
        if (aux[start1].compareTo(aux[start2]) < 0) {
            // the first element of the first half is smaller
            arr[arrIndex++] = aux[start1++];
        } else {
            arr[arrIndex++] = aux[start2++];
        }

        // merge half-arrays until one of the reaches its end
        while (start1 <= end1 && start2 <= end2) {
            Comparable smaller = (aux[start1].compareTo(aux[start2])) < 0 ? aux[start1++] : aux[start2++];
            if (arr[arrIndex - 1].compareTo(smaller) != 0) {
                // not a duplicate
                arr[arrIndex++] = smaller;
            }
        }

        // determine start and end of the leftover half-array
        int leftoverStart, leftoverEnd;
        if (start1 > end1) {
            // second half is leftover
            leftoverStart = start2;
            leftoverEnd = end2;
        } else {
            leftoverStart = start1;
            leftoverEnd = end1;
        }

        // put leftover elements in merged array (if not duplicates)
        while(leftoverStart <= leftoverEnd) {
            if (arr[arrIndex - 1].compareTo(aux[leftoverStart]) != 0) {
                // not a duplicate
                arr[arrIndex++] = aux[leftoverStart++];
            } else {
                leftoverStart++;
            }
        }

        // index of the last merged element
        return arrIndex - 1;
    }

    /**
     * Checks whether array's elements are in the right order and there are duplicates.
     * So, each element should be smaller than the next, because
     * if it is bigger, the order is wrong,
     * if it is equal, it is a duplicate.
     *
     * @param arr array to check
     * @param lo starting point of check
     * @param hi ending point of check (inclusively)
     * @return true if and only if the array is sorted correctly
     */
    private static boolean isSortedCorrectly(Comparable[] arr, int lo, int hi) {
        for (int i = lo + 1; i < hi; i++) {
            int cmp = arr[i - 1].compareTo(arr[i]);
            if(cmp >= 0) {
                if (cmp == 0) {
                    System.err.println("Has a duplicate.");
                } else {
                    System.err.println("Wrong order.");
                }
                return false;
            }
        }
        return true;
    }
}