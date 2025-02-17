package p2pNetwork;

import Messages.Header;
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
import server.ConsoleColors;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static p2pNetwork.HomeP2PMain.simulator;

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
    //rappresenta questa casa
    private Home thisHome;
    //se la casa sta uscendo dalla rete o no
    private Status status;
    private Home coordinator = null;

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

    //la casa comunica al server che è uscita dalla rete
    public boolean SignOutFromServer()
    {
        WebResource webResource = client.resource(serverURL + "home/remove/" + thisHome.getId());
        ClientResponse response = webResource.post(ClientResponse.class);
        if(response.getStatus() == Response.Status.OK.getStatusCode())
            return true;
        else
            return false;
    }

    //comunica alle altre case l'uscita dalla rete
    public void leaveNetwork() throws IOException {
        System.out.println("provo ad uscire dalla rete");
        setStatus(Status.EXITING);
        SignOutFromServer();
        //se è l'unica casa nella rete esce, altrimenti invia un messaggio di uscita a tutte le case
        if(getHomesList().size() == 0)
            System.exit(0);
        else {
            Message netExitMessage = new Message<Home>(Header.NET_EXIT, makeTimestamp(), thisHome);
            Message boostOkMessage = new Message<Home>(Header.BOOST_OK, makeTimestamp(), thisHome);
            //prima di uscire da l'ok a tutte le case in attesa di boost
            broadCastMessageCustomHomesList(boostOkMessage, getBoostWaitingList());
            //dice a tutte le case di essere uscita dalla rete
            broadCastMessage(netExitMessage);
        }
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

    public synchronized Status getStatus() {
        return this.status;
    }

    public synchronized void  setStatus(Status s)
    {
        this.status = s;
    }

    public synchronized void addHome(Home h) {
        homesLocalStats.put(h.getId(), null);
        //possibile cambio di coordinatore
        homesList.add(h);
        setCoordinator();
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
        //possibile cambio di coordinatore
        setCoordinator();
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
            unicastMessage(m, h);
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
        m.setTimestamp(makeTimestamp());
        HomeP2PClient client = new HomeP2PClient(h.getPort(), m);
        client.start();
    }

    //ritorna i secondi passati dal 1970
    public long makeTimestamp() {
        return java.time.Instant.now().getEpochSecond();

    }

    //ritorna true se la casa è coordinatore
    public boolean isCoordinator() {
        if(this.coordinator.equals(thisHome))
            return true;
        else
            return false;
    }

    //la casa cerca chi è il nuovo coordinatore
    public void setCoordinator() {
        Home tempCoord = thisHome;
        for(Home h : getHomesList())
            if(h.getId() > tempCoord.getId()) {
                tempCoord = h;
            }
        this.coordinator = tempCoord;
    }

    public Home getCoordinator() {
        return this.coordinator;
    }

    //////////////////////////////////////////RICART AGRAWALA///////////////////////////////////////////////////////
    private boolean requestingBoost = false;
    private boolean boosting = false;
    private long boostRequestTimestamp = Long.MAX_VALUE; //timestamp della richiesta di questa casa
    private List<Home> boostWaitingList = new ArrayList<>(); //case in attesa di OK da questa casa
    private List<Home> boostOkList = new ArrayList<>();  //case che hanno dato OK a questa casa
    private List<Home> homesListAtBoostRequest = new ArrayList<>(); //lista di case nel momento in cui chiedo il boost

    public synchronized boolean isRequestingBoost() {
        //System.out.println("<<<<< isRequestingBoost <<<<<<" + LocalTime.now(ZoneId.systemDefault()));
        return requestingBoost;
    }

    public synchronized void setBoost(boolean boosting) {
        this.boosting = boosting;
    }

    public synchronized boolean isBoosting() {
        //System.out.println("<<<<< isBoosting <<<<<<");
        return boosting;
    }

    public synchronized void requestBoost(boolean requestingBoost) throws IOException {
        //System.out.println("<<<<< requestBoost <<<<<<" + LocalTime.now(ZoneId.systemDefault()));
        if(requestingBoost) {
            //per sicurezza resetto di nuovo le liste
            setBoost(false);
            boostOkList.clear();
            boostWaitingList.clear();
            homesListAtBoostRequest.clear();
            this.requestingBoost = true;
            //setto la lista di case da cui devo ricevere un OK
            homesListAtBoostRequest = getHomesList();
            //se ci sono più di due case nella rete devo fare richiesta di boost
            if (homesListAtBoostRequest.size() >= 2) {
                this.boostRequestTimestamp = makeTimestamp();
                Message m = new Message<Home>(Header.BOOST_REQUEST, makeTimestamp(), thisHome);
                broadCastMessage(m);
            }
            else {
                new HomeP2PBoostStarter().start();
            }
        }
        else
            this.requestingBoost = false;
    }

    public synchronized void addToBoostWaitingList(Home h) {
        boostWaitingList.add(h);
    }

    public synchronized List<Home> getBoostWaitingList() {
        List<Home> l = new ArrayList<>(boostWaitingList);
        return l;
    }

    public synchronized void addToBoostOkList(Home h) {
        //System.out.println("<<<<< addToOkBoostList <<<<<<" + LocalTime.now(ZoneId.systemDefault()));
        boostOkList.add(h);
    }

    public synchronized List<Home> getBoostOkList() {
        List<Home> l = new ArrayList<>(boostOkList);
        return l;
    }

    public synchronized List<Home> getHomesListAtBoostRequest() {
        List<Home> l = new ArrayList<>(homesListAtBoostRequest);
        return l;
    }

    public void boostRequestTest(Home otherHome, long otherTimestamp) throws IOException {
        //System.out.println("<<<<< boostRequestTest <<<<<<" + LocalTime.now(ZoneId.systemDefault()));
        int otherId = otherHome.getId();
        //se la casa vuole boostarsi
        if(isBoosting()) {
            //System.out.println("mi sto boostando, aggiungo " + otherHome.getId() + " alla coda");
            addToBoostWaitingList(otherHome);
        }
        else if(isRequestingBoost()) {
            if(boostRequestTimestamp > otherTimestamp) /*l'altra casa ha la priorità */{
                //System.out.println("sto richiedendo il boost e la casa " + otherId + " ha la priorità sul boost");
                Message m = new Message<Home>(Header.BOOST_OK, makeTimestamp(), thisHome);
                unicastMessage(m, otherHome);
            }
            else if (boostRequestTimestamp < otherTimestamp) /*questa casa ha la priorità*/ {
                //System.out.println("sto richiedendo il boost e ho la priorità sulla casa " + otherId + " per il boost");
                addToBoostWaitingList(otherHome);
            }
            else if(boostRequestTimestamp == otherTimestamp) /*il timestamp è uguale controllo gli id */ {
                //System.out.println("sto richiedendo il boost e Il timestamp della casa " + otherId + " è uguale al mio per il boost");
                if(thisHome.getId() > otherHome.getId()) {
                    //System.out.println("Il mio Id è maggiore e quindi vinco");
                    addToBoostWaitingList(otherHome);
                }
                else {
                    //System.out.println("Il mio Id è minore e quindi perdo");
                    Message m = new Message<Home>(Header.BOOST_OK, makeTimestamp(), thisHome);
                    unicastMessage(m, otherHome);
                }
            }
        }
        else {
            Message m = new Message<Home>(Header.BOOST_OK, makeTimestamp(), thisHome);
            unicastMessage(m, otherHome);
        }
    }

    public synchronized boolean canBoost() {
        //System.out.println("<<<<< canBoost <<<<<< " + LocalTime.now(ZoneId.systemDefault()));
        if(isBoosting())
            return false;
        if(getBoostOkList().size() == getHomesListAtBoostRequest().size()) {
            //System.out.println("Ricevuto OK da tutte le case ---> true");
            return true;
        }
        else if (getBoostOkList().size() == getHomesListAtBoostRequest().size() -1) {
            //System.out.println("Ricevuto OK da tutte le case meno una ---> true");
            return true;
        }
        else {
            //System.out.println("Manca OK di almeno 2 case ---> false");
            return false;
        }
    }

    public synchronized void startBoost() throws IOException, InterruptedException {
        if(isBoosting())
            return;
        System.out.print(ConsoleColors.GREEN_BOLD);
        System.out.println("<<<<< startBoost <<<<<< " + LocalTime.now(ZoneId.systemDefault()));
        System.out.print(ConsoleColors.RESET);
        requestBoost(false);
        setBoost(true);
        simulator.boost();
    }

    public void endBoost() throws IOException {
        System.out.print(ConsoleColors.GREEN_BOLD);
        System.out.println("<<<<< endBoost <<<<<< " + LocalTime.now(ZoneId.systemDefault()));
        System.out.print(ConsoleColors.RESET);
        List<Home> l = getBoostWaitingList();
        Message m = new Message(Header.BOOST_OK, makeTimestamp(), thisHome);
        broadCastMessageCustomHomesList(m, l);
        setBoost(false);
        boostOkList.clear();
        boostWaitingList.clear();
        homesListAtBoostRequest.clear();
    }
}
