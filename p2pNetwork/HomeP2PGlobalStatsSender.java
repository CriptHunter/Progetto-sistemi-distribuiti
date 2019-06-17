package p2pNetwork;

import beans.Statistics;

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
        homep2p.sendGlobalStatToServer(globalStat);
        for(int homeId : localStats.keySet()) {
            homep2p.sendLocalStatToServer(localStats.get(homeId));
        }
    }
}
