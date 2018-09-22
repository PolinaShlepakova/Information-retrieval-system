package main.indexes;

import java.io.Serializable;
import java.util.LinkedList;

public class PositionalIndex implements Serializable {

    private Node first;
    private Node last;
    private int size;

    public class Node implements Serializable {
        Node next;
        Node skip;
        int ID;
        LinkedList<Integer> positions;

        private Node(int ID) {
            this(ID, null);
        }

        private Node(int ID, LinkedList<Integer> positions) {
            this.next = null;
            this.skip = null;
            this.ID = ID;
            this.positions = positions;
        }

        public int getID() {
            return ID;
        }

        public LinkedList<Integer> getPositions() {
            return positions;
        }

        private void add(int pos) {
            if (positions == null) {
                positions = new LinkedList<Integer>();
            }
            positions.add(pos);
        }

        public int getFirst() {
            return positions.getFirst();
        }

        public String toString() {
            if (positions == null) {
                return new String("ID: " + ID + " ");
            }
            String res = new String();
            res += "ID: " + ID + ", size: " + positions.size() + ", pos: ";
            for (Integer i : positions) {
                res += i + ", ";
            }
            return res;
        }
    }

    public PositionalIndex() {
        this.first = null;
        this.last = null;
        size = 0;
    }

    /**
     * Adds new node with specified id
     *
     * @param id
     */
    public void addLast(int id) {
        Node oldLast = last;
        last = new Node(id);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Add specified position to a node with specified id.
     * If such node does not exist, creates it.
     * @param ID
     * @param pos
     */
    public void addLast(int ID, int pos) {
        if (!isEmpty() && last.ID == ID) {
            last.positions.add(pos);
        } else {
            addLastNode(ID, pos);
        }
    }

    /**
     * Adds new node with specified id and a list, which contains 1 specified index
     * @param ID
     * @param pos
     */
    public void addLastNode(int ID, int pos) {
        assert(!isEmpty() && ID > last.ID);
        Node oldLast = last;
        last = new Node(ID);
        last.add(pos);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Adds new specified node to the end of the list
     * @param node
     */
    public void addLast(Node node) {
        assert(node.ID > last.ID);
        Node oldLast = last;
        last = node;
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Add new Node to the end of the list
     *
     * @param ID id the new node will have
     * @param positions list of positions new node will have
     */
    public void addLastNode(int ID, LinkedList<Integer> positions) {
        assert(!isEmpty() && ID > last.ID);
        Node oldLast = last;
        last = new Node(ID, positions);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    /**
     * Finds Node with specified id and adds specified position to it
     *
     * @param id id to addLastNode pos to
     * @param pos position in doc with specified ID to addLastNode
     */
    public boolean addPos(int id, int pos) {
        NodeIterator itr = nodeIterator();
        while (itr.hasNext()) {
            Node curr = itr.next();
            if (curr.ID == id) {
                curr.add(pos);
                return true;
            }
        }
        return false;
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

    public int getFirstID() {
        return first.ID;
    }

    public Node getFirst() {
        return first;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     *
     * @return info about each Node
     */
    public String toString() {
        String str = "";
        NodeIterator itr = nodeIterator();
        while (itr.hasNext()) {
            str += itr.next() + "\n";
        }
        return str;
    }

    /**
     *
     * @return IDs this index contains, separated by whitespace
     */
    public String toStringSimple() {
        String str = new String();
        IDIterator itr = idIterator();
        while (itr.hasNext()) {
            str += itr.next() + " ";
        }
        return str;
    }

    public NodeIterator nodeIterator() {
        return new NodeIterator();
    }

    public class NodeIterator {

        private Node current = first;

        public boolean hasNext() {
            return current != null;
        }

        public Node next() {
            Node node = current;
            current = current.next;
            return node;
        }

        public Node peek() {
            return current;
        }

        public Node skip() {
            Node node = current.skip;
            current = current.skip;
            return node;
        }

        public Node skipPeek() {
            return current.skip;
        }

        public boolean hasSkip() {
            return current.skip != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public IDIterator idIterator() {
        return new IDIterator();
    }

    public class IDIterator {

        private Node current = first;

        public boolean hasNext() {
            return current != null;
        }

        public Integer next() {
            Integer val = current.ID;
            current = current.next;
            return val;
        }

        public Integer peek() {
            return current.ID;
        }

        public Integer skip() {
            Integer val = current.skip.ID;
            current = current.skip;
            return val;
        }

        public Integer skipPeek() {
            return current.skip.ID;
        }

        public boolean hasSkip() {
            return current.skip != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
