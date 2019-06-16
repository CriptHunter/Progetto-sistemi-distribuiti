package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Home;
import beans.Statistics;

import java.io.IOException;
import java.util.HashMap;

public class HomeP2PGlobalStatsMaker extends Thread {

    private HashMap<Integer, Statistics> homesStat;
    private double globalStatValue;
    private boolean canMakeGlobalStat;
    private HomeP2P homep2p;

    public HomeP2PGlobalStatsMaker() {
        globalStatValue = 0;
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        while(true)
        {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean isCoordinator = true;
            //controllo se è il coordinatore
            for(Home h : homep2p.getHomesList())
                if(h.getId() > homep2p.getThisHome().getId()) {
                    isCoordinator = false;
                    break;
                }
            if(isCoordinator) {
                //se è il coordinatore
                globalStatValue = 0;
                canMakeGlobalStat = true;
                homesStat = homep2p.getHomesLocalStats();

                //se trova anche un solo valore nullo nell'hashmap la statistica globale non è calcolata
                for (int homeId : homesStat.keySet()) {
                    if (homesStat.get(homeId) == null) {
                        canMakeGlobalStat = false;
                        break;
                    } else
                        globalStatValue = globalStatValue + homesStat.get(homeId).getValue();
                }

                if (canMakeGlobalStat && globalStatValue != 0) {
                    System.out.println("--------------------------------------------------");
                    System.out.println("La global stat è " + globalStatValue);
                    System.out.println("Misurazioni che la compongono: ");
                    //stampo le ultime statistiche di ogni casa
                    for (int homeId : homep2p.getHomesLocalStats().keySet()) {
                        if (homesStat.get(homeId) != null)
                            System.out.println("id casa: " + homeId + "| valore: " + homesStat.get(homeId).getValue() + " | timestamp: " + homesStat.get(homeId).getTimestamp());
                    }
                    homep2p.flushLocalStats();
                }
            }
        }
    }
}

