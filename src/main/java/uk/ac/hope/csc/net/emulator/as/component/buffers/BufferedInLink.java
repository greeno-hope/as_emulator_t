package uk.ac.hope.csc.net.emulator.as.component.buffers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.hope.csc.net.emulator.as.component.Link;
import uk.ac.hope.csc.net.emulator.as.component.Router;

public class BufferedInLink {

    private static Logger log = LoggerFactory.getLogger(BufferedInLink.class);

    private long id;

    private RouterInBuffer buffer;
    private Link link;

    public BufferedInLink(Router router, Link link) {
        // The link that will write to this BufferedInLink
        this.link = link;
        // Set the id of this BufferedInLink to be the id of the
        // previous hop router
        this.id = link.getTerminatingRouter(router).getId();
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
        return id;
    }
}
