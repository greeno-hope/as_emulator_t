package uk.ac.hope.csc.net.emulator.as.sdn;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.component.buffers.BufferedOutCx;

import java.util.*;

public class RoutingTableBuilder {

    /**
     *
     * @param as
     * @param routerId
     * @return
     */
    public static RoutingTable buildWithLinkState(AutonomousSystem as, long routerId) {

        // The return class
        RoutingTable routingTable = new RoutingTable();

        long count = as.getRouterList().size();

        // knownBestRoutes - the N group - starts empty
        Map<Long, BestRoute> knownBestRoutes = new HashMap<>();

        // Candidate routes (including the one to us) all start with
        // cost == MAX_VALUE and previous hop router = -1;
        Map<Long, BestRoute> candidateRoutes = new HashMap<>();
        for(long i=0L; i<count; i++) {
            candidateRoutes.put(i, new BestRoute(i));
        }

        // Move our node out of the set of candidate nodes
        BestRoute temp = candidateRoutes.get(routerId);
        temp.destRouterId = routerId;
        temp.cost = 0;

        // Add our node to the solved 'N' group
        knownBestRoutes.put(temp.destRouterId, temp);

        // Start the looping Dijkstra algorithm
        while( candidateRoutes.size() > 0 ) {
            List<OneHop> nextHops = new ArrayList<>();
            Router router = as.findRouterByRouterId(temp.destRouterId);
            for(BufferedOutCx cx : router.getBufferedOutCxes()) {
                long nextRouterId = cx.getReceivingRouterId();
                if(!knownBestRoutes.containsKey(nextRouterId)) {
                    
                }
            }
        }

        return routingTable;
    }

}

/**
 * Helper class
 */
class BestRoute {

    public BestRoute(long destRouterId) {
        this.destRouterId = destRouterId;
        this.previousHopRouterId = -1;
        this.cost = Long.MAX_VALUE;
    }
    long destRouterId;
    long previousHopRouterId;
    long cost;
}

/**
 * Helper class
 */
class OneHop {
    OneHop(long hereId, long thereId, long hopCost) {
        this.hereId = hereId;
        this.thereId = thereId;
        this.hopCost = hopCost;
    }
    long hereId;
    long thereId;
    long hopCost;
}