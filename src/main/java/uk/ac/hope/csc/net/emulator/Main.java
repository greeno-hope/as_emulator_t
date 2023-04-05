package uk.ac.hope.csc.net.emulator;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;
import uk.ac.hope.csc.net.emulator.as.component.Router;
import uk.ac.hope.csc.net.emulator.as.packet.Datagram;
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
