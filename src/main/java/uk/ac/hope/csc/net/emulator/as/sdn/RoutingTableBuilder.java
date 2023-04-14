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

        // How many nodes (Routers) in the graph (AS)
        long count = as.getRouterList().size();

        // knownBestRoutes - the N group - starts empty
        Map<Long, Hop> knownBestRoutes = new HashMap<>();

        // Candidate routes (including the one to us) all start with
        // cost == MAX_VALUE and previous hop router = -1;
        Map<Long, Hop> candidateRoutes = new HashMap<>();
        for(long i=0L; i<count; i++) {
            candidateRoutes.put(i, new Hop(i));
        }

        // Move our (starting) node out of the set of candidate nodes
        Hop lowNode = candidateRoutes.get(routerId);
        lowNode.routerId = routerId;
        lowNode.cost = 0;

        // Add our (starting) lowestCostNode to the solved 'N' group
        knownBestRoutes.put(lowNode.routerId, lowNode);
        candidateRoutes.remove(lowNode.routerId);

        // Start the looping Dijkstra algorithm
        while( candidateRoutes.size() > 0 ) {
            Router router = as.findRouterByRouterId(lowNode.routerId);
            for(BufferedOutCx cx : router.getBufferedOutCxes()) {
                long nextRouterId = cx.getReceivingRouterId();
                if(!knownBestRoutes.containsKey(nextRouterId)) {
                    // Cost from lowestCostNode to
                    long linkCost = cx.getLink().getCost();
                    // Update candidate node(s)
                    Hop br = candidateRoutes.get(nextRouterId);
                    if (lowNode.cost + linkCost < br.cost) {
                        br.cost = linkCost + lowNode.cost;
                        br.previousHopRouterId = lowNode.routerId;
                    }
                }
            }
            // Find the new lowest cost in the candidate routes
            lowNode = getLowestCostCandidateNode(candidateRoutes);
            knownBestRoutes.put(lowNode.routerId, lowNode);
            candidateRoutes.remove(lowNode.routerId);
        }

        // Now do the backwards lookup to create the RouterTable
        return buildRoutingTable(routerId, knownBestRoutes);
    }

    private static RoutingTable buildRoutingTable(long routerId, Map<Long, Hop> knownBestRoutes) {
        RoutingTable rt = new RoutingTable();
        for(Hop h : knownBestRoutes.values()) {
            if (routerId != h.routerId) {
                long dest = h.routerId;
                long nextHop = h.routerId;
                while (h.previousHopRouterId != routerId) {
                    h = knownBestRoutes.get(h.previousHopRouterId);
                    nextHop = h.routerId;
                }
                rt.setPath(dest, nextHop);
            }
        }
        return rt;
    }

    private static Hop getLowestCostCandidateNode(Map<Long, Hop> candidateNodes) {
        Hop ret = null;
        long currentowestCost = Long.MAX_VALUE;
        for ( Hop hac : candidateNodes.values() ) {
            if(hac.cost < currentowestCost) {
                currentowestCost = hac.cost;
                ret = hac;
            }
        }
        return ret;
    }

}


/**
 * Helper class
 */
class Hop {
    public Hop(long routerId) {
        this.routerId = routerId;
        this.previousHopRouterId = -1;
        this.cost = Long.MAX_VALUE;
    }
    long routerId;
    long previousHopRouterId;
    long cost;
}

