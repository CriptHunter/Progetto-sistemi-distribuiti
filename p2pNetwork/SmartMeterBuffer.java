package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Statistics;
import simulationSrc.Buffer;
import simulationSrc.Measurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmartMeterBuffer implements Buffer {

    private List<Measurement> measurementList;
    private int windowSize;
    private int overlapSize;
    private HomeP2P homep2p;

    private class AverageThread extends Thread {
        List<Measurement> todoAverage;
        public AverageThread(List<Measurement> todoAverage) {
            this.todoAverage = todoAverage;
        }

        private double average() {
            double average = 0;
            for(Measurement m : todoAverage)
                average = average + m.getValue();
            return average / todoAverage.size();
        }

        public void run() {
            HomeP2P homep2p = HomeP2P.getInstance();
            Statistics statistic = new Statistics(homep2p.getThisHome().getId(), average(), homep2p.makeTimestamp());
            Message message = new Message<Statistics>(Header.STAT, homep2p.makeTimestamp(), statistic);
            //aggiunge alla casa la nuova statistica locale
            homep2p.addStatistic(statistic);
            homep2p.getHomesLocalStats().put(homep2p.getThisHome().getId(), statistic);
            try {
                //invia la statistica a tutte le altre case
                HomeP2P.getInstance().broadCastMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public SmartMeterBuffer(int windowSize, int overlapSize) {
        this.windowSize = windowSize;
        this.overlapSize = overlapSize;
        measurementList = new ArrayList<>();
        homep2p.getInstance();
    }

    @Override
    public synchronized void addMeasurement(Measurement m) {
        measurementList.add(m);
        if(measurementList.size() == windowSize)
        {
            AverageThread at = new AverageThread(measurementList);
            at.start();
            flushBuffer();
        }
    }

    //svuota metà della finestra
    private void flushBuffer() {
        int n = overlapSize;
        while(n > 0)
        {
            measurementList.remove(0);
            --n;
        }
    }
}
