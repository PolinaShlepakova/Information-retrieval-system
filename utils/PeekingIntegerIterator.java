package utils;

import java.util.Iterator;

public class PeekingIntegerIterator implements Iterator<Integer> {

    private int cache;
    private Iterator<Integer> itr;
    private boolean hasNext;

    public PeekingIntegerIterator(Iterator<Integer> iterator) {
        itr = iterator;
        hasNext = iterator.hasNext();
        if (hasNext) {
            cache = itr.next();
        }
    }

    public Integer peek() {
        return cache;
    }

    @Override
    public Integer next() {
        int curr = cache;
        hasNext = itr.hasNext();
        if (hasNext) {
            cache = itr.next();
        }
        return curr;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }
}
