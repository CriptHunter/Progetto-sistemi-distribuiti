package p2pNetwork;

import Messages.Header;
import Messages.Message;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import server.Home;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeP2P {
    private int id;
    private String ip;
    private int port;
    private Client client;
    private String serverURL;
    private List<Home> homesList;
    //lista delle case che hanno dato l'ack per far uscire questa casa dalla rete
    private List<Home> homesListExitAck;
    private static HomeP2P instance = null;
    private Home thisHome;
    private Status status;

    private HomeP2P() {
    }

    //setta le variabili della casa
    public void init(int id, String ip, int port, String serverURL)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.serverURL = serverURL;
        homesListExitAck = new ArrayList<>();
        SetupServerConnection();
    }

    public synchronized static HomeP2P getInstance() {
        if (instance==null)
            instance = new HomeP2P();
        return instance;
    }

    private void SetupServerConnection()
    {
        ClientConfig clientConfig;
        clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        client = Client.create(clientConfig);
    }

    //ritorna true se la registrazione sul server va a buon fine
    //riempe la lista di case
    public boolean SignOnServer()
    {
        thisHome = new Home();
        thisHome.setId(this.id);
        thisHome.setIp(this.ip);
        thisHome.setPort(this.port);
        WebResource webResource = client.resource(serverURL + "home/add");
        //provo a registrarmi sul server
        try {
            ClientResponse response = webResource.accept("application/xml").post(ClientResponse.class, thisHome);
            if(response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                System.err.println("Esiste già una casa con lo stesso id");
                return false;
            }
            //se la registrazione va a buon fine ottengo la lista di case
            homesList = response.getEntity(new GenericType<List<Home>>(){});
        }

        catch(Exception e) {
            System.err.println("REST server irraggiungibile");
            return false;
        }

        status = Status.WORKING;
        //rimuove se stessa dalla lista di case
        removeHome(thisHome);
        return true;
    }

    public boolean SignOutFromServer()
    {
        WebResource webResource = client.resource(serverURL + "home/remove/" + thisHome.getId());
        ClientResponse response = webResource.post(ClientResponse.class);
        if(response.getStatus() == Response.Status.OK.getStatusCode())
            return true;
        else
            return false;
    }

    public Home getThisHome() {
        return thisHome;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status s)
    {
        this.status = s;
    }

    public synchronized boolean addHome(Home h) {
        return homesList.add(h);
    }

    public synchronized void removeHome(Home h) {
        for(Iterator<Home> itr = homesList.iterator(); itr.hasNext();){
            Home iter = itr.next();
            if(iter.getId() == h.getId()){
                itr.remove();
            }
        }
    }

    public synchronized List<Home> getHomesList() {
        return homesList;
    }

    public synchronized List<Home> getHomesListExitAck()
    {
        return homesListExitAck;
    }

    //controlla se due liste contengono gli stessi elementi
    public boolean sameElements(List firstList, List secondList)
    {
        if (firstList.size() != secondList.size())
            return false;
        for (int index = 0; index < firstList.size(); index++)
        {
            if (!secondList.contains(firstList.get(index)))
                return false;
        }
        return true;
    }

    public void broadCastMessage(Message m) throws IOException {
        List<Home> homesList = getHomesList();
        for(Home h : homesList) {
            HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
            client.start();
        }
    }

    public void unicastMessage(Message m, Home h) throws IOException {
        HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
        client.start();
    }
}
