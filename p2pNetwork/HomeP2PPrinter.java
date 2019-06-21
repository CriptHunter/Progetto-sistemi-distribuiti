package p2pNetwork;

import beans.Home;
import server.ConsoleColors;

import java.util.List;

public class HomeP2PPrinter extends Thread {
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print(ConsoleColors.PURPLE_BOLD);
            System.out.println("------------------------------------------------------------------------");
            System.out.println("Lista case nella rete:");
            List<Home> homesList = HomeP2P.getInstance().getHomesList();
            if(homesList.size() == 0)
                System.out.println("non c'è nessun altra casa nella rete");
            else {
                for (Home h : HomeP2P.getInstance().getHomesList())
                    System.out.println("id casa: " + h.getId() + "| porta: " + h.getPort());
            }
            System.out.print(ConsoleColors.RESET);
        }
    }
}
