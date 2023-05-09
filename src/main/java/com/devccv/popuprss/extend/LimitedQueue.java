package com.devccv.popuprss.extend;

import java.util.LinkedList;

public final class LimitedQueue<E> extends LinkedList<E> {

    private final int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        boolean added = super.add(o);
        while (added && size() > limit) {
            super.remove();
        }
        return added;
    }
}
