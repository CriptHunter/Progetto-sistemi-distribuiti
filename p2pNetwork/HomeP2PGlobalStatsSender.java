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
        //invio la statistica globale alle altre case
        Message globalStatMessage = new Message<Statistics>(Header.GLOBAL_STAT, homep2p.makeTimestamp(), globalStat);
        try {
            homep2p.broadCastMessage(globalStatMessage);
        } catch (IOException e) {
            System.out.println("problemi nell'invio broadcast delle statistiche globali");
        }
        //invio le statistiche locali alle altre case
        for(int homeId : localStats.keySet()) {
            try {
                Message localStatMessage = new Message<Statistics>(Header.LOCAL_STAT_COORD, homep2p.makeTimestamp(), localStats.get(homeId));
                homep2p.broadCastMessage(localStatMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //invia statistiche globali e locali al server
        homep2p.sendGlobalStatToServer(globalStat);
        //invia statistiche locali al server
        for(int homeId : localStats.keySet()) {
            homep2p.sendLocalStatToServer(localStats.get(homeId));
        }
    }
}
