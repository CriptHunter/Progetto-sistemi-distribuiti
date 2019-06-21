package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Statistics;

import java.io.IOException;
import java.util.HashMap;

public class HomeP2PGlobalStatsSender extends Thread {
    private Statistics globalStat;
    private HashMap<Integer,Statistics> localStats;
    private HomeP2P homep2p;

    public HomeP2PGlobalStatsSender(Statistics globalStat, HashMap<Integer, Statistics> localStats) {
        this.globalStat = globalStat;
        this.localStats = localStats;
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        Message m = new Message<Statistics>(Header.GLOBAL_STAT, homep2p.makeTimestamp(), globalStat);
        try {
            homep2p.broadCastMessage(m);
        } catch (IOException e) {
            System.out.println("problemi nell'invio broadcast delle statistiche globali");
        }

        //invia statistiche globali e locali al server
        homep2p.sendGlobalStatToServer(globalStat);
        //invia statistiche locali al server
        for(int homeId : localStats.keySet()) {
            homep2p.sendLocalStatToServer(localStats.get(homeId));
        }
    }
}
