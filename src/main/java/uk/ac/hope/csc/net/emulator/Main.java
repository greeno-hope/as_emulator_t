package uk.ac.hope.csc.net.emulator;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

public class Main {

    public static void main(String[] args) {

        // Setup
        AutonomousSystem as = new AutonomousSystem(3);

        // Add 1 link
        as.addLink(0, 1, 20);
        as.addLink(1,2,20);

        Router sourceRouter = as.findRouterByRouterId(0);
        RoutingTable rt = new RoutingTable();
        rt.setPath(1, 1);
        rt.setPath(2, 1);

        Router midRouter = as.findRouterByRouterId(1);
        RoutingTable rt2 = new RoutingTable();
        rt2.setPath(2, 2);

        Datagram d = new Datagram(0, 2);
        sourceRouter.setRoutingTable(rt);
        midRouter.setRoutingTable(rt2);
        sourceRouter.sendDatagram(d);

        while(true) {
            try {
                Thread.sleep(10);
                as.handleTick();
            } catch (Exception e) {

            }
        }

    }

}
