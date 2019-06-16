package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Home;
import simulationSrc.SmartMeterSimulator;

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
        if(!homep2p.SignOnServer())
            return;

        //avvia il server che crea server socket
        HomeP2PServer server = new HomeP2PServer(port);
        server.start();

        //dice a tutte le case della rete di essere entrata
        Message netEntranceMessage = new Message<Home>(Header.NET_ENTRANCE, homep2p.makeTimestamp(), new Home(id, ip, port));
        homep2p.broadCastMessage(netEntranceMessage);
        //svuoto la lista di case perché prima di aggiungerle davvero aspetto risposta
        homep2p.getHomesList().clear();

        //avvia il simulatore
        SmartMeterBuffer buffer = new SmartMeterBuffer(24, 12);
        SmartMeterSimulator simulator = new SmartMeterSimulator(buffer);
        simulator.start();

        //avvia il calcolatore di stat globali
        new HomeP2PGlobalStatsMaker().start();

        new HomeP2PPrinter().start();

        while(true) {
            Message netExitMessage = new Message<Home>(Header.NET_EXIT, homep2p.makeTimestamp(), new Home(id, ip, port));
            System.out.println("Premi invio per rimuovere la casa dalla rete e terminare il processo");
            System.in.read();
            homep2p.setStatus(Status.EXITING);
            homep2p.SignOutFromServer();
            //se è l'unica casa nella rete esce, altrimenti invia un messaggio di uscita a tutte le case
            if(homep2p.getHomesList().size() == 0)
                System.exit(0);
            else
                homep2p.broadCastMessage(netExitMessage);
        }


    }
}
