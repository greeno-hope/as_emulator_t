package uk.ac.hope.csc.net.emulator;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;

public class Main {

    public static void main(String[] args) {

        // Setup
        AutonomousSystem as = new AutonomousSystem(5);

        // Add links
        as.addLink(0,1,10);
        as.addLink(0,2,10);
        as.addLink(1,3,10);
        as.addLink(2,4,10);

        
        Datagram d = new Datagram(0, Router.BROADCAST);
        as.findRouterByRouterId(0).sendDatagram(d);


        while(true) {
            try {
                Thread.sleep(10);
                as.handleTick();
            } catch (Exception e) {

            }
        }

    }

}
