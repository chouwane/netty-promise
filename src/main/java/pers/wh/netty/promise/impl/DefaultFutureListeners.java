package pers.wh.netty.promise.impl;

import pers.wh.netty.promise.GenericFutureListener;

import java.util.Arrays;

/**
 * copy from netty 4.1.29.Final-SNAPSHOT
 * 维护监听器
 */
final class DefaultFutureListeners {

    private GenericFutureListener[] listeners;
    private int size;

    @SuppressWarnings("unchecked")
    DefaultFutureListeners(GenericFutureListener first, GenericFutureListener second) {
        listeners = new GenericFutureListener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
    }

    public void add(GenericFutureListener l) {
        GenericFutureListener[] listeners = this.listeners;
        final int size = this.size;
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        listeners[size] = l;
        this.size = size + 1;
    }

    public void remove(GenericFutureListener l) {
        final GenericFutureListener[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i ++) {
            if (listeners[i] == l) {
                int listenersToMove = size - i - 1;
                if (listenersToMove > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
                }
                listeners[-- size] = null;
                this.size = size;

                return;
            }
        }
    }

    public GenericFutureListener[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

}
