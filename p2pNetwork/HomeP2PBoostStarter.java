package p2pNetwork;

import java.io.IOException;

public class HomeP2PBoostStarter extends Thread {

    private HomeP2P homep2p;

    public HomeP2PBoostStarter() {
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        if(homep2p.canBoost() && !homep2p.isBoosting()) {
            try {
                homep2p.startBoost();
                Thread.sleep(5000);
                homep2p.endBoost();
            } catch (InterruptedException | IOException e) {
                System.out.println("errore nel processo di boosting");
                e.printStackTrace();
            }
        }
    }
}
