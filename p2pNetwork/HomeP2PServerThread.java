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
            if(genericMessage.getHeader() == Header.NET_ENTRANCE || genericMessage.getHeader() == Header.NET_EXIT || genericMessage.getHeader() == Header.NET_EXIT_ACK)
            {
                Type typeToken = new TypeToken<Message<Home>>() {}.getType();
                Message m = gson.fromJson(clientMessage, typeToken);
                Home h = (Home)m.getContent();
                //se la casa è in uno stato normale riceve messaggi di net entrance e net exit, scarta i net exit ack
                if(homep2p.getStatus() == Status.WORKING) {
                    //aggiunge una nuova casa nella rete e invia l'ultima statistica per allinearla
                    if (m.getHeader() == Header.NET_ENTRANCE) {
                        homep2p.addHome(h);
                        Message statMessage = new Message<Statistics>(Header.STAT, homep2p.makeTimestamp(), homep2p.getHomesLocalStats().get(homep2p.getThisHome().getId()));
                        homep2p.unicastMessage(statMessage, h);
                    }
                    //rimuove la casa uscita dalla rete
                    else if (m.getHeader() == Header.NET_EXIT) {
                        //rispondo alla casa che sta uscendo dalla rete con un ack
                        Message netExitAck = new Message<Home>(Header.NET_EXIT_ACK, 1, homep2p.getThisHome());
                        homep2p.unicastMessage(netExitAck, h);
                        homep2p.removeHome(h);
                    }
                }
                //se sta uscendo dalla rete aspetta gli exit ack
                else if (homep2p.getStatus() == Status.EXITING || m.getHeader() == Header.NET_EXIT_ACK)
                {
                    System.out.print("ricevuto ack da: " + h.getId() + ", ");
                    homep2p.getHomesListExitAck().add(h);
                    if(homep2p.sameElements(homep2p.getHomesList(), homep2p.getHomesListExitAck()))
                        System.exit(0);
                    else
                        System.out.println("manca l'ack di qualche casa");
                }
            }
            //se sta ricevendo una statistica
            else if (genericMessage.getHeader() == Header.STAT)
            {
                Type typeToken = new TypeToken<Message<Statistics>>() {}.getType();
                Message m = gson.fromJson(clientMessage, typeToken);
                Statistics s = (Statistics)m.getContent();
                homep2p.addStatistic(s);
            }
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}