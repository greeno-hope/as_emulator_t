package uk.ac.hope.csc.net.emulator.as.component.buffers;

import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

import java.util.LinkedList;
import java.util.Queue;

public  class RouterOutBuffer {

    protected Queue<Datagram> outQueue;

    public RouterOutBuffer() {
        outQueue = new LinkedList<>();
    }

    public long size() {
        return outQueue.size();
    }

    public void enqueue(Datagram d) {
        // TODO max buff size
        outQueue.add(d);
    }

    public Datagram peek() {
        return outQueue.peek();
    }

    public Datagram dequeue() {
        return outQueue.poll();
    }

}