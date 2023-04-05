package uk.ac.hope.csc.net.emulator.as.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.Tickable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedInCx;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedOutCx;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

public class Router implements Tickable {

    static Logger log = LoggerFactory.getLogger(Router.class);

    public static final long BROADCAST = -1;

    /**
     * The AS to which THIS Router belongs
     */
    protected AutonomousSystem autonomousSystem;

    /**
     * Just used as an incrementing ID generator
     */
    private static long _id = 0;

    /**
     * Router ID.This acts as an address field for the AS emulator
     */
    protected long id;

    /**
     * This BufferedInCx acts as the in buffer from the (fake) subnet that this router sits at the border of
     */
    protected BufferedInCx lanInBuffer;

    /**
     * List of buffered in connections
     */
    protected List<BufferedInCx> bufferedInCxes;

    /**
     * List of buffered out connections
     */
    protected List<BufferedOutCx> bufferedOutCxes;

    /**
     * Wrapper class for a Map<Long, Long> that maps a destination router ID
     * to a next hop router ID
     */
    protected RoutingTable routingTable;

    /**
     * Constructor
     * @param autonomousSystem - is a reference to the AS. means that if we breakpoint in
     *                         a router function we can see what's going on around us.
     */
    public Router(AutonomousSystem autonomousSystem) {
        this.autonomousSystem = autonomousSystem;
        this.id = _id++;
        lanInBuffer = new BufferedInCx(this, null);
        bufferedInCxes = new LinkedList<>();
        bufferedOutCxes = new LinkedList<>();
        routingTable = new RoutingTable();
    }

    /**
     * Adds a 2-way (undirected link) this means we need one in buffer and one
     * out buffer for the link
     * @param link
     */
    public void addUndirectedLink(Link link) {

        // Set up the in links
        BufferedInCx bufferedInCx = new BufferedInCx(this, link);
        bufferedInCxes.add(bufferedInCx);
        // Set up the out links
        BufferedOutCx bufferedOutCx = new BufferedOutCx(this, link);
        bufferedOutCxes.add(bufferedOutCx);
    }

    /**
     * Sets a new routing table for this router. Can use this to simulate SDNs
     * @param routingTable
     */
    public void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    /**
     * Flushes the routing table. Can do this as part of a network reset
     * after pertubation etc.
     */
    public void resetRoutingTable() {
        routingTable = new RoutingTable();
    }

    /**
     * Returns the Router id. In this implementation the id is the address.
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @param id
     * @return
     */
    public BufferedOutCx findBufferedOutLinkById(long id) {
        for(BufferedOutCx out : bufferedOutCxes) {
            if(out.getReceivingRouterId() == id) {
                return out;
            }
        }
        return null;
    }

    public void sendDatagram(Datagram d) {
        log.info("Router {} - sending datagram id:{} type:{} src:{} dest:{} ttl={}", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId(), d.getTtl());
        lanInBuffer.getBuffer().enqueue(d);
    }

    public BufferedInCx getBufferedInLinkByLinkId(long linkId) {
        for(BufferedInCx bil : bufferedInCxes) {
            if(linkId == bil.getLinkId()) {
                return bil;
            }
        }
        return null;
    }

    private void processInBuffer(BufferedInCx bin) {
        if(bin.getBuffer().size() > 0) {
            // Dequeue
            Datagram d = bin.getBuffer().dequeue();
            if (d.getTtl() != 0) {
                if(d.getSrcRouterId() != id) {
                    // Decrement the hop counter IF the datagram does not originate here
                    d.decTtl();
                }
                if(d.getDestRouterId() == id) {
                    log.info("Router {} - datagram id:{} type:{} src:{} dest:{} DELIVERED", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId());
                } else {
                    if(d.getDestRouterId() == BROADCAST) {
                        for(BufferedOutCx bol : bufferedOutCxes) {
                            //if(rin.)
                            bol.getBuffer().enqueue(new Datagram(d));
                            log.info("Router {} - broadcasting datagram id:{} type:{} to:{} ttl:{}", id, d.getId(), d.getType(), bol.getReceivingRouterId(), d.getTtl());
                        }
                    } else {
                        // pass it to the correct OutBuffer
                        long outLinkId = routingTable.getNextHopLinkId(d.getDestRouterId());
                        // Write to the link outBuffer
                        BufferedOutCx out = findBufferedOutLinkById(outLinkId);
                        if(out != null) {
                            out.getBuffer().enqueue(new Datagram(d));
                        }
                        log.info("Router {} - routing datagram id:{} type:{} src:{} dest:{} ttl:{} nextHop:{}", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId(), d.getTtl(), outLinkId);
                    }
                }
            } else {
                // Log that the Datagram has timed out
                log.info("Router {} - datagram id:{} type:{} src:{} dest:{} EXPIRED ttl=0", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId());
            }
        }
    }

    private void processSourceBuffer() {
        processInBuffer(lanInBuffer);
    }

    private void processInBuffers() {
        // Loop the in buffers and pass datagrams through the matrix
        for(BufferedInCx lin : bufferedInCxes) {
            // Is there anything to de-queue
            processInBuffer(lin);
        }
    }

    private void processOutBuffers() {
        // Loop the out buffers and attempt to write onto the links
        for(BufferedOutCx bout : bufferedOutCxes) {
            if(bout.getBuffer().size() > 0) {
                Datagram d = bout.getBuffer().dequeue();
                Link link = bout.getLink();
                if(! link.busy(this)) {
                    bout.getLink().sendDatagram(d, this, bout.getLink().getTerminatingRouter(this));
                }
            }
        }
    }

    @Override
    public void handleTick() {
        // TODO use a mod (%) operator and only process one buffer per tick ?
        processSourceBuffer();
        processInBuffers();
        processOutBuffers();
    }

}
