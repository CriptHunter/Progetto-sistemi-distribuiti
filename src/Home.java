package src;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Home {

    private int id;
    private String ip;
    private int port;

    public Home(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}