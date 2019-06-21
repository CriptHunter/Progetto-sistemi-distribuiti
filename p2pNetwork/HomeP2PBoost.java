package p2pNetwork;

import beans.Home;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;

public class HomeP2PBoost extends Thread {

    private HomeP2P homep2p;

    public HomeP2PBoost() {
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        if(homep2p.canBoost() && !homep2p.isBoosting()) {
            try {
                homep2p.startBoost();
                Thread.sleep(10000);
                homep2p.endBoost();
            } catch (InterruptedException | IOException e) {
                System.out.println("errore nel processo di boosting");
                e.printStackTrace();
            }
        }
    }
}
