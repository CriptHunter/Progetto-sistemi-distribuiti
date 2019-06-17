package p2pNetwork;

import Messages.Message;
import com.google.gson.Gson;
import com.sun.media.sound.InvalidFormatException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class HomeP2PClient extends Thread {
    private int port;
    private Socket socket;
    private DataOutputStream outToServer;
    private Message message;
    private Gson gson;
    private boolean running;

    public HomeP2PClient(int port, Message message) throws IOException {
        this.port = port;
        this.message = message;
        this.running = true;
        gson = new Gson();
        try {
            socket = new Socket("localhost", port);
            outToServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (ConnectException e)
        {
            System.out.println("nessuna server socket sulla porta " + port);
            running = false;
        }
    }

    public void run() {
        //se non è running qualcosa è andato storto nella creazione della socket
        if(!running)
            return;

        try {
            Gson gson = new Gson();
            outToServer.writeBytes(gson.toJson(message) + "\n");
        } catch (IOException e) {
            System.out.println("impossibile comunicare con il server");
        }
    }
}
