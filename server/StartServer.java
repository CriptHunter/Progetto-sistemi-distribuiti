package server;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class StartServer {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create("http://" + HOST + ":" + PORT + "/");
        server.start();
        System.out.println("Server attivo!");
        System.out.println("Server attivo all'indirizzo: http://" + HOST + ":" + PORT);
        System.out.println("Premi invio per terminare");
        System.in.read();
        System.out.println("Arresto in corso...");
        server.stop(0);
        System.out.println("Server spento!");
        System.exit(0);
    }
}
