package uk.ac.hope.csc.net.emulator.as.component.buffers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.hope.csc.net.emulator.as.component.Link;
import uk.ac.hope.csc.net.emulator.as.component.Router;

public class BufferedOutCx {

    private static Logger log = LoggerFactory.getLogger(BufferedInCx.class);

    private long receivingRouterId;

    private RouterOutBuffer buffer;
    private Link link;

    public BufferedOutCx(Router router, Link link) {
        // the link that this BufferedOutLink will write to
        this.link = link;
        // Make the ID of this BufferedOutLink the same as the ID
        // of the router at the other end
        this.receivingRouterId = link.getTerminatingRouter(router).getId();
        // Create the actual buffer
        buffer = new RouterOutBuffer();
    }

    public RouterOutBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(RouterOutBuffer buffer) {
        this.buffer = buffer;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public long getReceivingRouterId(){
        return receivingRouterId;
    }



}
