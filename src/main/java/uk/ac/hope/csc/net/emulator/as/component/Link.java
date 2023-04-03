package uk.ac.hope.csc.net.emulator.as.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.hope.csc.net.emulator.as.Tickable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedInLink;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

public class Link implements Tickable {

    private static Logger log = LoggerFactory.getLogger(Link.class);

    private static long _id = 0;

    private Router routerA;
    private Router routerB;

    private Datagram datagramAtoB;
    private Datagram datagramBtoA;

    long aToBCountdown;
    long bToACountdown;

    long cost;
    long id;

    public boolean sendDatagram(Datagram datagram, Router from, Router to) {
        boolean ret = false;
        if(from == routerA){
            // A to B traffic
            if(datagramAtoB == null) {
                datagramAtoB =  datagram;
                aToBCountdown = cost;
                ret = true;
            }
        } else {
            // B to A traffic
            if(datagramBtoA == null){
                datagramBtoA =  datagram;
                bToACountdown = cost;
                ret = true;
            }
        }
        return ret;
    }

    public Link(Router routerA, Router routerB, long cost) {
        this.routerA = routerA;
        this.routerB = routerB;
        this.cost = cost;
        this.id = _id++;
    }

    @Override
    public void handleTick() {

        if(datagramAtoB != null) {
            if(0 == aToBCountdown--) {
                // There's a datagram from A to B and its cost has expired.
                // relay the datagram to a router in link
                BufferedInLink bil = routerB.getBufferedInLinkByLinkId(id);
                bil.getBuffer().enqueue(datagramAtoB);
                datagramAtoB = null;
            }
        }

        if(datagramBtoA != null) {
            if(0 == bToACountdown--) {
                // There's a datagram from B to A and its cost has expired.
                // relay the datagram to a router in link
                BufferedInLink bil = routerA.getBufferedInLinkByLinkId(id);
                bil.getBuffer().enqueue(datagramBtoA);
                datagramBtoA = null;
            }
        }
    }

    public Router getRouterA() {
        return routerA;
    }

    public Router getRouterB() {
        return routerB;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getId() {
        return id;
    }

    public boolean busy(Router queryingRouter) {
        if(queryingRouter == routerA) {
            return datagramAtoB != null;
        } else {
            return datagramBtoA != null;
        }
    }

    public Router getTerminatingRouter(Router queryingRouter) {
        if(queryingRouter == routerA) {
            return routerB;
        } else if (queryingRouter == routerB){
            return routerA;
        } else {
            throw new IllegalArgumentException("Invalid or null router");
        }
    }
}
