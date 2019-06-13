package p2pNetwork;

import server.Home;

public class HomeP2PPrinter extends Thread {
    public void run() {
        while(true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Lista case nella rete:");
            for(Home h : HomeP2P.getInstance().getHomesList())
                System.out.println("id: " + h.getId() + " porta: " + h.getPort());
            System.out.println("------------------------------------------");
        }
    }

}
