package p2pNetwork;

import Messages.Header;
import Messages.Message;
import beans.Home;
import simulationSrc.SmartMeterSimulator;

import java.io.IOException;
import java.util.List;

public class HomeP2PMain {

    public static SmartMeterSimulator simulator;

    public static void main(String argv[]) throws IOException, InterruptedException {
        //parametri passati dal main
        int id = Integer.parseInt(argv[0]);
        String ip = argv[1];
        int port = Integer.parseInt(argv[2]);
        String address = argv[3];

        //avvia il server che crea server socket
        HomeP2PServer server = new HomeP2PServer(port);
        server.start();
        System.out.println("Server avviato");
        //registro la casa sul server REST, se non riesce termina il processo
        HomeP2P homep2p = HomeP2P.getInstance();
        homep2p.init(id, ip, port, address);
        if(!homep2p.SignOnServer())
            return;
        System.out.println("Registrazione sul server REST completata");
        List<Home> tempHomesList = homep2p.getHomesList();
        //svuoto la lista di case perchè prima di aggiungerle aspetto un ACK
        homep2p.clearHomesList();
        //dice a tutte le case della rete di essere entrata
        Message netEntranceMessage = new Message<Home>(Header.NET_ENTRANCE, homep2p.makeTimestamp(), new Home(id, ip, port));
        homep2p.broadCastMessageCustomHomesList(netEntranceMessage, tempHomesList);
        System.out.println("Inviato broadcast alle altre case");
        //avvia il simulatore
        SmartMeterBuffer buffer = new SmartMeterBuffer(24, 12);
        simulator = new SmartMeterSimulator(buffer);
        simulator.start();

        new HomeP2PGlobalStatsMaker().start();
        //new HomeP2PPrinter().start();

        System.out.println("Comandi casa:");
        System.out.println("#1 Esci dalla rete");
        System.out.println("#2 Richiedi boost");
        while(true) {
            int command = System.in.read();
            //49 corrisponde al numero 1, 50 al numero 2 (sulla tastiera)
            if(command == 49) {
                homep2p.leaveNetwork();
            }
            else if(command == 50) {
                if(!homep2p.isRequestingBoost() && !homep2p.isBoosting()) {
                    System.out.println("richiedo boost");
                    homep2p.requestBoost(true);
                }
                else if (homep2p.isRequestingBoost())
                    System.out.println("sto già richiedendo il boost");
                else
                    System.out.println("mi sto già boostando");
            }
        }
    }
}
