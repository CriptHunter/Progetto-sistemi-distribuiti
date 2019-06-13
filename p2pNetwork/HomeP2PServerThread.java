package p2pNetwork;

import Messages.Header;
import Messages.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import server.Home;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
public class HomeP2PServerThread extends Thread {
    private Socket connectionSocket = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    Gson gson;

    public HomeP2PServerThread(Socket s) {
        connectionSocket = s;
        gson = new Gson();
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
            if(genericMessage.getHeader() == Header.NET_ENTRANCE || genericMessage.getHeader() == Header.NET_EXIT)
            {
                Type typeToken = new TypeToken<Message<Home>>() {}.getType();
                Message m = gson.fromJson(clientMessage, typeToken);
                Home h = (Home)m.getContent();
                if(m.getHeader() == Header.NET_ENTRANCE)
                    HomeP2P.getInstance().addHome(h);
                else
                    HomeP2P.getInstance().removeHome(h);
            }
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}