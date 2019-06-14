package p2pNetwork;

import Messages.Header;
import Messages.Message;
import server.Home;
import java.io.IOException;



public class HomeP2PMain {
    public static void main(String argv[]) throws IOException, InterruptedException {
        //parametri passati dal main
        int id = Integer.parseInt(argv[0]);
        String ip = argv[1];
        int port = Integer.parseInt(argv[2]);
        String address = argv[3];
        //registro la casa sul server REST, se non riesce termina il processo
        HomeP2P homep2p = HomeP2P.getInstance();
        homep2p.init(id, ip, port, address);
        if(!HomeP2P.getInstance().SignOnServer())
            return;

        //avvia il server che crea server socket
        HomeP2PServer server = new HomeP2PServer(port);
        server.start();

        //dice a tutte le case della rete di essere entrata
        Message netEntranceMessage = new Message<Home>(Header.NET_ENTRANCE, 1, new Home(id, ip, port));
        homep2p.broadCastMessage(netEntranceMessage);

        //avvia il thread che si occupa della stampa dei messaggi
        HomeP2PPrinter printer = new HomeP2PPrinter();
        printer.start();

        while(true)
        {   Message netExitMessage = new Message<Home>(Header.NET_EXIT, 1, new Home(id, ip, port));
            System.out.println("Premi invio per rimuovere la casa dalla rete e terminare il processo");
            System.in.read();
            homep2p.setStatus(Status.EXITING);
            homep2p.SignOutFromServer();
            homep2p.broadCastMessage(netExitMessage);
        }
    }
}
