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
import java.util.Iterator;
import java.util.List;

public class HomeP2P {
    private int id;
    private String ip;
    private int port;
    private Client client;
    private String serverURL;
    private List<Home> homesList;
    private static HomeP2P instance = null;

    private HomeP2P() {
    }

    //setta le variabili della casa
    public void init(int id, String ip, int port, String serverURL)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.serverURL = serverURL;
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
        Home thisHome = new Home();
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

        //rimuove se stessa dalla lista di case
        removeHome(thisHome);
        return true;
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

    public void broadCastMessage(Message m) throws IOException {
        List<Home> homesList = getHomesList();
        for(Home h : homesList) {
            HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
            client.start();
        }
    }
}
