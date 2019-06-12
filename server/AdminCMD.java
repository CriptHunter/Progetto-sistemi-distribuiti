package server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import src.Home;
import src.Statistics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdminCMD {
    private static ClientConfig clientConfig;
    private static Client client;
    private static  String serverURL;
    private static final int NOT_FOUND = 404;
    private static BufferedReader br;

    public static void main(String[] args) {
        clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        client = Client.create(clientConfig);
        serverURL = "http://localhost:8888/rest/";
        br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.println();
            System.out.print(ConsoleColors.BLUE_BOLD);
            System.out.println("Seleziona un comando inserendo il numero corrispondente: ");
            System.out.print(ConsoleColors.BLUE);
            System.out.println("    #1 lista case");
            System.out.println("    #2 statistiche di una casa");
            System.out.println("    #3 media statistiche di una casa");
            System.out.println("    #4 deviazione standard statistiche di una casa");
            System.out.println("    #5 statistiche del condominio");
            System.out.println("    #6 media statistiche condominio");
            System.out.println("    #7 deviazione standard statistiche di un condominio");
            System.out.println("    #8 chiudi console");
            System.out.print(ConsoleColors.RESET);
            try {
                String s = br.readLine();
                int command = Integer.parseInt(s);
                selectCommand(command);
            } catch (NumberFormatException | IOException e) {
                System.err.println("Inserisci un numero!");
            }
        }
    }

    private static void selectCommand(int command) throws IOException {
        int homeId = 0;
        int n = 0;
        switch(command) {
            case 1:
                getHomeList();
                break;
            case 2:
                System.out.println("Inserisci l'id della casa:");
                homeId = Integer.parseInt(br.readLine());
                System.out.println("Inserisci il numero di statistiche locali da visualizzare:");
                n = Integer.parseInt(br.readLine());
                getLocalStatistics(homeId,n);
                break;
            case 3:
                System.out.println("Inserisci l'id della casa:");
                homeId = Integer.parseInt(br.readLine());
                System.out.println("Inserisci il numero di statistiche locali di cui vuoi calcolare la media:");
                n = Integer.parseInt(br.readLine());
                getLocalStatisticsAverage(homeId, n);
                break;
            case 4:
                System.out.println("Inserisci l'id della casa:");
                homeId = Integer.parseInt(br.readLine());
                System.out.println("Inserisci il numero di statistiche locali di cui vuoi calcolare la deviazione standard:");
                n = Integer.parseInt(br.readLine());
                getLocalStatisticsSTD(homeId, n);
                break;
            case 5:
                System.out.println("Inserisci il numero di statistiche globali da visualizzare:");
                n = Integer.parseInt(br.readLine());
                getGlobalStatistics(n);
                break;
            case 6:
                System.out.println("Inserisci il numero di statistiche globali di cui vuoi calcolare la media:");
                n = Integer.parseInt(br.readLine());
                getGlobalStatisticsAverage(n);
                break;
            case 7:
                System.out.println("Inserisci il numero di statistiche globali di cui vuoi calcolare la deviazione standard:");
                n = Integer.parseInt(br.readLine());
                getGlobalStatisticsSTD(n);
                break;
            case 8:
                System.exit(0);
            default:
                System.err.println("Il numero inserito non corrisponde a nessun comando");
        }
    }

    private static List<Home> getHomeList() {
        List<Home> homeList = new ArrayList<>();
        System.out.println("Lista case:");
        ClientResponse response = client.resource(serverURL + "home/get").get(ClientResponse.class);
        if(response.getStatus() == NOT_FOUND) {
            System.out.println("Nessuna casa registrata");
            return homeList;
        }
        homeList = response.getEntity(new GenericType<List<Home>>(){});
        for(Home h : homeList)
            System.out.println("id: " + h.getId() + " | ip: " + h.getIp() + " | port: " + h.getPort());
        return homeList;
    }

    private static List<Statistics> getLocalStatistics(int homeId, int n) {
        List<Statistics> statsList = new ArrayList<>();
        System.out.println("Ultime " + n + " statistiche delle casa " + homeId + ":" );
        ClientResponse response = client.resource(serverURL + "stats/local/get/" + homeId + "/" + n).get(ClientResponse.class);
        if(response.getStatus() == NOT_FOUND) {
            System.out.println("Nessuna statistica per la casa " + homeId);
            return statsList;
        }
        statsList = response.getEntity(new GenericType<List<Statistics>>(){});
        for(Statistics s : statsList)
            System.out.println("timestamp: " + s.getTimestamp() + " | value: " + s.getValue());
        return statsList;
    }

    private static List<Statistics> getGlobalStatistics(int n) {
        List<Statistics> statsList = new ArrayList<>();
        System.out.println("Ultime " + n + " statistiche del condominio:");
        ClientResponse response = client.resource(serverURL + "stats/global/get/" + n).get(ClientResponse.class);
        if(response.getStatus() == NOT_FOUND) {
            System.out.println("Nessuna statistica per il condominio");
            return statsList;
        }
        statsList = response.getEntity(new GenericType<List<Statistics>>(){});
        for(Statistics s : statsList)
            System.out.println("timestamp: " + s.getTimestamp() + " | value: " + s.getValue());
        return statsList;
    }

    private static double getGlobalStatisticsAverage(int n) {
        double average = client.resource(serverURL + "stats/global/get/average/" + n).get(Double.class);
        System.out.println("Media delle ultime " + n + " statistiche condominiali:");
        System.out.println(average);
        return average;
    }

    private static double getGlobalStatisticsSTD(int n)
    {
        double std = client.resource(serverURL + "stats/global/get/std/" + n).get(Double.class);
        System.out.println("deviazione standard delle ultime " + n + " statistiche condominiali:");
        System.out.println(std);
        return std;
    }

    private static double getLocalStatisticsAverage(int homeId, int n) {
        double average = client.resource(serverURL + "stats/local/get/average/" + homeId + "/" + n).get(Double.class);
        System.out.println("Media delle ultime " + n + " statistiche della casa " + homeId + ":");
        System.out.println(average);
        return average;
    }

    private static double getLocalStatisticsSTD(int homeId, int n) {
        double std = client.resource(serverURL + "stats/local/get/std/" + homeId + "/" + n).get(Double.class);
        System.out.println("deviazione standard delle ultime " + n + " statistiche della casa " + homeId + ":");
        System.out.println(std);
        return std;
    }
}
