package main.indexes;

import java.io.Serializable;

/**
 * A convenience class to make it easier to create and read ID list from a String
 * (as ID lists are stored in a file on the disc) and add new elements to it.
 * It can also add skips to a list, which will make the iteration over it faster.
 */
// size: 8 + 4 + 4 + 4 = 20 + padding = 24
public class IDList implements Serializable { // 8

    private Node first; // 4
    private Node last; // 4
    private int size; // 4

    // size: 8 + 4 + 4 + 4 = 20 + padding = 24
    class Node implements Serializable { // 8
        /** next element of the list after this one */
        Node next; // 4
        /** a node to skip to, if skip exists */
        Node skip; // 4
        /** value (id) */
        int val; // 4

        Node(int val) {
            this.next = null;
            this.skip = null;
            this.val = val;
        }

        public String toString() {
            return "" + val;
        }
    }

    /**
     * Creates an empty ID list
     */
    public IDList() {
        this.first = null;
        this.last = null;
        size = 0;
    }

    /**
     * Creates a list with one ID
     * @param firstVal the first id
     */
    public IDList(int firstVal) {
        this.first = new Node(firstVal);
        this.last = first;
        size = 1;
    }

    /**
     * Creates IDList from a line with IDs, separated by whitespaces
     *
     * @param line a String with IDs separated by whitespaces
     */
    public IDList(String line) {
        String[] input = line.split(" ");
        int len = input.length;
        switch (len) {
            case 0:
                this.first = null;
                this.last = null;
                break;
            case 1:
                this.first = new Node(Integer.parseInt(input[0]));
                this.last = first;
                break;
            default:
                this.first = new Node(Integer.parseInt(input[0]));
                Node prev = this.first;
                for (int i = 1; i < len; i++) {
                    Node curr = new Node(Integer.parseInt(input[i]));
                    prev.next = curr;
                    prev = curr;
                }
                this.last = prev;
                break;
        }
        this.size = len;
    }

    /**
     * Add new ID to the list
     * @param val new ID
     */
    public void add(int val) {
        Node oldLast = last;
        last = new Node(val);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Add new ID to the list only if it is not the same as the last one added
     * @param val new ID
     */
    public void addNoRepeat(int val) {
        if (last.val == val) {
            return;
        }
        Node oldLast = last;
        last = new Node(val);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Add skips to the list for faster iteration
     */
    public void addSkips() {
        // the optimal amount of steps (skips)
        int step = (int) Math.round(Math.sqrt(size));
        int stepCounter = 0;
        Node current = first;
        Node prevWithSkip = first;
        for (int i = 0; i < size; i++) {
            if (stepCounter == step) {
                stepCounter = 0;
                prevWithSkip.skip = current;
                prevWithSkip = current;
            }
            current = current.next;
        }
    }

    /**
     * Get first added ID in list
     * @return first added ID in list
     */
    public int getFirst() {
        return first.val;
    }

    /**
     * Get size of the list
     * @return size of the list
     */
    public int size() {
        return size;
    }

    /**
     * Is list empty?
     * @return whether a list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Converts ID list to a String.
     * A String will contain numbers, separated by whitespaces
     * (the same format as this class can read from,
     * so the generated String can then be converted to IDList again).
     * @return a String with numbers separated by whitespaces.
     */
    public String toString() {
        String str = new String();
        IDIterator itr = idIterator();
        while (itr.hasNext()) {
            str += itr.next() + " ";
        }
        return str;
    }

    public IDIterator idIterator() {
        return new IDIterator();
    }

    class IDIterator {

        private Node current = first;

        public boolean hasNext() {
            return current != null;
        }

        public Integer next() {
            Integer val = current.val;
            current = current.next;
            return val;
        }

        public Integer peek() {
            return current.val;
        }

        public Integer skip() {
            Integer val = current.skip.val;
            current = current.skip;
            return val;
        }

        public Integer skipPeek() {
            return current.skip.val;
        }

        public boolean hasSkip() {
            return current.skip != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
