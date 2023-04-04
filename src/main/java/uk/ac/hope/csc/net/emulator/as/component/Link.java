package uk.ac.hope.csc.net.emulator.as.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.hope.csc.net.emulator.as.Tickable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedInCx;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

public class Link implements Tickable {

    private static Logger log = LoggerFactory.getLogger(Link.class);

    private static long _id = 0;

    private Router router0;
    private Router router1;

    private Datagram datagram01;
    private Datagram datagram10;

    long countdown01;
    long countdown10;

    long cost;
    long id;

    public boolean sendDatagram(Datagram datagram, Router from, Router to) {
        boolean ret = false;
        if(from == router0){
            // A to B traffic
            if(datagram01 == null) {
                datagram01 =  datagram;
                countdown01 = cost;
                ret = true;
                log.info("Link:{} - accepting datagram id:{} in_router:{} out_router:{}", id, datagram.getId(), router0.getId(), router1.getId());
            }
        } else {
            // B to A traffic
            if(datagram10 == null){
                datagram10 =  datagram;
                countdown10 = cost;
                ret = true;
                log.info("Link:{} - accepting datagram id:{} in_router:{} out_router:{}", id, datagram.getId(), router1.getId(), router0.getId());
            }
        }
        return ret;
    }

    public Link(Router router0, Router router1, long cost) {
        this.router0 = router0;
        this.router1 = router1;
        this.cost = cost;
        this.id = _id++;
    }

    @Override
    public void handleTick() {

        if(datagram01 != null) {
            if(0 == countdown01--) {
                // There's a datagram from A to B and its cost has expired.
                // relay the datagram to a router in link
                BufferedInCx bil = router1.getBufferedInLinkByLinkId(id);
                bil.getBuffer().enqueue(datagram01);
                log.info("Link:{} - writing datagram id:{} from in_router:{} to out_router:{}", id, datagram01.getId(), router0.getId(), router1.getId());
                datagram01 = null;
            }
        }

        if(datagram10 != null) {
            if(0 == countdown10--) {
                // There's a datagram from B to A and its cost has expired.
                // relay the datagram to a router in link
                BufferedInCx bil = router0.getBufferedInLinkByLinkId(id);
                bil.getBuffer().enqueue(datagram10);
                log.info("Link:{} - writing datagram id:{} from in_router:{} to out_router:{}", id, datagram10.getId(), router1.getId(), router0.getId());
                datagram10 = null;
            }
        }
    }

    public Router getRouter0() {
        return router0;
    }

    public Router getRouter1() {
        return router1;
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
        if(queryingRouter == router0) {
            return datagram01 != null;
        } else {
            return datagram10 != null;
        }
    }

    public Router getTerminatingRouter(Router queryingRouter) {
        if(queryingRouter == router0) {
            return router1;
        } else if (queryingRouter == router1){
            return router0;
        } else {
            throw new IllegalArgumentException("Invalid or null router");
        }
    }
}
