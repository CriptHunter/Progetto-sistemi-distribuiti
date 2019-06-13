package p2pNetwork;

import simulationSrc.Buffer;
import simulationSrc.Measurement;
import java.util.ArrayList;
import java.util.List;

public class SmartMeterBuffer implements Buffer {

    private List<Measurement> measurementList;

    public SmartMeterBuffer() {
        measurementList = new ArrayList<>();
    }

    @Override
    public void addMeasurement(Measurement m) {
        measurementList.add(m);
    }
}
