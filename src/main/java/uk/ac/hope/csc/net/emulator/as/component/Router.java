package uk.ac.hope.csc.net.emulator.as.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import uk.ac.hope.csc.net.emulator.as.Tickable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedInLink;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedOutLink;
import uk.ac.hope.csc.net.emulator.as.component.buffers.RouterInBuffer;
import uk.ac.hope.csc.net.emulator.as.component.buffers.RouterOutBuffer;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

// TODO - Do I want to identify outlinks in terms of nextHopRouterIds rather than link ID's per-se ?
// TODO - YES - it means that I can get the router endpoints (especially the in router) more easily
// TODO - Routing tables are easier to configure (perhaps) as dest, nestHopRouter (both longs) ?

public class Router implements Tickable {

    static Logger log = LoggerFactory.getLogger(Router.class);

    private static long _id = 0;

    protected long id;

    protected RouterInBuffer sourceBuffer;

    protected List<BufferedInLink> bufferedInLinks;
    protected List<BufferedOutLink> bufferedOutLinks;

    protected RoutingTable routingTable;

    public Router() {
        this.id = _id++;
        sourceBuffer = new RouterInBuffer();
        bufferedInLinks = new LinkedList<>();
        bufferedOutLinks = new LinkedList<>();
        routingTable = new RoutingTable();
    }

    public void addLink(Link link) {
        // Set up the in links
        BufferedInLink bufferedInLink = new BufferedInLink(this, link);
        bufferedInLinks.add(bufferedInLink);
        // Set up the out links
        BufferedOutLink bufferedOutLink = new BufferedOutLink(this, link);
        bufferedOutLinks.add(bufferedOutLink);
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

    public BufferedOutLink findBufferedOutLinkById(long id) {
        for(BufferedOutLink out : bufferedOutLinks) {
            if(out.getId() == id) {
                return out;
            }
        }
        return null;
    }

    public void sendDatagram(Datagram d) {
        log.info("Router {} - sending datagram id:{} type:{} from:{} to:{} ttl={}", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId(), d.getTtl());
        sourceBuffer.enqueue(d);
    }

    public BufferedInLink getBufferedInLinkByLinkId(long linkId) {
        for(BufferedInLink bil : bufferedInLinks) {
            if(linkId == bil.getLinkId()) {
                return bil;
            }
        }
        return null;
    }

    private void processInBuffer(RouterInBuffer rin) {
        if(rin.size() > 0) {
            // Dequeue
            Datagram d = rin.dequeue();
            if (d.getTtl() != 0) {
                d.decTtl();
                if(d.getDestRouterId() == id) {
                    // If the datagram for THIS router then log that it's arrived
                    log.info("Router {} - datagram id:{} type:{} from:{} to:{} DELIVERED", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId());
                } else {
                    // pass it to the correct OutBuffer
                    long outLinkId = routingTable.getNextHopLinkId(d.getDestRouterId());
                    // Write to the link outBuffer
                    BufferedOutLink out = findBufferedOutLinkById(outLinkId);
                    if(out != null) {
                        out.getBuffer().enqueue(d);
                    }
                    log.info("Router {} - routing datagram id:{} type:{} from:{} to:{} ttl:{} nextHop:{}", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId(), d.getTtl(), outLinkId);
                }
            } else {
                // Log that the Datagram has timed out
                log.info("Router {} - datagram id:{} type:{} from:{} to:{} EXPIRED ttl=0", id, d.getId(), d.getType(), d.getSrcRouterId(), d.getDestRouterId());
            }
        }
    }

    private void processSourceBuffer() {
        processInBuffer(sourceBuffer);
    }

    private void processInBuffers() {
        // Loop the in buffers and pass datagrams through the matrix
        for(BufferedInLink lin : bufferedInLinks) {
            // Is there anything to de-queue
            RouterInBuffer rin = lin.getBuffer();
            processInBuffer(rin);
        }
    }

    private void processOutBuffers() {
        // Loop the out buffers and attempt to write onto the links
        for(BufferedOutLink bout : bufferedOutLinks) {
            RouterOutBuffer rout = bout.getBuffer();
            if(rout.size() > 0) {
                Datagram d = rout.dequeue();
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
