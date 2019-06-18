package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Statistics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import beans.Home;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
public class HomeP2PServerThread extends Thread {
    private Socket connectionSocket = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private Gson gson;
    private HomeP2P homep2p;

    public HomeP2PServerThread(Socket s) {
        connectionSocket = s;
        gson = new Gson();
        homep2p = HomeP2P.getInstance();
        try{
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String clientMessage = inFromClient.readLine();
            //leggo l'header del messaggio per capire che tipo di oggetto c'è in content
            Message genericMessage = gson.fromJson(clientMessage, Message.class);
            //ora che so quale content c'è faccio un if diverso per ogni tipo di contenuto
            if(genericMessage.getHeader() == Header.NET_ENTRANCE || genericMessage.getHeader() == Header.NET_ENTRANCE_ACK ||
                    genericMessage.getHeader() == Header.NET_EXIT || genericMessage.getHeader() == Header.NET_EXIT_ACK ||
                    genericMessage.getHeader() == Header.BOOST_REQUEST || genericMessage.getHeader() == Header.BOOST_OK )
            {
                Type typeToken = new TypeToken<Message<Home>>() {}.getType();
                Message m = gson.fromJson(clientMessage, typeToken);
                Home h = (Home)m.getContent();
                //aggiunge una nuova casa nella rete
                if (m.getHeader() == Header.NET_ENTRANCE && homep2p.getStatus() != Status.EXITING) {
                    homep2p.addHome(h);
                    Message netEntranceAck = new Message<Home>(Header.NET_ENTRANCE_ACK, homep2p.makeTimestamp(), homep2p.getThisHome());
                    homep2p.unicastMessage(netEntranceAck, h);
                }
                //una casa appena aggiunta aggiunge le altre case della rete solo dopo un ack
                else if(m.getHeader() == Header.NET_ENTRANCE_ACK && homep2p.getStatus() != Status.EXITING) {
                    System.out.println("ricevuto net_entrance_ack da " + h.getId());
                    homep2p.addHome(h);
                }
                //rimuove la casa uscita dalla rete
                else if (m.getHeader() == Header.NET_EXIT) {
                    //rispondo alla casa che sta uscendo dalla rete con un ack
                    Message netExitAck = new Message<Home>(Header.NET_EXIT_ACK, homep2p.makeTimestamp(), homep2p.getThisHome());
                    homep2p.unicastMessage(netExitAck, h);
                    homep2p.removeHome(h);
                }
                else if(m.getHeader() == Header.BOOST_REQUEST && homep2p.getStatus() != Status.EXITING) {
                    //se gli arriva una richiesta di boost fa il test per vedere se l'altra casa può boostarsi
                    homep2p.boostRequestTest(h, m.getTimestamp());
                }
                else if(m.getHeader() == Header.BOOST_OK && homep2p.getStatus() != Status.EXITING) {
                    //se gli arriva un OK prova a vedere se può boostarsi
                    new HomeP2PBoost(h).start();
                }
                //se sta uscendo dalla rete aspetta gli exit ack
                else if (m.getHeader() == Header.NET_EXIT_ACK && homep2p.getStatus() == Status.EXITING)
                {
                    System.out.println("ricevuto net_exit_ack da: " + h.getId());
                    homep2p.getHomesListExitAck().add(h);
                    if(homep2p.sameElements(homep2p.getHomesList(), homep2p.getHomesListExitAck()))
                        System.exit(0);
                }
            }
            //se sta ricevendo una statistica la aggiunge alla sua hashmap
            else if (genericMessage.getHeader() == Header.LOCAL_STAT || genericMessage.getHeader() == Header.GLOBAL_STAT)
            {
                Type typeToken = new TypeToken<Message<Statistics>>() {}.getType();
                Message m = gson.fromJson(clientMessage, typeToken);
                Statistics s = (Statistics)m.getContent();
                if(genericMessage.getHeader() == Header.LOCAL_STAT && homep2p.getStatus() != Status.EXITING) {
                    homep2p.addStatistic(s);
                    new HomeP2PGlobalStatsMaker().start();
                }
                else if (genericMessage.getHeader() == Header.GLOBAL_STAT && homep2p.getStatus() != Status.EXITING)
                    System.out.println("Statistica globale ricevuta: " + s.getValue() + " | " + s.getTimestamp());
            }
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}