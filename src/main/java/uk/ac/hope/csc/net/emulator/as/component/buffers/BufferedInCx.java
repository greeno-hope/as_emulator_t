package uk.ac.hope.csc.net.emulator.as.component.buffers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.hope.csc.net.emulator.as.component.Link;
import uk.ac.hope.csc.net.emulator.as.component.Router;

public class BufferedInCx {

    private static Logger log = LoggerFactory.getLogger(BufferedInCx.class);

    private long sendingRouterId;
    private long linkId;

    private RouterInBuffer buffer;
    private Link link;

    public BufferedInCx(Router router, Link link) {
        // The link that will write to this BufferedInLink
        this.link = link;
        if(link != null) {
            // Set the ID of this BufferedInLink to be the ID of the
            // previous hop router
            this.sendingRouterId = link.getTerminatingRouter(router).getId();
            this.linkId = link.getId();
        } else {
            // If link is null we are a LAN source buffer just make
            // our ID equal the router ID
            this.sendingRouterId = router.getId();
            this.linkId = -1;
        }
        // Create a new in buffer
        buffer = new RouterInBuffer();
    }

    public RouterInBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(RouterInBuffer buffer) {
        this.buffer = buffer;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public long getLinkId() {
        return linkId;
    }

}
