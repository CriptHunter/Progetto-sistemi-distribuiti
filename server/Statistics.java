package server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistics {
    private int homeId;
    private double value;
    private long timestamp;

    public Statistics() {

    }

    public int getHomeId() {
        return homeId;
    }

    public void setHomeId(int homeId) {
        this.homeId = homeId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
