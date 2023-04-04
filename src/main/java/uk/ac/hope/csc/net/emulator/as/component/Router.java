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

// TODO - Do I want to identify outlinks in terms of nextHopRouterIds rather than link ID's per-se ?
// TODO - YES - it means that I can get the router endpoints (especially the in router) more easily
// TODO - Routing tables are easier to configure (perhaps) as dest, nestHopRouter (both longs) ?

public class Router implements Tickable {

    static Logger log = LoggerFactory.getLogger(Router.class);

    public static final long BROADCAST = -1;

    /**
     * The AS to which THIS Router belongs
     */
    protected AutonomousSystem autonomousSystem;

    private static long _id = 0;

    /**
     * Router ID.This acts as an address field for the AS emulator
     */
    protected long id;

    /**
     * This BufferedInCx acts as the in buffer from the (fake) subnet that this router sits at the border of
     */
    protected BufferedInCx lanInBuffer;

    protected List<BufferedInCx> bufferedInCxes;
    protected List<BufferedOutCx> bufferedOutCxes;

    protected RoutingTable routingTable;

    public Router(AutonomousSystem autonomousSystem) {
        this.autonomousSystem = autonomousSystem;
        this.id = _id++;
        lanInBuffer = new BufferedInCx(this, null);
        bufferedInCxes = new LinkedList<>();
        bufferedOutCxes = new LinkedList<>();
        routingTable = new RoutingTable();
    }

    public void addLink(Link link) {

        // Set up the in links
        BufferedInCx bufferedInCx = new BufferedInCx(this, link);
        bufferedInCxes.add(bufferedInCx);
        // Set up the out links
        BufferedOutCx bufferedOutCx = new BufferedOutCx(this, link);
        bufferedOutCxes.add(bufferedOutCx);
    }

    public void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void resetRoutingTable() {
        routingTable = new RoutingTable();
    }

    public long getId() {
        return id;
    }

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
                d.decTtl();
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
