package uk.ac.hope.csc.net.emulator;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.component.RoutingTable;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;
import uk.ac.hope.csc.net.emulator.as.sdn.RoutingTableBuilder;
import uk.ac.hope.csc.net.emulator.utils.FileUtils;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        // Setup
        AutonomousSystem as = new AutonomousSystem();

        // Add links
        try {
            as.loadNetworkFromCsv("./files/net1.csv");
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            System.exit(1);
        }

        // Build routing tables (SDN style)
        for(Router r : as.getRouterList()) {
            RoutingTable rt = RoutingTableBuilder.buildWithLinkState(as, r.getId());
            r.setRoutingTable(rt);
        }


        Datagram d = new Datagram(3, 12);
        as.findRouterByRouterId(3).sendDatagram(d);

        while(true) {
            try {
                Thread.sleep(10);
                as.handleTick();
            } catch (Exception e) {

            }
        }

    }

}
