package uk.ac.hope.csc.net.emulator.as;

import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.component.Link;

import java.util.LinkedList;
import java.util.List;

public class AutonomousSystem implements Tickable {


    List<Router> routerList;
    List<Link> linkList;

    public AutonomousSystem(long routerCount) {

        // List setup
        routerList = new LinkedList<>();
        linkList = new LinkedList<>();

        // Create routerCount initial routers
        for (long i=0; i<routerCount; i++) {
            routerList.add(new Router());
        }
    }

    public void setRoutingTable(long routerId, RoutingTable routingTable) {
        findRouterByRouterId(routerId).setRoutingTable(routingTable);
    }

    public void addLink(long idR1, long idR2, long cost) {

        // Find the routers
        Router r1 = findRouterByRouterId(idR1);
        Router r2 = findRouterByRouterId(idR2);
        // Create the link and wire it to the routers
        Link link = new Link(r1, r2, cost);
        // Wire the routers to the link
        r1.addLink(link);
        r2.addLink(link);
        // Store the Link id locally
        linkList.add(link);

    }

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
