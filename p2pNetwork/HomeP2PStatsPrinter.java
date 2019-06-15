package p2pNetwork;

import beans.Statistics;

import java.util.HashMap;

public class HomeP2PStatsPrinter extends Thread {

    private HashMap<Integer, Statistics> homesStat;

    public void run() {
        while(true) {
            homesStat = HomeP2P.getInstance().getHomesLocalStats();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("---------------------------------------------------------");
            //stampo le ultime statistiche di ogni casa
            for (int homeId : homesStat.keySet())
                System.out.println("id casa: " + homeId + "| valore: " + homesStat.get(homeId).getValue() + " | timestamp: " + homesStat.get(homeId).getTimestamp());
        }
    }
}