package main.indexes;

import main.PostingZones;

import java.io.Serializable;

/**
 * @author Polina Shlepakova
 */
public class ZonesIDList implements Serializable {

    private Node first;
    private Node last;
    private int size;

    class Node implements Serializable {
        Node next;
        Node skip;
        PostingZones val;

        Node(PostingZones val) {
            this.next = null;
            this.skip = null;
            this.val = val;
        }

        public String toString() {
            return "" + val;
        }
    }

    public ZonesIDList() {
        this.first = null;
        this.last = null;
        size = 0;
    }

    public ZonesIDList(PostingZones firstVal) {
        this.first = new Node(firstVal);
        this.last = first;
        size = 1;
    }

    /**
     * Creates ZonesIDList from a line with IDs, separated by whitespaces
     *
     * @param line
     */
    public ZonesIDList(String line) {
        String[] input = line.split(" ");
        int len = input.length;
        switch (len) {
            case 0:
                this.first = null;
                this.last = null;
                break;
            case 1:
                this.first = new Node(new PostingZones(input[0]));
                this.last = first;
                break;
            default:
                this.first = new Node(new PostingZones(input[0]));
                Node prev = this.first;
                for (int i = 1; i < len; i++) {
                    Node curr = new Node(new PostingZones(input[i]));
                    prev.next = curr;
                    prev = curr;
                }
                this.last = prev;
                break;
        }
        this.size = len;
    }

    public void add(PostingZones val) {
        Node oldLast = last;
        last = new Node(val);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    public void addNoRepeat(PostingZones val) {
        if (last.val.getID() == val.getID()) {
            last.val.addZone(val.getZone());
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

    public void addSkips() {
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

    public PostingZones getFirst() {
        return first.val;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public String toString() {
        String str = "";
        IDIterator itr = idIterator();
        while (itr.hasNext()) {
            str += itr.next() + " ";
        }
        return str;
    }

    public PostingZones[] toArray() {
        PostingZones[] arr = new PostingZones[size];
        IDIterator itr = new IDIterator();
        for (int i = 0; i < size && itr.hasNext(); i++) {
            arr[i] = itr.next();
        }
        return arr;
    }

    public IDIterator idIterator() {
        return new IDIterator();
    }

    class IDIterator {

        private Node current = first;

        public boolean hasNext() {
            return current != null;
        }

        public PostingZones next() {
            PostingZones val = current.val;
            current = current.next;
            return val;
        }

        public PostingZones peek() {
            return current.val;
        }

        public PostingZones skip() {
            PostingZones val = current.skip.val;
            current = current.skip;
            return val;
        }

        public PostingZones skipPeek() {
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
