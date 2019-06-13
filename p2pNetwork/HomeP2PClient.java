package p2pNetwork;

import Messages.Message;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HomeP2PClient extends Thread {
    private int port;
    private Socket socket;
    private DataOutputStream outToServer;
    private Message message;
    Gson gson;

    public HomeP2PClient(int port, Message message) throws IOException {
        this.port = port;
        this.message = message;
        gson = new Gson();
        socket = new Socket("localhost", port);
        outToServer = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        try {
            Gson gson = new Gson();
            outToServer.writeBytes(gson.toJson(message) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
