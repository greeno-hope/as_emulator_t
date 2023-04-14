package uk.ac.hope.csc.net.emulator.as.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.hope.csc.net.emulator.as.Tickable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedInCx;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;


/**
 * Link just represents a physical link between 2 routers identified as 0 and 1
 * xxx01 and xxx10 in the attribute names concerns metadata 0->1 and 1->0 respectively.
 * Link cost is modelled by number of ticks before the datagram is written to a terminating
 * in buffer in the receiving router.
 * Tick timing is set in the main function (for text only app).
 */
public class Link implements Tickable {

    /**
     * private logger
     */
    private static Logger log = LoggerFactory.getLogger(Link.class);

    /**
     * Used to create unique IDs for links
     */
    private static long _id = 0;

    /**
     * Cost (in ticks) of this link
     */
    long cost;

    /**
     * id (unique) of this link
     */
    long id;

    /**
     * Terminating router 0
     */
    private Router router0;

    /**
     * Terminating router 1
     */
    private Router router1;

    /**
     * Datagram reference (placeholder) for a datagram travelling
     * 0 -> 1
     */
    private Datagram datagram01;

    /**
     * Datagram reference (placeholder) for a datagram travelling
     * 1 -> 0
     */
    private Datagram datagram10;

    /**
     * Countdown for delay 0->1
     */
    long delayTicks01;

    /**
     * Countdown for delay 1->0
     */
    long delayTicks10;

    public boolean sendDatagram(Datagram datagram, Router from, Router to) {
        boolean ret = false;
        if(from == router0){
            // 0 to 1 traffic
            if(datagram01 == null) {
                datagram01 =  datagram;
                delayTicks01 = cost;
                ret = true;
                log.info("Link:{} - accepting datagram id:{} in_router:{} out_router:{}", id, datagram.getId(), router0.getId(), router1.getId());
            }
        } else {
            // 1 to 0 traffic
            if(datagram10 == null){
                datagram10 =  datagram;
                delayTicks10 = cost;
                ret = true;
                log.info("Link:{} - accepting datagram id:{} in_router:{} out_router:{}", id, datagram.getId(), router1.getId(), router0.getId());
            }
        }
        return ret;
    }

    /**
     *
     * @param router0
     * @param router1
     * @param cost
     */
    public Link(Router router0, Router router1, long cost) {
        this.router0 = router0;
        this.router1 = router1;
        this.cost = cost;
        this.id = _id++;
    }

    /**
     *
     */
    @Override
    public void handleTick() {

        if(datagram01 != null) {
            if(0 == delayTicks01--) {
                // There's a datagram from A to B and its link cost has expired.
                // relay the datagram to the terminating router inCx
                BufferedInCx bil = router1.getBufferedInLinkByLinkId(id);
                bil.getBuffer().enqueue(datagram01);
                log.info("Link:{} - writing datagram id:{} from in_router:{} to out_router:{}", id, datagram01.getId(), router0.getId(), router1.getId());
                datagram01 = null;
            }
        }

        if(datagram10 != null) {
            if(0 == delayTicks10--) {
                // There's a datagram from B to A and its link cost has expired.
                // relay the datagram to the terminating router inCx
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
