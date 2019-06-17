package p2pNetwork;

import beans.Statistics;

public class HomeP2PGlobalStatsSender extends Thread {
    private Statistics globalStat;
    private Statistics[] localStats;
    private HomeP2P homep2p;

    public HomeP2PGlobalStatsSender(Statistics globalStat, Statistics[] localStats) {
        this.globalStat = globalStat;
        this.localStats = localStats;
        homep2p = HomeP2P.getInstance();
    }

    public void run() {
        homep2p.sendGlobalStatToServer(globalStat);
    }
}
