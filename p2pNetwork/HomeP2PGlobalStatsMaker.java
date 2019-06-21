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
    private long globalStatTimestamp;
    private boolean canMakeGlobalStat;
    private HomeP2P homep2p;

    public HomeP2PGlobalStatsMaker() {
        globalStatValue = 0;
        globalStatTimestamp = 0;
        canMakeGlobalStat = true;
        homep2p = HomeP2P.getInstance();
    }

    public void run() {

        boolean isCoordinator = homep2p.isCoordinator();
        if(isCoordinator) {
            homesStat = homep2p.getHomesLocalStats();
            //se trova anche un solo valore nullo nell'hashmap la statistica globale non è calcolata
            for (int homeId : homesStat.keySet()) {
                if (homesStat.get(homeId) == null) {
                    canMakeGlobalStat = false;
                    break;
                } else {
                    globalStatValue = globalStatValue + homesStat.get(homeId).getValue();
                    long timestamp = homesStat.get(homeId).getTimestamp();
                    globalStatTimestamp = (globalStatTimestamp > timestamp) ? globalStatTimestamp : timestamp;
                }
            }

            if (canMakeGlobalStat && globalStatValue != 0) {
                homep2p.flushLocalStats();
                Statistics globalStat = new Statistics(-1, globalStatValue, globalStatTimestamp);
                System.out.println("------------------------------------------------------------------------");
                System.out.println("Statistica globale prodotta: " + globalStat.getValue() + " | " + globalStat.getTimestamp());
                System.out.println("Misurazioni che la compongono: ");
                //stampo le ultime statistiche di ogni casa
                for (int homeId : homesStat.keySet()) {
                    if (homesStat.get(homeId) != null)
                        System.out.println(homesStat.get(homeId));
                }
                new HomeP2PGlobalStatsSender(globalStat, new HashMap<>(homesStat)).start();
                Message m = new Message<Statistics>(Header.GLOBAL_STAT, homep2p.makeTimestamp(), globalStat);
                try {
                    homep2p.broadCastMessage(m);
                } catch (IOException e) {
                    System.out.println("problemi nell'invio di messaggi broadcast");
                }
            }
        }
    }
}

