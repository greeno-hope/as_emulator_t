package uk.ac.hope.csc.net.emulator.as;

import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.component.Link;
import uk.ac.hope.csc.net.emulator.utils.FileUtils;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

public class AutonomousSystem implements Tickable {


    List<Router> routerList;
    List<Link> linkList;

    /**
     * Default (no-arg) constructor. Just initialtes the
     * two main component lists
     */
    public AutonomousSystem() {
        // List setup
        routerList = new LinkedList<>();
        linkList = new LinkedList<>();
    }

    /**
     * Just creates an AS of a set number of unconnected Routers
     * @param routerCount
     */
    public AutonomousSystem(long routerCount) {
        this();
        // Create routerCount initial routers
        for (long i=0; i<routerCount; i++) {
            routerList.add(new Router(this));
        }
    }

    /**
     * Loads a previously saved (or authored) network from a CSV file
     * See FileUtils.loadNetworkFromCsv() for more details
     * @param path
     * @throws FileNotFoundException
     */
    public void loadNetworkFromCsv(String path) throws FileNotFoundException {
        FileUtils.loadUndirectedNetworkFromCsv(this, path);
    }

    /**
     * Sets a (generated elsewhere) routing table for a router. Principally going to
     * be used to support simple SDN style routing.
     * @param routerId
     * @param routingTable
     */
    public void setRoutingTable(long routerId, RoutingTable routingTable) {
        findRouterByRouterId(routerId).setRoutingTable(routingTable);
    }

    /**
     * Creates a link between 2 Routers, if either endpoint is not currently
     * in the Router list then create it and add to the list.
     * @param idR1
     * @param idR2
     * @param cost
     */
    public void addlink(long idR1, long idR2, long cost) {

        // Find (or create) the routers
        Router r1 = findRouterByRouterId(idR1);
        if (r1 == null) {
            r1 = new Router(this);
            routerList.add(r1);
        }
        Router r2 = findRouterByRouterId(idR2);
        if (r2 == null) {
            r2 = new Router(this);
            routerList.add(r2);
        }

        // Create the link and wire it to the routers
        Link link = new Link(r1, r2, cost);

        // Wire the routers to the link
        r1.addUndirectedLink(link);
        r2.addUndirectedLink(link);

        // Store the Link id locally
        linkList.add(link);
    }

    /**
     * Driven from the main UI event loop either JavaFX
     * or
     */
    @Override
    public void handleTick() {

        for(Router r : routerList) {
            r.handleTick();
        }

        for(Link l : linkList) {
            l.handleTick();
        }
    }

    public Router findRouterByRouterId(long id) {
        for(Router r : routerList) {
            if(id == r.getId()) {
                return r;
            }
        }
        return null;
    }

    public Link findLink(long id) {
        for(Link l : linkList) {
            if(id == l.getId()) {
                return l;
            }
        }
        return null;
    }
}
