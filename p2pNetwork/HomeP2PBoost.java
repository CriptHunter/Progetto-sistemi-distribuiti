package p2pNetwork;

import beans.Home;

import java.io.IOException;

public class HomeP2PBoost extends Thread {

    private Home h;
    private HomeP2P homep2p;

    public HomeP2PBoost(Home h) {
        this.h = h;
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        homep2p.addToBoostOkList(h);
        System.out.println("ricevuto OK da " + h.getId());
        if(homep2p.sameElements(homep2p.getBoostOkList(), homep2p.getHomesList())) {
            System.out.println("posso boostarmi");
            try {
                homep2p.requestBoost(false);
                homep2p.setBoost(true);
                Thread.sleep(10000);
                System.out.println("ho finito di boostarmi");
                homep2p.boostFinished();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("manca OK da qualche casa");
    }
}
