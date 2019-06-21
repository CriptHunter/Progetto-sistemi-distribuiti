package p2pNetwork;

import java.io.IOException;
import java.net.*;

class HomeP2PServer extends Thread {

    private int port;
    private ServerSocket socket;

    public HomeP2PServer(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
    }

    public void run() {
        while(true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = socket.accept();
            } catch (IOException e) {
                System.out.println("errore su accept()");
            }
            HomeP2PServerThread serverThread = new HomeP2PServerThread(connectionSocket);
            serverThread.start();
        }
    }
}