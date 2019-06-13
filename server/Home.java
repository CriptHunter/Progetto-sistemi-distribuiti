package server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Home {
    private int id;
    private String ip;
    private int port;

    public Home(){}

    public Home(int id, String ip, int port)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

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