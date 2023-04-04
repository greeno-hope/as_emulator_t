package uk.ac.hope.csc.net.emulator.as.component;

import java.util.HashMap;
import java.util.Map;

/**
 *  Simple wrapper class. Each entry is:
 *      <Long destRouterId, Long linkId>
 *  Where the linkId == the next hop router id.
 *  NOTE: this id is not the same as the link's own id
 */
public class RoutingTable {

    Map<Long, Long> routingMap;

    /**
     *  c'tor
     */
    public RoutingTable() {
        routingMap = new HashMap<>();
    }

    /**
     *
     * @param destRouterId
     * @param linkId
     */
    public void setPath(long destRouterId, long linkId) {
        routingMap.put(destRouterId, linkId);
    }

    /**
     *
     * @param destRouterId
     * @return
     */
    public long getNextHopLinkId(long destRouterId) {
        return routingMap.get(destRouterId);
    }

}
