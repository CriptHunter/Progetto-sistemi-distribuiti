package p2pNetwork;

import Messages.Message;
import beans.Statistics;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import beans.Home;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class HomeP2P {
    private int id;
    private String ip;
    private int port;
    private Client client;
    private String serverURL;
    //lista delle altre case nella rete
    private List<Home> homesList;
    //lista delle case che hanno dato l'ack per far uscire questa casa dalla rete
    private List<Home> homesListExitAck;
    //mappa delle ultime statistiche globali di ogni casa
    HashMap<Integer, Statistics> homesLocalStats = new HashMap<>();
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
        homesLocalStats = new HashMap<>();
        //homesLocalStats.put(id, null);
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

    public boolean sendGlobalStatToServer(Statistics globalStat) {
        WebResource webResource = client.resource(serverURL + "stats/global/add");
        ClientResponse response = webResource.accept("application/xml").post(ClientResponse.class, globalStat);
        if(response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("problema nell'invio della statistica globale");
            return false;
        }
        return true;
    }

    public boolean sendLocalStatToServer(Statistics localStat)
    {
        WebResource webResource = client.resource(serverURL + "stats/local/add");
        ClientResponse response = webResource.accept("application/xml").post(ClientResponse.class, localStat);
        if(response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("problema nell'invio della statistica locale");
            return false;
        }
        return true;
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
        homesLocalStats.put(h.getId(), null);
        return homesList.add(h);
    }

    public synchronized void removeHome(Home h) {
        //rimuove la casa dall'hashmap
        homesLocalStats.remove(h.getId());

        //rimuove la casa dalla lista di case
        for(Iterator<Home> itr = homesList.iterator(); itr.hasNext();){
            Home iter = itr.next();
            if(iter.getId() == h.getId()){
                itr.remove();
            }
        }
    }

    public synchronized List<Home> getHomesList() {
        List<Home> homesListCopy = new ArrayList<>(homesList);
        return homesListCopy;
    }

    public synchronized List<Home> getHomesListExitAck()
    {
        List<Home> homesListCopy = new ArrayList<>(homesList);
        return homesListExitAck;
    }

    public synchronized void clearHomesList() {
        homesList.clear();
    }

    public synchronized void addStatistic(Statistics s) {
        homesLocalStats.put(s.getHomeId(), s);
    }

    public synchronized void flushLocalStats()
    {
        for(int homeId : homesLocalStats.keySet())
            homesLocalStats.put(homeId, null);
    }

    public synchronized HashMap<Integer, Statistics> getHomesLocalStats() {
        HashMap<Integer, Statistics> homesLocalStatsCopy = new HashMap<>(homesLocalStats);
        return homesLocalStatsCopy;
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
            m.setTimestamp(makeTimestamp());
            HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
            client.start();
        }
    }

    public void  broadCastMessageCustomHomesList(Message m, List<Home> homesList) throws IOException {
        for(Home h : homesList) {
            m.setTimestamp(makeTimestamp());
            HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
            client.start();
        }
    }

    public void unicastMessage(Message m, Home h) throws IOException {
        HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
        client.start();
    }

    /*public long makeTimestamp() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        return now.toSecondOfDay();
    }*/

    //ritorna i secondi passati dal 1970
    public long makeTimestamp() {
        return java.time.Instant.now().getEpochSecond();

    }

    public boolean isCoordinator()
    {
        for(Home h : getHomesList())
            if(h.getId() > getThisHome().getId()) {
                return false;
            }
        return true;
    }

        public Home getCoordinator() {
        Home coordinator = thisHome;
        for(Home h : getHomesList())
            if(h.getId() > coordinator.getId()) {
                coordinator = h;
            }
        return coordinator;
    }
}
