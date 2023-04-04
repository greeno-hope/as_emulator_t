package uk.ac.hope.csc.net.emulator.as.component.buffers;

import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

import java.util.LinkedList;
import java.util.Queue;

public  class RouterInBuffer {

    public static long MAX_BUFF_SIZE = 255;

    protected Queue<Datagram> inQueue;

    public static void setMaxBufSize(long maxBufSize) {
        MAX_BUFF_SIZE = maxBufSize;
    }

    public RouterInBuffer() {
        inQueue = new LinkedList<>();
    }

    public long size() {
        return inQueue.size();
    }

    public void enqueue(Datagram d) {
        inQueue.add(d);
    }

    public Datagram peek() {
        return inQueue.peek();
    }

    public Datagram dequeue() {
        return inQueue.poll();
    }

}