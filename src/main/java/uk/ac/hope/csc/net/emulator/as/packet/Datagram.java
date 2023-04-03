package uk.ac.hope.csc.net.emulator.as.packet;

public class Datagram {

    public static final String TYPE_DATA = "DATA";

    public static long _id = 0;

    protected long id;
    protected long srcRouterId;
    protected long destRouterId;

    protected long ttl = 16;

    protected String type;

    public Datagram(long srcRouterId, long destRouterId ) {
        this.id = _id++;
        this.srcRouterId = srcRouterId;
        this.destRouterId = destRouterId;
        type = TYPE_DATA;
    }

    public long getId() {
        return id;
    }

    public long getSrcRouterId() {
        return srcRouterId;
    }

    public long getDestRouterId() {
        return destRouterId;
    }

    public long getTtl() {
        return ttl;
    }

    public void decTtl() {
        ttl--;
    }

    public String getType() {
        return type;
    }
}
